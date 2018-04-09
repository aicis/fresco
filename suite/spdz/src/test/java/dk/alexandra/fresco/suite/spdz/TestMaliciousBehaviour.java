package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz.maccheck.MaliciousSpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.maccheck.MaliciousSpdzRoundSynchronization;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMaliciousBehaviour {

  enum Corrupt {
    COMMIT_ROUND, OPEN_COMMIT_ROUND, INPUT
  }

  /**
   * Reset malicious behavior.
   */
  @Before
  public void reset() {
    MaliciousSpdzMacCheckProtocol.corruptCommitRound = false;
    MaliciousSpdzMacCheckProtocol.corruptOpenCommitRound = false;
  }

  @Test
  public void testCommitmentCorruptRound2() {
    try {
      runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
          Corrupt.COMMIT_ROUND);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause()
          .getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testOpenCommitmentCorruptRound2() {
    try {
      runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
          Corrupt.OPEN_COMMIT_ROUND);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause()
          .getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testMaliciousInput() {
    try {
      runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
          Corrupt.INPUT);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause()
          .getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties, Corrupt corrupt) {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite;
      if (playerId == 1) {
        protocolSuite = new MaliciousSpdzProtocolSuite(150, corrupt);
      } else {
        protocolSuite = new SpdzProtocolSuite(150);
      }
      BatchEvaluationStrategy<SpdzResourcePool> batchEvalStrat = evalStrategy.getStrategy();

      ProtocolEvaluator<SpdzResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(batchEvalStrat, protocolSuite);

      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(protocolSuite, evaluator);

      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> createResourcePool(playerId, noOfParties),
              () -> new KryoNetNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private SpdzResourcePool createResourcePool(int myId, int size) {
    return new SpdzResourcePoolImpl(myId, size, new SpdzOpenedValueStoreImpl(),
        new SpdzDummyDataSupplier(myId, size),
        new AesCtrDrbg(new byte[32]));
  }

  private class MaliciousSpdzProtocolSuite extends SpdzProtocolSuite {

    private Corrupt corrupt;

    MaliciousSpdzProtocolSuite(int maxBitLength, Corrupt corrupt) {
      super(maxBitLength);
      this.corrupt = corrupt;
      switch (corrupt) {
        case COMMIT_ROUND:
          MaliciousSpdzMacCheckProtocol.corruptCommitRound = true;
          break;
        case OPEN_COMMIT_ROUND:
          MaliciousSpdzMacCheckProtocol.corruptOpenCommitRound = true;
          break;
        default:
          break;
      }
    }

    @Override
    public BuilderFactoryNumeric init(SpdzResourcePool resourcePool, Network network) {
      BasicNumericContext spdzFactory = createNumericContext(resourcePool);
      RealNumericContext realNumericContext = createRealNumericContext();
      if (resourcePool.getMyId() == 1 && corrupt.compareTo(Corrupt.INPUT) == 0) {
        return new MaliciousSpdzBuilder(spdzFactory, realNumericContext);
      } else {
        return new SpdzBuilder(spdzFactory, realNumericContext);
      }
    }

    @Override
    public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
      return new MaliciousSpdzRoundSynchronization(this);
    }
  }
}

