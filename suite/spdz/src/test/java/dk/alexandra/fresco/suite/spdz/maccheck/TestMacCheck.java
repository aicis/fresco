package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkTestUtils;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests.TestDivision;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestMacCheck {

  @Test
  public void testMacCorrupt() throws Exception {
    try {
      runTest(new TestDivision<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2, true);
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null
          || !(e.getCause().getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  @Test
  public void testClosedValuesIncorrectSize() throws Exception {
    try {
      runTest(new TestDivision<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2, false);
    } catch (RuntimeException e) {
      if (e.getCause().getCause() == null
          || !(e.getCause().getCause() instanceof MaliciousException)) {
        Assert.fail();
      }
    }
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties, boolean corruptMac) throws Exception {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        NetworkTestUtils.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite = new SpdzProtocolSuite(150);
      BatchEvaluationStrategy<SpdzResourcePool> batchEvalStrat = evalStrategy.getStrategy();

      ProtocolEvaluator<SpdzResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(batchEvalStrat, protocolSuite);

      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(protocolSuite, evaluator);

      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce, () -> createResourcePool(playerId,
              noOfParties, new Random(), new SecureRandom(), corruptMac),
              () -> new AsyncNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private SpdzResourcePool createResourcePool(int myId, int size, Random rand, SecureRandom secRand,
      boolean corruptMac) {
    SpdzDataSupplier supplier;
    if (myId == 1 && corruptMac) {
      supplier = new DummyMaliciousDataSupplier(myId, size);
    } else {
      supplier = new SpdzDummyDataSupplier(myId, size);
    }
    return new SpdzResourcePoolImpl(myId, size, new SpdzOpenedValueStoreImpl(), supplier,
        new AesCtrDrbg(new byte[32]));
  }

  private class DummyMaliciousDataSupplier extends SpdzDummyDataSupplier {

    int maliciousCountdown = 10;

    DummyMaliciousDataSupplier(int myId, int numberOfPlayers) {
      super(myId, numberOfPlayers);
    }

    @Override
    public SpdzTriple getNextTriple() {
      maliciousCountdown--;
      SpdzTriple trip = super.getNextTriple();
      if (maliciousCountdown == 0) {
        BigInteger share = trip.getA().getShare();
        share = share.add(BigInteger.ONE);
        SpdzSInt newA = new SpdzSInt(share, trip.getA().getMac(), getModulus());
        trip = new SpdzTriple(newA, trip.getB(), trip.getC());
      }
      return trip;
    }

  }
}
