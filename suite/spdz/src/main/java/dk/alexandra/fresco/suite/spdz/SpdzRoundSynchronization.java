package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.io.IOException;
import java.util.List;

/**
 * A default implementation of the round synchronization for spdz - mostly doing the MAC check if
 * needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private static final int macCheckThreshold = 100000;
  private int gatesEvaluated = 0;

  private void doMACCheck(SpdzResourcePool resourcePool, SCENetwork sceNetworks)
      throws IOException {
    SpdzStorage storage = resourcePool.getStore();
    int batchSize = 128;

    if (!storage.getOpenedValues().isEmpty()) {
      SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(resourcePool.getSecureRandom(),
          resourcePool.getMessageDigest(), storage, null, resourcePool.getModulus());

      do {
        ProtocolCollectionList<SpdzResourcePool> protocolCollectionList =
            new ProtocolCollectionList<>(batchSize);
        macCheck.getNextProtocols(protocolCollectionList);

        BatchedStrategy.processBatch(protocolCollectionList, sceNetworks, 0, resourcePool);
      } while (macCheck.hasNextProtocols());
    }
  }

  private void checkMACsInBatch(SpdzResourcePool resourcePool, SCENetwork sceNetwork)
      throws IOException {
    doMACCheck(resourcePool, sceNetwork);

    // Check for output protocols in the batch. If any are present, evaluate them one at a time with
    // a MAC Check in between to prevent cheating.
    List<SpdzOutputProtocol<?>> outputProtocols = resourcePool.getOutputProtocolsInBatch();
    SpdzBatchedStrategy.processOutputProtocolBatch(outputProtocols, sceNetwork, resourcePool);
    doMACCheck(resourcePool, sceNetwork);
    // reset stuff to gain memory back and not check them again.
    resourcePool.getOutputProtocolsInBatch().clear();
    this.gatesEvaluated = 0;
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, SCENetwork sceNetwork)
      throws IOException {
    checkMACsInBatch(resourcePool, sceNetwork);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool,
      SCENetwork sceNetwork) throws IOException {
    this.gatesEvaluated += gatesEvaluated;
    if (this.gatesEvaluated > macCheckThreshold || resourcePool.isOutputProtocolInBatch()) {
      checkMACsInBatch(resourcePool, sceNetwork);
    }
  }
}
