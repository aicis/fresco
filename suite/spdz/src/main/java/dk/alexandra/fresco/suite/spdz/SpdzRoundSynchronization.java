package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolCollectionList;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.io.IOException;

/**
 * A default implementation of the round synchronization for spdz - mostly doing
 * the MAC check if needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private static final int macCheckThreshold = 100000;

  private void MACCheck(SpdzResourcePool resourcePool,
      SCENetwork sceNetworks) throws IOException {

    SpdzStorage storage = resourcePool.getStore();

    SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(
        resourcePool.getSecureRandom(),
        resourcePool.getMessageDigest(),
        storage,
        null, resourcePool.getModulus());

    int batchSize = 128;

    do {
      ProtocolCollectionList protocolCollectionList =
          new ProtocolCollectionList(batchSize);
      macCheck.getNextProtocols(protocolCollectionList);

      BatchedStrategy.processBatch(protocolCollectionList, sceNetworks, 0, resourcePool);
    } while (macCheck.hasNextProtocols());

    //reset boolean value
    resourcePool.setOutputProtocolInBatch(false);
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, SCENetwork sceNetwork)
      throws IOException {
    MACCheck(resourcePool, sceNetwork);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool,
      SCENetwork sceNetwork) throws IOException {
    gatesEvaluated += gatesEvaluated;
    if (gatesEvaluated > macCheckThreshold || resourcePool.isOutputProtocolInBatch()) {
      MACCheck(resourcePool, sceNetwork);
    }
  }
}
