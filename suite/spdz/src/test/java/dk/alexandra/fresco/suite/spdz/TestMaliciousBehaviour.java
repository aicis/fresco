package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.lib.common.compare.CompareTests;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.suite.spdz.maccheck.MaliciousSpdzMacCheckComputation;
import dk.alexandra.fresco.suite.spdz.maccheck.MaliciousSpdzRoundSynchronization;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.math.BigInteger;
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
    MaliciousSpdzMacCheckComputation.corruptCommitRound = false;
    MaliciousSpdzMacCheckComputation.corruptOpenCommitRound = false;
  }

  @Test
  public void testCommitmentCorruptRound2() {
    try {
      runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
          Corrupt.COMMIT_ROUND, 1);
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
          Corrupt.OPEN_COMMIT_ROUND, 1);
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
          Corrupt.INPUT, 1);
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
      EvaluationStrategy evalStrategy, int noOfParties, Corrupt corrupt, int cheatingParty) {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
    Map<Integer, TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite;
      if (playerId == cheatingParty) {
        protocolSuite = new MaliciousSpdzProtocolSuite(150, corrupt, cheatingParty);
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
              () -> new SocketNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private SpdzResourcePool createResourcePool(int myId, int size) {
    BigInteger modulus = ModulusFinder.findSuitableModulus(512);
    return new SpdzResourcePoolImpl(myId, size, new SpdzOpenedValueStoreImpl(),
        new SpdzDummyDataSupplier(myId, size,
            new BigIntegerFieldDefinition(modulus), modulus),
        AesCtrDrbg::new);
  }

  private class MaliciousSpdzProtocolSuite extends SpdzProtocolSuite {

    private Corrupt corrupt;
    private final int cheatingParty;

    MaliciousSpdzProtocolSuite(int maxBitLength, Corrupt corrupt, int cheatingParty) {
      super(maxBitLength);
      this.corrupt = corrupt;
      this.cheatingParty = cheatingParty;
      switch (corrupt) {
        case COMMIT_ROUND:
          MaliciousSpdzMacCheckComputation.corruptCommitRound = true;
          break;
        case OPEN_COMMIT_ROUND:
          MaliciousSpdzMacCheckComputation.corruptOpenCommitRound = true;
          break;
        default:
          break;
      }
    }

    @Override
    public BuilderFactoryNumeric init(SpdzResourcePool resourcePool) {
      BasicNumericContext numericContext = createNumericContext(resourcePool);
      if (resourcePool.getMyId() == cheatingParty && corrupt.compareTo(Corrupt.INPUT) == 0) {
        return new MaliciousSpdzBuilder(numericContext);
      } else {
        return new SpdzBuilder(numericContext);
      }
    }

    @Override
    public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
      return new MaliciousSpdzRoundSynchronization(this, (resPool) -> {
        BasicNumericContext numericContext = createNumericContext(resPool);
        if (resPool.getMyId() == cheatingParty && corrupt.compareTo(Corrupt.INPUT) == 0) {
          return new MaliciousSpdzBuilder(numericContext);
        } else {
          return new SpdzBuilder(numericContext);
        }
      });
    }
  }
}

