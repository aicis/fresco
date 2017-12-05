package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.lib.bool.ComparisonBooleanTests;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanBuilderFactory;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestBinaryLoggingDecorators {

  @Test
  public void testBinaryComparisonLoggingDecorator() throws Exception {
    
    TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f
      = new ComparisonBooleanTests.TestGreaterThan<>();
    
    int noOfParties = 2;
    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 
    
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();

    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      DummyBooleanProtocolSuite ps = new DummyBooleanProtocolSuite();
      BatchEvaluationStrategy<ResourcePoolImpl> strat = evalStrategy.getStrategy();
      
      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new BatchedProtocolEvaluator<>(strat, ps);
      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> sce = new SecureComputationEngineImpl<>(
          ps, evaluator);
      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new ResourcePoolImpl(playerId, noOfParties, drbg),
              () -> {
                return new KryoNetNetwork(partyNetConf);
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
    
    for(Integer pId: netConf.keySet()) {
      List<PerformanceLogger> pl = DummyBooleanBuilderFactory.performanceLoggers.get(pId);

      ListLogger testLogger = new ListLogger();
      pl.get(1).printToLog(testLogger, pId);
      pl.get(1).reset();
      pl.get(1).printToLog(testLogger, pId);
    
      Assert.assertTrue(testLogger.getData().get(0).contains("=== Binary comparison operations logged - results ==="));
      Assert.assertTrue(testLogger.getData().get(1).contains("Greater than: 2"));
      Assert.assertTrue(testLogger.getData().get(2).contains("Equals: 0"));
      
      Assert.assertTrue(testLogger.getData().get(4).contains("Greater than: 0"));
      Assert.assertTrue(testLogger.getData().get(5).contains("Equals: 0"));
    }
  }
  
    
  
  @Test
  public void testBinaryLoggingDecorator() throws Exception {
    
    TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f
        = new ComparisonBooleanTests.TestGreaterThan<>();

    int noOfParties = 2;
    EvaluationStrategy evalStrategy = EvaluationStrategy.SEQUENTIAL; 

    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();

    for (int playerId : netConf.keySet()) {
      NetworkConfiguration partyNetConf = netConf.get(playerId);
      DummyBooleanProtocolSuite ps = new DummyBooleanProtocolSuite();
      BatchEvaluationStrategy<ResourcePoolImpl> strat = evalStrategy.getStrategy();

      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new BatchedProtocolEvaluator<>(strat, ps);
      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> sce = new SecureComputationEngineImpl<>(
          ps, evaluator);
      
      Drbg drbg = new HmacDrbg();
      TestThreadRunner.TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new ResourcePoolImpl(playerId, noOfParties, drbg),
              () -> {
                return new KryoNetNetwork(partyNetConf);
              });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);

    for(Integer pId: netConf.keySet()) {
      List<PerformanceLogger> pl = DummyBooleanBuilderFactory.performanceLoggers.get(pId);

      ListLogger testLogger = new ListLogger();
      pl.get(0).printToLog(testLogger, pId);
      pl.get(0).reset();
      pl.get(0).printToLog(testLogger, pId);

      Assert.assertTrue(testLogger.getData().get(0).contains("=== Basic binary operations logged - results ==="));
      Assert.assertTrue(testLogger.getData().get(1).contains("Xors: 26"));
      Assert.assertTrue(testLogger.getData().get(2).contains("Ands: 10"));
      Assert.assertTrue(testLogger.getData().get(3).contains("Random bits: 0"));

      Assert.assertTrue(testLogger.getData().get(5).contains("Xors: 0"));
      Assert.assertTrue(testLogger.getData().get(6).contains("Ands: 0"));
      Assert.assertTrue(testLogger.getData().get(7).contains("Random bits: 0"));
    }
  }
}