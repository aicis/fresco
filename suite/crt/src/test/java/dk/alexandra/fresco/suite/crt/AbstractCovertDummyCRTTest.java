package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.MersennePrimeFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTCovertDataSupplier;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTDataSupplier;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePoolImpl;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTSequentialStrategy;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticBuilderFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public class AbstractCovertDummyCRTTest {

  // Note that the modulus on the right should have twice the bit length of that to the left in order for RandomModP to
  // work correctly.
  protected static final FieldDefinition DEFAULT_FIELD_LEFT =
      MersennePrimeFieldDefinition.find(64);
  protected static final FieldDefinition DEFAULT_FIELD_RIGHT = new BigIntegerFieldDefinition(
      new BigInteger(152 + 40, new Random(1234)).nextProbablePrime());

  public void runTest(
      TestThreadRunner.TestThreadFactory<CRTResourcePool<DummyArithmeticResourcePool, DummyArithmeticResourcePool>, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) {

    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
    Map<Integer,
        TestThreadRunner.TestThreadConfiguration<
            CRTResourcePool<DummyArithmeticResourcePool, DummyArithmeticResourcePool>,
            ProtocolBuilderNumeric>
        > conf = new HashMap<>();

    for (int playerId : netConf.keySet()) {

      BatchEvaluationStrategy<CRTResourcePool<DummyArithmeticResourcePool, DummyArithmeticResourcePool>> batchEvaluationStrategy =
          new CRTSequentialStrategy<>();
      DummyArithmeticResourcePool rpLeft = new DummyArithmeticResourcePoolImpl(playerId,
          noOfParties, DEFAULT_FIELD_LEFT);
      DummyArithmeticResourcePool rpRight = new DummyArithmeticResourcePoolImpl(playerId,
          noOfParties, DEFAULT_FIELD_RIGHT);
      CRTResourcePool<DummyArithmeticResourcePool, DummyArithmeticResourcePool> rp =
          new CRTResourcePoolImpl<>(playerId, noOfParties, null, rpLeft, rpRight);

      CRTProtocolSuite<DummyArithmeticResourcePool, DummyArithmeticResourcePool> ps =
          new CRTProtocolSuite<>(
              new DummyArithmeticBuilderFactory(new BasicNumericContext(DEFAULT_FIELD_LEFT.getBitLength() - 24,
              playerId, noOfParties, DEFAULT_FIELD_LEFT, 16, 40)),
              new DummyArithmeticBuilderFactory(new BasicNumericContext(DEFAULT_FIELD_RIGHT.getBitLength()- 40,
                      playerId, noOfParties, DEFAULT_FIELD_RIGHT, 16, 40)));
      ProtocolEvaluator<CRTResourcePool<DummyArithmeticResourcePool,
          DummyArithmeticResourcePool>> evaluator =
          new BatchedProtocolEvaluator<>(batchEvaluationStrategy, ps);

      NetworkConfiguration partyNetConf = netConf.get(playerId);
      SecureComputationEngine<CRTResourcePool<DummyArithmeticResourcePool,
          DummyArithmeticResourcePool>, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);

      Supplier<Network> networkSupplier =  () -> new SocketNetwork(partyNetConf);
      CRTDataSupplier dataSupplier = new CRTCovertDataSupplier<DummyArithmeticResourcePool,
                DummyArithmeticResourcePool>(
              rp);

      TestThreadRunner.TestThreadConfiguration<
          CRTResourcePool<DummyArithmeticResourcePool, DummyArithmeticResourcePool>,
          ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new CRTResourcePoolImpl<>(playerId, noOfParties, dataSupplier, rpLeft, rpRight),
              networkSupplier);
      conf.put(playerId, ttc);
    }

    TestThreadRunner.run(f, conf);
  }

}
