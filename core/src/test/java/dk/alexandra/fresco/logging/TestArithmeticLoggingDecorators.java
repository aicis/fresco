package dk.alexandra.fresco.logging;

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
import dk.alexandra.fresco.lib.math.integer.sqrt.SqrtTests;
import dk.alexandra.fresco.logging.arithmetic.ComparisonLoggerDecorator;
import dk.alexandra.fresco.logging.arithmetic.NumericLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticBuilderFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestArithmeticLoggingDecorators {

  @Test
  public void testNumericLoggingDecorator() throws Exception {
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
      = new SqrtTests.TestSquareRoot<>();
    
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

    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      
      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(mod, 200);
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
    
    for(Integer pId: netConf.keySet()) {
      List<PerformanceLogger> pl = DummyArithmeticBuilderFactory.performanceLoggers.get(pId);
      
      ListLogger testLogger = new ListLogger();
      pl.get(0).printToLog(testLogger, pId);
      Map<String, Object> loggedValues = pl.get(0).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_ADD), Is.is((long)5719386));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_MULT), Is.is((long)15996));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_SUB), Is.is((long)46416));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_BIT), Is.is((long)5669220));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_RAND), Is.is((long)960));
      
      pl.get(0).reset();
      pl.get(0).printToLog(testLogger, pId);
      loggedValues = pl.get(0).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_ADD), Is.is((long)0));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_MULT), Is.is((long)0));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_SUB), Is.is((long)0));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_BIT), Is.is((long)0));
      Assert.assertThat(loggedValues.get(NumericLoggingDecorator.ARITHMETIC_BASIC_RAND), Is.is((long)0));
      
      Assert.assertTrue(testLogger.getData().get(0).contains("Basic numeric operations logged - results ==="));
      Assert.assertTrue(testLogger.getData().get(1).contains("Multiplications: 15996"));
      Assert.assertTrue(testLogger.getData().get(2).contains("Additions: 5719386"));
      Assert.assertTrue(testLogger.getData().get(3).contains("Subtractions: 46416"));
      Assert.assertTrue(testLogger.getData().get(4).contains("Random bits fetched: 5669220"));
      Assert.assertTrue(testLogger.getData().get(5).contains("Random elements fetched: 960"));
      
      Assert.assertTrue(testLogger.getData().get(7).contains("Multiplications: 0"));
      Assert.assertTrue(testLogger.getData().get(8).contains("Additions: 0"));
      Assert.assertTrue(testLogger.getData().get(9).contains("Subtractions: 0"));
      Assert.assertTrue(testLogger.getData().get(10).contains("Random bits fetched: 0"));
      Assert.assertTrue(testLogger.getData().get(11).contains("Random elements fetched: 0"));
    }
  }
  
  @Test
  public void testComparisonLoggingDecorator() throws Exception {
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
      = new SqrtTests.TestSquareRoot<>();
    
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

    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      
      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(mod, 200);
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
    
    for(Integer pId: netConf.keySet()) {
      List<PerformanceLogger> pl = DummyArithmeticBuilderFactory.performanceLoggers.get(pId);
    
      ListLogger testLogger = new ListLogger();

      pl.get(1).printToLog(testLogger, pId);
      Map<String, Object> loggedValues = pl.get(1).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_EQ), Is.is((long)0));
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), Is.is((long)0));
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_SIGN), Is.is((long)60));
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_COMP0), Is.is((long)480));

      pl.get(1).reset();
      pl.get(1).printToLog(testLogger, pId);
      loggedValues = pl.get(1).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_EQ), Is.is((long)0));
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), Is.is((long)0));
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_SIGN), Is.is((long)0));
      Assert.assertThat(loggedValues.get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_COMP0), Is.is((long)0));
      
      Assert.assertTrue(testLogger.getData().get(0).contains("Comparison operations logged - results ==="));
      Assert.assertTrue(testLogger.getData().get(1).contains("EQ: 0"));
      Assert.assertTrue(testLogger.getData().get(2).contains("LEQ: 0"));
      Assert.assertTrue(testLogger.getData().get(3).contains("Compute sign: 60"));
      Assert.assertTrue(testLogger.getData().get(4).contains("Compare to 0: 480"));
      
      Assert.assertTrue(testLogger.getData().get(6).contains("EQ: 0"));
      Assert.assertTrue(testLogger.getData().get(7).contains("LEQ: 0"));
      Assert.assertTrue(testLogger.getData().get(8).contains("Compute sign: 0"));
      Assert.assertTrue(testLogger.getData().get(9).contains("Compare to 0: 0"));
    }
  }
}