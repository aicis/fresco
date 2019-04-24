package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import java.security.SecureRandom;
import java.util.stream.StreamSupport;

/**
 * A default implementation of the round synchronization for spdz - mostly doing the MAC check if
 * needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private static final int DEFAULT_VALUE_THRESHOLD = 1000000;
  private static final int DEFAULT_BATCH_SIZE = 128;
  private final int openValueThreshold;
  private final SpdzProtocolSuite spdzProtocolSuite;
  private final SecureRandom secRand;
  private boolean isCheckRequired = false;
  private final int batchSize;

  /**
   * Creates new {@link SpdzRoundSynchronization}.
   *
   * @param spdzProtocolSuite the spdz protocol suite which we will use for the mac-check
   *     computation
   * @param openValueThreshold number of open values we accumulating before forcing mac-check
   *     (the mac-check will always run if there are output gates but in order to reduce memory
   *     usage we will run the mac-check even when there are no output gates yet but the threshold
   *     is exceeded)
   * @param batchSize batch size for mac-check protocol
   */
  public SpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite, int openValueThreshold,
      int batchSize) {
    this.spdzProtocolSuite = spdzProtocolSuite;
    this.secRand = new SecureRandom();
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
  }

  public SpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite) {
    this(spdzProtocolSuite, DEFAULT_VALUE_THRESHOLD, DEFAULT_BATCH_SIZE);
  }

  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
    SpdzBuilder spdzBuilder = new SpdzBuilder(
        spdzProtocolSuite.createNumericContext(resourcePool),
        spdzProtocolSuite.createRealNumericContext());
    BatchEvaluationStrategy<SpdzResourcePool> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<SpdzResourcePool> evaluator =
        new BatchedProtocolEvaluator<>(batchStrategy, spdzProtocolSuite, batchSize);
    OpenedValueStore<SpdzSInt, FieldElement> store = resourcePool.getOpenedValueStore();
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
    OpenedValueStore<SpdzSInt, FieldElement> store = resourcePool.getOpenedValueStore();
    if (isCheckRequired) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    } else if (store.exceedsThreshold(openValueThreshold)) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, Network network) {
    OpenedValueStore<SpdzSInt, FieldElement> store = resourcePool.getOpenedValueStore();
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
    OpenedValueStore<SpdzSInt, FieldElement> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues() && isCheckRequired) {
      doMacCheck(resourcePool, network);
    }
  }

  protected int getBatchSize() {
    return batchSize;
  }
}
