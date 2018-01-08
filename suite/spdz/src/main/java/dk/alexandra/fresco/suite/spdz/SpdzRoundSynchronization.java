package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.security.SecureRandom;

/**
 * A default implementation of the round synchronization for spdz - mostly doing the MAC check if
 * needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private static final int macCheckThreshold = 100000;
  private int gatesEvaluated = 0;
  private boolean doMacCheck = false;
  private final SecureRandom secRand;

  public SpdzRoundSynchronization() {
    this.secRand = new SecureRandom();
  }
  
  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(
            resourcePool.getNoOfParties(),
            network);
    SpdzStorage storage = resourcePool.getStore();
    int batchSize = 128;

    //Ensure that we have any values to do MAC check on
    if (!storage.getOpenedValues().isEmpty()) {
      SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(secRand,
          resourcePool.getMessageDigest(), storage, resourcePool.getModulus());

      do {
        ProtocolCollectionList<SpdzResourcePool> protocolCollectionList =
            new ProtocolCollectionList<>(batchSize);
        macCheck.getNextProtocols(protocolCollectionList);
        BatchEvaluationStrategy<SpdzResourcePool> batchStrat = new BatchedStrategy<>();
        batchStrat.processBatch(protocolCollectionList, resourcePool, networkBatchDecorator);
      } while (macCheck.hasNextProtocols());
    }
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, Network network) {
    doMacCheck(resourcePool, network);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool, Network network) {
    this.gatesEvaluated += gatesEvaluated;
    if (this.gatesEvaluated > macCheckThreshold || doMacCheck) {
      doMacCheck(resourcePool, network);
      doMacCheck = false;
      this.gatesEvaluated = 0;
    }
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<SpdzResourcePool> protocols, SpdzResourcePool resourcePool,
      Network network) {
    // If an output gate resides within the next batch, we need to do a MAC check on all previous
    // gates which lead to this output gate.
    protocols.forEach(p -> {
      if (p instanceof SpdzOutputProtocol) {
        doMacCheck = true;
      }
    });
    if (doMacCheck) {
      doMacCheck(resourcePool, network);
    }
  }
}
