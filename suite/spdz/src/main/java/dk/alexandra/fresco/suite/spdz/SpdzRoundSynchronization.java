package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.stream.StreamSupport;

/**
 * A default implementation of the round synchronization for spdz - mostly doing the MAC check if
 * needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private final int openValueThreshold;
  private final SpdzProtocolSuite spdzProtocolSuite;
  private final SecureRandom secRand;
  private boolean isCheckRequired = false;
  private final int batchSize;

  public SpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite, int openValueThreshold,
      int batchSize) {
    this.spdzProtocolSuite = spdzProtocolSuite;
    this.secRand = new SecureRandom();
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
  }

  public SpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite) {
    this(spdzProtocolSuite, 1000000, 128);
  }

  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
    SpdzBuilder spdzBuilder = new SpdzBuilder(
        spdzProtocolSuite.createNumericContext(resourcePool),
        spdzProtocolSuite.createRealNumericContext());
    BatchEvaluationStrategy<SpdzResourcePool> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<SpdzResourcePool> evaluator =
        new BatchedProtocolEvaluator<>(batchStrategy, spdzProtocolSuite, batchSize);
    OpenedValueStore<SpdzSInt, BigInteger> store = resourcePool.getOpenedValueStore();
    SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(secRand,
        resourcePool.getMessageDigest(),
        store.popValues(),
        resourcePool.getModulus(),
        resourcePool.getRandomGenerator(),
        resourcePool.getDataSupplier().getSecretSharedKey());
    ProtocolBuilderNumeric sequential = spdzBuilder.createSequential();
    macCheck.buildComputation(sequential);
    evaluator.eval(sequential.build(), resourcePool, network);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool, Network network) {
    OpenedValueStore<SpdzSInt, BigInteger> store = resourcePool.getOpenedValueStore();
    if (isCheckRequired || store.exceedsThreshold(openValueThreshold)) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, Network network) {
    OpenedValueStore<SpdzSInt, BigInteger> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues()) {
      doMacCheck(resourcePool, network);
    }
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<SpdzResourcePool> protocols, SpdzResourcePool resourcePool,
      Network network) {
    isCheckRequired = StreamSupport.stream(protocols.spliterator(), false)
        .anyMatch(p -> p instanceof SpdzOutputProtocol);
    OpenedValueStore<SpdzSInt, BigInteger> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues() && isCheckRequired) {
      doMacCheck(resourcePool, network);
    }
  }

  public int getBatchSize() {
    return batchSize;
  }

}
