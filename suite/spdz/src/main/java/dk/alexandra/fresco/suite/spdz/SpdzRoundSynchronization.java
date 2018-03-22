package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
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

  private final int macCheckThreshold;
  private final SpdzProtocolSuite spdzProtocolSuite;
  private final SecureRandom secRand;
  private boolean doMacCheck = false;

  public SpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite) {
    this.spdzProtocolSuite = spdzProtocolSuite;
    this.secRand = new SecureRandom();
    this.macCheckThreshold = 1000000;
  }

  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
    SpdzStorage storage = resourcePool.getStore();
    int batchSize = 128;

    //Ensure that we have any values to do MAC check on
    if (!storage.getOpenedValues().isEmpty() && !storage.isBeingChecked()) {
      SpdzBuilder spdzBuilder = new SpdzBuilder(
          spdzProtocolSuite.createNumericContext(resourcePool));
      BatchEvaluationStrategy<SpdzResourcePool> batchStrategy = new BatchedStrategy<>();
      BatchedProtocolEvaluator<SpdzResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(batchStrategy, spdzProtocolSuite, batchSize);

      SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(secRand,
          resourcePool.getMessageDigest(), storage, resourcePool.getModulus(),
          resourcePool.getRandomGenerator());
      ProtocolBuilderNumeric sequential = spdzBuilder.createSequential();
      macCheck.buildComputation(sequential);
      evaluator.eval(sequential.build(), resourcePool, network);
      storage.toggleIsBeingChecked();
    }
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, Network network) {
    doMacCheck(resourcePool, network);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool, Network network) {
    int numUnchecked = resourcePool.getStore().getOpenedValues().size();
    if (numUnchecked > macCheckThreshold || doMacCheck) {
      doMacCheck(resourcePool, network);
      doMacCheck = false;
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
