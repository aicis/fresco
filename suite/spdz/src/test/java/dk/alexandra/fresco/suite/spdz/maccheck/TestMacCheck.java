package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests.TestSecretSharedDivision;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
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
      runTest(new TestSecretSharedDivision<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2, true);
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
      runTest(new TestSecretSharedDivision<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2, false);
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
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
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
              noOfParties, new Random(), new SecureRandom(), corruptMac), () -> {
            KryoNetNetwork kryoNetwork = new KryoNetNetwork(netConf.get(playerId));
            return kryoNetwork;
          });
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
    SpdzStorage store;
    if (!corruptMac) {
      store = new MaliciousSpdzStorage(supplier);
    } else {
      store = new SpdzStorageImpl(supplier);
    }

    return new SpdzResourcePoolImpl(myId, size, new HmacDrbg(), store);
  }

  private class MaliciousSpdzStorage extends SpdzStorageImpl {

    public MaliciousSpdzStorage(SpdzDataSupplier supplier) {
      super(supplier);
    }

    @Override
    public List<SpdzElement> getClosedValues() {
      return new ArrayList<SpdzElement>();
    }

  }

  private class DummyMaliciousDataSupplier extends SpdzDummyDataSupplier {

    int maliciousCountdown = 10;

    public DummyMaliciousDataSupplier(int myId, int numberOfPlayers) {
      super(myId, numberOfPlayers);
    }

    @Override
    public SpdzTriple getNextTriple() {
      maliciousCountdown--;
      SpdzTriple trip = super.getNextTriple();
      if (maliciousCountdown == 0) {
        BigInteger share = trip.getA().getShare();
        share = share.add(BigInteger.ONE);
        SpdzElement newA = new SpdzElement(share, trip.getA().getMac(), getModulus());
        trip = new SpdzTriple(newA, trip.getB(), trip.getC());
      }
      return trip;
    }

  }
}
