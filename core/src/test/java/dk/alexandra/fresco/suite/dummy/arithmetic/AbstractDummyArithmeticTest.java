package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
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
import dk.alexandra.fresco.framework.util.DetermSecureRandom;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.PerformanceLogger.Flag;
import dk.alexandra.fresco.logging.SCELoggingDecorator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractDummyArithmeticTest {

  /**
   * Runs test with default modulus and no performance logging. i.e. standard test setup.
   */
  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties)
      throws Exception {
    BigInteger mod = new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
    runTest(f, evalStrategy, noOfParties, mod, null);
  }

  /**
   * Runs test with all parameters free. Only the starting port of 9000 is chosen by default.
   */
  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties,
      BigInteger mod, EnumSet<Flag> performanceLoggerFlags) throws Exception {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    List<PerformanceLogger> pls = new ArrayList<>();
    for (int playerId : netConf.keySet()) {

      NetworkConfiguration partyNetConf = netConf.get(playerId);

      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(mod, 200);

      BatchEvaluationStrategy<DummyArithmeticResourcePool> batchEvaluationStrategy =
          EvaluationStrategy.fromEnum(evalStrategy);
      if (performanceLoggerFlags != null && performanceLoggerFlags
          .contains(Flag.LOG_NATIVE_BATCH)) {
        batchEvaluationStrategy =
            new BatchEvaluationLoggingDecorator<>(batchEvaluationStrategy);
        pls.add((PerformanceLogger) batchEvaluationStrategy);
      }
      ProtocolEvaluator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> evaluator =
          new BatchedProtocolEvaluator<>(batchEvaluationStrategy);
      Network network;
      KryoNetNetwork kryoNetwork = new KryoNetNetwork();
      if (performanceLoggerFlags != null && performanceLoggerFlags.contains(Flag.LOG_NETWORK)) {
        network = new NetworkLoggingDecorator(kryoNetwork);
        pls.add((PerformanceLogger) network);
      } else {
        network = kryoNetwork;
      }

      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);
      if (performanceLoggerFlags != null && performanceLoggerFlags.contains(Flag.LOG_RUNTIME)) {
        sce = new SCELoggingDecorator<>(sce, ps);
        pls.add((PerformanceLogger) sce);
      }

      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(
              sce, () -> {
            kryoNetwork.init(partyNetConf);
            return new DummyArithmeticResourcePoolImpl(playerId,
                noOfParties,
                network, new Random(0), new DetermSecureRandom(), mod);
          });
      conf.put(playerId, ttc);
    }

    TestThreadRunner.run(f, conf);
    int id = 1;
    for (PerformanceLogger pl : pls) {
      pl.printPerformanceLog(id++);
      pl.reset();
    }
  }
}
