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
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.math.integer.sqrt.SqrtTests;
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

public class TestGenericLoggingDecorators {

  private final BigInteger mod 
    = new BigInteger("6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");

  @Test
  public void testPerformanceLoggerEnums(){
    Assert.assertThat(PerformanceLogger.Flag.valueOf("LOG_EVALUATOR"), Is.is(PerformanceLogger.Flag.LOG_EVALUATOR));
    Assert.assertThat(PerformanceLogger.Flag.valueOf("LOG_NETWORK"), Is.is(PerformanceLogger.Flag.LOG_NETWORK));
    Assert.assertThat(PerformanceLogger.Flag.valueOf("LOG_NATIVE_BATCH"), Is.is(PerformanceLogger.Flag.LOG_NATIVE_BATCH));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testEvaluatorLoggingDecorator() throws Exception {
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new CompareTests.TestCompareEQ<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 
    Map<Integer, NetworkConfiguration> netConf = getNetConf();
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      
      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(mod, 200);
      BatchEvaluationStrategy<DummyArithmeticResourcePool> strat = evalStrategy.getStrategy();
      ProtocolEvaluator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> evaluator 
          = new BatchedProtocolEvaluator<>(strat, ps);
      EvaluatorLoggingDecorator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> decoratedEvaluator
          = new EvaluatorLoggingDecorator<>(evaluator);
      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce 
          = new SecureComputationEngineImpl<>(ps, decoratedEvaluator);
      decoratedLoggers.add(decoratedEvaluator);
      
      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new DummyArithmeticResourcePoolImpl(playerId,
                  netConf.keySet().size(), drbg, mod),
              () -> {
                return new KryoNetNetwork(partyNetConf);
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    for (Integer pId : netConf.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);

      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      List<Long> runningTimes = (List<Long>)loggedValues.get(EvaluatorLoggingDecorator.SCE_RUNNINGTIMES);
      Assert.assertTrue(runningTimes.get(0) > 0);
    }
    for (Integer pId : netConf.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.reset();
      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      List<Long> runningTimes = (List<Long>)loggedValues.get(EvaluatorLoggingDecorator.SCE_RUNNINGTIMES);
      Assert.assertTrue(runningTimes.size() == 0);
    }
  }  

  @SuppressWarnings("unchecked")
  @Test
  public void testNetworkLoggingDecorator() throws Exception {
    
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new CompareTests.TestCompareLT<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 
    Map<Integer, NetworkConfiguration> netConf = getNetConf();
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
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
                  netConf.keySet().size(), drbg, mod),
              () -> {
                NetworkLoggingDecorator network = new NetworkLoggingDecorator(new KryoNetNetwork(partyNetConf));
                decoratedLoggers.add(network);
                return network;
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    for (Integer pId : netConf.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);

      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), Is.is((long)132));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BATCHES), Is.is(2));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_MAX_BYTES), Is.is(66));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_MIN_BYTES), Is.is(66));
      Assert.assertThat(((Map<Integer, Integer>)loggedValues.get(NetworkLoggingDecorator.NETWORK_PARTY_BYTES)).get(1), Is.is(132));
    }
    for (Integer pId : netConf.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.reset();

      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), Is.is((long)0));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BATCHES), Is.is(0));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_MAX_BYTES), Is.is(0));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_MIN_BYTES), Is.is(Integer.MAX_VALUE));
      Assert.assertTrue(((Map<Integer, Integer>)loggedValues.get(NetworkLoggingDecorator.NETWORK_PARTY_BYTES)).isEmpty());
    }
  }
  
  @Test
  public void testBatchEvaluationLoggingDecorator() throws Exception {
    
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new BasicArithmeticTests.TestSumAndMult<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 

    Map<Integer, NetworkConfiguration> netConf = getNetConf();

    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      
      DummyArithmeticProtocolSuite ps = new DummyArithmeticProtocolSuite(mod, 200);
      BatchEvaluationStrategy<DummyArithmeticResourcePool> strat = evalStrategy.getStrategy();
      BatchEvaluationLoggingDecorator<DummyArithmeticResourcePool> decoratedStrat =
          new BatchEvaluationLoggingDecorator<>(strat);
      decoratedLoggers.add(decoratedStrat);
      
      ProtocolEvaluator<DummyArithmeticResourcePool, ProtocolBuilderNumeric> evaluator 
          = new BatchedProtocolEvaluator<>(decoratedStrat, ps);
      SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce 
          = new SecureComputationEngineImpl<>(ps, evaluator);
      
      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new DummyArithmeticResourcePoolImpl(playerId,
                  netConf.keySet().size(), drbg, mod),
              () -> {
                return new KryoNetNetwork(partyNetConf);
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    for (Integer pId : netConf.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_COUNTER), Is.is(8));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_NATIVE_PROTOCOLS), Is.is(43));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MIN_PROTOCOLS), Is.is(1));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MAX_PROTOCOLS), Is.is(21));
    }
    for (Integer pId : netConf.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.reset();
      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_COUNTER), Is.is(0));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_NATIVE_PROTOCOLS), Is.is(0));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MIN_PROTOCOLS), Is.is(Integer.MAX_VALUE));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MAX_PROTOCOLS), Is.is(0));
    }
  }
  
  private Map<Integer, NetworkConfiguration> getNetConf() {
    int noOfParties = 2;
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }
    return TestConfiguration.getNetworkConfigurations(noOfParties, ports);
  }
}