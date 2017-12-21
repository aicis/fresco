package dk.alexandra.fresco.logging;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.logging.arithmetic.ComparisonLoggerDecorator;
import dk.alexandra.fresco.logging.arithmetic.NumericLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TestNumericSuiteLoggingDecorators {

  @Test
  public void testNumericLoggingDecorator() throws Exception {
    //Test a single conditional select (1 mult and 1 sub (consisting of 1 add)).
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = ConditionalSelectTests.testSelectLeft();

    int noOfParties = 2;
    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL;

    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    BigInteger mod = new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");

    Map<Integer, NumericSuiteLogging<DummyArithmeticResourcePool>> performanceLoggers 
      = new HashMap<>();

    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);

      NumericSuiteLogging<DummyArithmeticResourcePool> ps =
          new NumericSuiteLogging<>(new DummyArithmeticProtocolSuite(mod, 200));
      performanceLoggers.put(playerId, ps);
      BatchEvaluationStrategy<DummyArithmeticResourcePool> strat = evalStrategy.getStrategy();
      ProtocolEvaluator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> evaluator
          = new BatchedProtocolEvaluator<>(strat, ps);
      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce
          = new SecureComputationEngineImpl<>(ps, evaluator);

      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new DummyArithmeticResourcePoolImpl(playerId,
                  noOfParties, drbg, mod),
              () -> {
                return new KryoNetNetwork(partyNetConf);
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    for (Integer pId : netConf.keySet()) {
      PerformanceLogger pl = performanceLoggers.get(pId);

      Map<String, Long> loggedValues = pl.getLoggedValues();
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_ADD), is((long) 1));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_MULT), is((long) 1));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_SUB), is((long) 1));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_BIT), is((long) 0));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_RAND), is((long) 0));

      pl.reset();
      loggedValues = pl.getLoggedValues();
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_ADD), is((long) 0));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_MULT), is((long) 0));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_SUB), is((long) 0));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_BIT), is((long) 0));
      assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_RAND), is((long) 0));
    }
  }

  @Test
  public void testComparisonLoggingDecorator() throws Exception {
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new CompareTests.TestCompareEQ<>();

    int noOfParties = 2;
    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL;

    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    BigInteger mod = new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");

    Map<Integer, NumericSuiteLogging<DummyArithmeticResourcePool>> performanceLoggers 
      = new HashMap<>();

    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);

      NumericSuiteLogging<DummyArithmeticResourcePool> ps = new NumericSuiteLogging<>(
          new DummyArithmeticProtocolSuite(mod, 200));
      performanceLoggers.put(playerId, ps);

      BatchEvaluationStrategy<DummyArithmeticResourcePool> strat = evalStrategy.getStrategy();
      ProtocolEvaluator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> evaluator
          = new BatchedProtocolEvaluator<>(strat, ps);
      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce
          = new SecureComputationEngineImpl<>(ps, evaluator);

      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new DummyArithmeticResourcePoolImpl(playerId,
                  noOfParties, drbg, mod),
              () -> {
                return new KryoNetNetwork(partyNetConf);
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    for (Integer partyId : netConf.keySet()) {
      PerformanceLogger logger = performanceLoggers.get(partyId);
      new DefaultPerformancePrinter().printPerformanceLog(logger);

      Map<String, Long> loggedValues = logger.getLoggedValues();
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_EQ),
          is((long) 2));
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ),
          is((long) 0));
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_SIGN),
          is((long) 0));
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_COMP0),
          is((long) 2));

      logger.reset();
      loggedValues = logger.getLoggedValues();
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_EQ),
          is((long) 0));
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ),
          is((long) 0));
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_SIGN),
          is((long) 0));
      assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_COMP0),
          is((long) 0));
    }
  }
}