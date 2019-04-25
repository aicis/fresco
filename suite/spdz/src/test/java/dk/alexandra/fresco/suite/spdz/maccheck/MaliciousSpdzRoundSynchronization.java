package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.spdz.SpdzBuilder;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzRoundSynchronization;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.security.SecureRandom;
import java.util.function.Function;

public class MaliciousSpdzRoundSynchronization extends SpdzRoundSynchronization {

  private final SpdzProtocolSuite hacked;
  private final Function<SpdzResourcePool, SpdzBuilder> builder;

  public MaliciousSpdzRoundSynchronization(SpdzProtocolSuite spdzProtocolSuite,
      Function<SpdzResourcePool, SpdzBuilder> builder) {
    super(spdzProtocolSuite);
    this.hacked = spdzProtocolSuite;
    this.builder = builder;
  }

  @Override
  protected void doMacCheck(SpdzResourcePool resourcePool, Network network) {
    OpenedValueStore<SpdzSInt, FieldElement> store = resourcePool.getOpenedValueStore();
    MaliciousSpdzMacCheckComputation macCheck = new MaliciousSpdzMacCheckComputation(
        new SecureRandom(),
        resourcePool.getMessageDigest(),
        store.popValues(),
        resourcePool.getFieldDefinition().getModulus(),
        resourcePool::createRandomGenerator,
        resourcePool.getDataSupplier().getSecretSharedKey());
    BatchEvaluationStrategy<SpdzResourcePool> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<SpdzResourcePool> evaluator =
        new BatchedProtocolEvaluator<>(batchStrategy, hacked, getBatchSize());
    ProtocolBuilderNumeric sequential = builder.apply(resourcePool).createSequential();
    macCheck.buildComputation(sequential);
    evaluator.eval(sequential.build(), resourcePool, network);
  }
}
