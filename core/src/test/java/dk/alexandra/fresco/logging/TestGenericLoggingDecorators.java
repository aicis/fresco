package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.sqrt.SqrtTests;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        = new TestSquareRootStartApplication<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 
    Map<Integer, NetworkConfiguration> netConf = getNetConf();
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    Map<Integer, ListLogger> pls = new HashMap<>();
    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
    for (int playerId : netConf.keySet()) {
      pls.put(playerId, new ListLogger());
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

    for (Integer pId : pls.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.printPerformanceLog(pId);
      performanceLogger.printToLog(pls.get(pId), pId);
      String expectedOutput = performanceLogger.makeLogString(pId);
      Assert.assertThat(pls.get(pId).getData().get(0), Is.is(expectedOutput));

      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      List<Long> runningTimes = (List<Long>)loggedValues.get(EvaluatorLoggingDecorator.SCE_RUNNINGTIMES);
      Assert.assertTrue(runningTimes.get(0) > 0);
    }
    for (Integer pId : pls.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.reset();
      performanceLogger.printToLog(pls.get(pId), pId);
      Assert.assertThat(pls.get(pId).getData().get(1), Is.is(performanceLogger.makeLogString(pId)));
      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      List<Long> runningTimes = (List<Long>)loggedValues.get(EvaluatorLoggingDecorator.SCE_RUNNINGTIMES);
      Assert.assertTrue(runningTimes.size() == 0);
    }
  }  

  @SuppressWarnings("unchecked")
  @Test
  public void testNetworkLoggingDecorator() throws Exception {
    
    TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric> f
        = new SqrtTests.TestSquareRoot<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 
    Map<Integer, NetworkConfiguration> netConf = getNetConf();
    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    Map<Integer, ListLogger> pls = new HashMap<>();
    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
    for (int playerId : netConf.keySet()) {
      pls.put(playerId, new ListLogger());
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

    for (Integer pId : pls.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.printToLog(pls.get(pId), pId);
      String expectedOutput = performanceLogger.makeLogString(pId);
      Assert.assertThat(pls.get(pId).getData().get(0), Is.is(expectedOutput));

      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), Is.is((long)396));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_TOTAL_BATCHES), Is.is(6));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_MAX_BYTES), Is.is(66));
      Assert.assertThat(loggedValues.get(NetworkLoggingDecorator.NETWORK_MIN_BYTES), Is.is(66));
      Assert.assertThat(((Map<Integer, Integer>)loggedValues.get(NetworkLoggingDecorator.NETWORK_PARTY_BYTES)).get(1), Is.is(396));
    }
    for (Integer pId : pls.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.reset();
      performanceLogger.printToLog(pls.get(pId), pId);
      Assert.assertThat(pls.get(pId).getData().get(1), Is.is(performanceLogger.makeLogString(pId)));
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
        = new SqrtTests.TestSquareRoot<>();

    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 

    Map<Integer, NetworkConfiguration> netConf = getNetConf();

    Map<Integer, TestThreadRunner.TestThreadConfiguration<DummyArithmeticResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    Map<Integer, ListLogger> pls = new HashMap<>();
    List<PerformanceLogger> decoratedLoggers = new ArrayList<>();
    for (int playerId : netConf.keySet()) {
      pls.put(playerId, new ListLogger());
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

    for (Integer pId : pls.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.printToLog(pls.get(pId), pId);
      String expectedOutput = performanceLogger.makeLogString(pId);
      Assert.assertThat(pls.get(pId).getData().get(0), Is.is(expectedOutput));

      Map<String, Object> loggedValues = performanceLogger.getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_COUNTER), Is.is(3496361));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_NATIVE_PROTOCOLS), Is.is(17275821));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MIN_PROTOCOLS), Is.is(1));
      Assert.assertThat(loggedValues.get(BatchEvaluationLoggingDecorator.BATCH_MAX_PROTOCOLS), Is.is(520));
    }
    for (Integer pId : pls.keySet()) {
      PerformanceLogger performanceLogger = decoratedLoggers.get(0);
      performanceLogger.reset();
      performanceLogger.printToLog(pls.get(pId), pId);
      Assert.assertThat(pls.get(pId).getData().get(1), Is.is(performanceLogger.makeLogString(pId)));
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
  
  
  public static class TestSquareRootStartApplication<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private final int maxBitLength = 32;
        private final BigInteger[] x = new BigInteger[] {BigInteger.valueOf(1234),
            BigInteger.valueOf(12345), BigInteger.valueOf(123456), BigInteger.valueOf(1234567),
            BigInteger.valueOf(12345678), BigInteger.valueOf(123456789)};
        private final int n = x.length;

        @Override
        protected <OutputT> OutputT runApplication(Application<OutputT, ProtocolBuilderNumeric> app) {
          try {
            return conf.sce.startApplication(app, conf.getResourcePool(), conf.getNetwork()).get();
          } catch (Exception e) {
            e.printStackTrace();
          }
          return null;
        }

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric numBuilder = builder.numeric();

            List<DRes<BigInteger>> results = new ArrayList<>(n);

            for (BigInteger input : x) {
              DRes<SInt> actualInput = numBuilder.input(input, 1);
              DRes<SInt> result = builder.advancedNumeric().sqrt(actualInput, maxBitLength);
              DRes<BigInteger> openResult = builder.numeric().open(result);
              results.add(openResult);
            }
            return () -> results.stream().map(DRes::out).collect(Collectors.toList());
          };

          List<BigInteger> results = runApplication(app);
          Assert.assertEquals(n, results.size());
          // We are not really interested in the result, only the running time
        }
      };
    }
  }

}