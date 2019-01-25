package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * A default implementation of the round synchronization for spdz - mostly doing the MAC check if
 * needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private static final int DEFAULT_VALUE_THRESHOLD = 300000;
  private static final int DEFAULT_BATCH_SIZE = 128;
  private final int openValueThreshold;
  private final SpdzProtocolSuite spdzProtocolSuite;
  private final SecureRandom secRand;
  private boolean isCheckRequired;
  private final int batchSize;

  /**
   * Creates new {@link SpdzRoundSynchronization}.
   *
   * @param spdzProtocolSuite the spdz protocol suite which we will use for the mac-check
   * computation
   * @param openValueThreshold number of open values we accumulating before forcing mac-check (the
   * mac-check will always run if there are output gates but in order to reduce memory usage we will
   * run the mac-check even when there are no output gates yet but the threshold is exceeded)
   * @param batchSize batch size for mac-check protocol
   */
  public SpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite, int openValueThreshold,
      int batchSize) {
    this.spdzProtocolSuite = spdzProtocolSuite;
    this.secRand = new SecureRandom();
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  public SpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite) {
    this(spdzProtocolSuite, DEFAULT_VALUE_THRESHOLD, DEFAULT_BATCH_SIZE);
  }

  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
//    Pair<List<SpdzSInt>, List<BigInteger>> bar = resourcePool.getOpenedValueStore().popValues();
//    bar.getFirst().clear();
//    bar.getSecond().clear();
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
    if (isCheckRequired) {
//      System.out.println("Because required finished batch");
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    } else if (store.exceedsThreshold(openValueThreshold)) {
//      System.out.println("Because exceeds ");
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, Network network) {
    OpenedValueStore<SpdzSInt, BigInteger> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues()) {
//      System.out.println("Because eval finished");
      doMacCheck(resourcePool, network);
    }
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<SpdzResourcePool> protocols, SpdzResourcePool resourcePool,
      Network network) {
    final boolean outputFound = StreamSupport.stream(protocols.spliterator(), false)
        .anyMatch(p -> p instanceof SpdzOutputProtocol);
//    System.out.println(outputFound);
    this.isCheckRequired = outputFound;
    OpenedValueStore<SpdzSInt, BigInteger> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues() && this.isCheckRequired) {
//      System.out.println("Because of output");
      doMacCheck(resourcePool, network);
    }
  }

  public int getBatchSize() {
    return batchSize;
  }

}
