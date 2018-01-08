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
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz.maccheck.MaliciousSpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.maccheck.MaliciousSpdzRoundSynchronization;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMaliciousBehaviour {

  enum Corrupt {
    COMMIT_ROUND_1, OPEN_COMMIT_ROUND_1, COMMIT_ROUND_2, OPEN_COMMIT_ROUND_2, INPUT
  }

  /**
   * Reset malicious behavior.
   */
  @Before
  public void reset() {
    MaliciousSpdzMacCheckProtocol.corruptCommitRound1 = false;
    MaliciousSpdzMacCheckProtocol.corruptOpenCommitRound1 = false;
    MaliciousSpdzMacCheckProtocol.corruptCommitRound2 = false;
    MaliciousSpdzMacCheckProtocol.corruptOpenCommitRound2 = false;

  }

  @Test
  public void testCommitmentCorruptRound1() throws Exception {
    try {
      runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
          Corrupt.COMMIT_ROUND_1);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause().getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testOpenCommitmentCorruptRound1() throws Exception {
    try {
      runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
          Corrupt.OPEN_COMMIT_ROUND_1);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause().getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testCommitmentCorruptRound2() throws Exception {
    try {
      runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
          Corrupt.COMMIT_ROUND_2);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause().getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testOpenCommitmentCorruptRound2() throws Exception {
    try {
      runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
          Corrupt.OPEN_COMMIT_ROUND_2);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause().getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testMaliciousInput() throws Exception {
    try {
      runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
          Corrupt.INPUT);
      Assert.fail("Should not go well");
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null || !(e.getCause().getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties, Corrupt corrupt) throws Exception {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite = null;
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
              () -> createResourcePool(playerId, noOfParties, new Random(), new SecureRandom()),
              () -> {
                KryoNetNetwork kryoNetwork = new KryoNetNetwork(netConf.get(playerId));
                return kryoNetwork;
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private SpdzResourcePool createResourcePool(int myId, int size, Random rand,
      SecureRandom secRand) {
    SpdzDataSupplier supplier = new SpdzDummyDataSupplier(myId, size);
    SpdzStorage store = new SpdzStorageImpl(supplier);
    return new SpdzResourcePoolImpl(myId, size, new HmacDrbg(), store);
  }

  private class MaliciousSpdzProtocolSuite implements ProtocolSuiteNumeric<SpdzResourcePool> {

    private final int maxBitLength;
    private Corrupt corrupt;

    public MaliciousSpdzProtocolSuite(int maxBitLength, Corrupt corrupt) {
      this.maxBitLength = maxBitLength;
      this.corrupt = corrupt;
      switch (corrupt) {
        case COMMIT_ROUND_1:
          MaliciousSpdzMacCheckProtocol.corruptCommitRound1 = true;
          break;
        case OPEN_COMMIT_ROUND_1:
          MaliciousSpdzMacCheckProtocol.corruptOpenCommitRound1 = true;
          break;
        case COMMIT_ROUND_2:
          MaliciousSpdzMacCheckProtocol.corruptCommitRound2 = true;
          break;
        case OPEN_COMMIT_ROUND_2:
          MaliciousSpdzMacCheckProtocol.corruptOpenCommitRound2 = true;
          break;
        default:
          break;
      }
    }

    @Override
    public BuilderFactoryNumeric init(SpdzResourcePool resourcePool, Network network) {
      BasicNumericContext spdzFactory = new BasicNumericContext(maxBitLength,
          resourcePool.getModulus(), resourcePool.getMyId(), resourcePool.getNoOfParties());
      if (resourcePool.getMyId() == 1 && corrupt.compareTo(Corrupt.INPUT) == 0) {
        return new MaliciousSpdzBuilder(spdzFactory);
      } else {
        return new SpdzBuilder(spdzFactory);
      }
    }

    @Override
    public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
      return new MaliciousSpdzRoundSynchronization();
    }

  }

}

