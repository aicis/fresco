package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
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
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests.TestDivision;
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

  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(
      ModulusFinder.findSuitableModulus(512));

  @Test
  public void testMacCorrupt() {
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
  public void testClosedValuesIncorrectSize() {
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
      EvaluationStrategy evalStrategy, int noOfParties, boolean corruptMac) {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
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
              noOfParties, corruptMac),
              () -> new SocketNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
  }

  private SpdzResourcePool createResourcePool(int myId, int size,
      boolean corruptMac) {
    BigInteger modulus = ModulusFinder.findSuitableModulus(512);
    SpdzDataSupplier supplier;
    if (myId == 1 && corruptMac) {
      supplier = new DummyMaliciousDataSupplier(myId, size,
          new BigIntegerFieldDefinition(modulus.toString()), modulus);
    } else {
      supplier = new SpdzDummyDataSupplier(myId, size,
          new BigIntegerFieldDefinition(modulus), modulus);
    }
    return new SpdzResourcePoolImpl(myId, size, new SpdzOpenedValueStoreImpl(), supplier,
        AesCtrDrbg::new);
  }

  private class DummyMaliciousDataSupplier extends SpdzDummyDataSupplier {

    int maliciousCountdown = 10;

    DummyMaliciousDataSupplier(int myId, int numberOfPlayers, FieldDefinition fieldDefinition,
        BigInteger secretSharedKey) {
      super(myId, numberOfPlayers, fieldDefinition, secretSharedKey);
    }

    @Override
    public SpdzTriple getNextTriple() {
      maliciousCountdown--;
      SpdzTriple trip = super.getNextTriple();
      if (maliciousCountdown == 0) {
        FieldElement share = trip.getA().getShare();
        share.add(definition.createElement(1));
        SpdzSInt newA = new SpdzSInt(share, trip.getA().getMac());
        trip = new SpdzTriple(newA, trip.getB(), trip.getC());
      }
      return trip;
    }
  }
}
