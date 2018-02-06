package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.DefaultPerformancePrinter;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.NumericSuiteLogging;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.PerformanceLoggerCountingAggregate;
import dk.alexandra.fresco.logging.PerformancePrinter;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractDummyArithmeticTest {

  protected Map<Integer, PerformanceLogger> performanceLoggers = new HashMap<>();
  private final BigInteger defaultMod = new BigInteger(
      "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
  private final int defaultMaxBitLength = 200;

  /**
   * Runs test with default modulus, max bit length, and no performance logging. i.e. standard test
   * setup.
   */
  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties) {
    runTest(f, evalStrategy, noOfParties, defaultMod, defaultMaxBitLength, false);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties, boolean logPerformance) {
    runTest(f, evalStrategy, noOfParties, defaultMod, defaultMaxBitLength, logPerformance);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties, BigInteger modulus) {
    runTest(f, evalStrategy, noOfParties, modulus, defaultMaxBitLength, false);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties,
      BigInteger mod, int maxBitLength, boolean logPerformance) {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      PerformanceLoggerCountingAggregate aggregate
          = new PerformanceLoggerCountingAggregate();

      NetworkConfiguration partyNetConf = netConf.get(playerId);

      ProtocolSuiteNumeric<DummyArithmeticResourcePool> ps = new DummyArithmeticProtocolSuite(mod,
          maxBitLength);
      if (logPerformance) {
        ps = new NumericSuiteLogging<>(ps);
        aggregate.add((PerformanceLogger) ps);
      }

      BatchEvaluationStrategy<DummyArithmeticResourcePool> batchEvaluationStrategy =
          evalStrategy.getStrategy();
      if (logPerformance) {
        batchEvaluationStrategy =
            new BatchEvaluationLoggingDecorator<>(batchEvaluationStrategy);
        aggregate.add((PerformanceLogger) batchEvaluationStrategy);
      }
      ProtocolEvaluator<DummyArithmeticResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(batchEvaluationStrategy, ps);
      if (logPerformance) {
        evaluator = new EvaluatorLoggingDecorator<>(evaluator);
        aggregate.add((PerformanceLogger) evaluator);
      }

      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);

      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(
              sce,
              () -> new DummyArithmeticResourcePoolImpl(playerId,
                  noOfParties, drbg, mod),
              () -> {
                KryoNetNetwork kryoNetwork = new KryoNetNetwork(partyNetConf);
                if (logPerformance) {
                  NetworkLoggingDecorator network = new NetworkLoggingDecorator(kryoNetwork);
                  aggregate.add(network);
                  return network;
                } else {
                  return kryoNetwork;
                }
              });

      conf.put(playerId, ttc);
      performanceLoggers.putIfAbsent(playerId, aggregate);
    }

    TestThreadRunner.run(f, conf);
    PerformancePrinter printer = new DefaultPerformancePrinter();
    for (PerformanceLogger pl : performanceLoggers.values()) {
      printer.printPerformanceLog(pl);
    }
  }
}
