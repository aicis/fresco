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
import dk.alexandra.fresco.logging.binary.BinaryComparisonLoggingDecorator;
import dk.alexandra.fresco.logging.binary.BinaryLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanBuilderFactory;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.core.Is;
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

      Map<String, Object> loggedValues = pl.get(1).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(BinaryComparisonLoggingDecorator.BINARY_COMPARISON_GT), Is.is(2));
      Assert.assertThat(loggedValues.get(BinaryComparisonLoggingDecorator.BINARY_COMPARISON_EQ), Is.is(0));

      pl.get(1).reset();
      loggedValues = pl.get(1).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(BinaryComparisonLoggingDecorator.BINARY_COMPARISON_GT), Is.is(0));
      Assert.assertThat(loggedValues.get(BinaryComparisonLoggingDecorator.BINARY_COMPARISON_EQ), Is.is(0));
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

      Map<String, Object> loggedValues = pl.get(0).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(BinaryLoggingDecorator.BINARY_BASIC_XOR), Is.is(26));
      Assert.assertThat(loggedValues.get(BinaryLoggingDecorator.BINARY_BASIC_AND), Is.is(10));
      Assert.assertThat(loggedValues.get(BinaryLoggingDecorator.BINARY_BASIC_RANDOM), Is.is(0));
      
      pl.get(0).reset();
      loggedValues = pl.get(0).getLoggedValues(pId);
      Assert.assertThat(loggedValues.get(BinaryLoggingDecorator.BINARY_BASIC_XOR), Is.is(0));
      Assert.assertThat(loggedValues.get(BinaryLoggingDecorator.BINARY_BASIC_AND), Is.is(0));
      Assert.assertThat(loggedValues.get(BinaryLoggingDecorator.BINARY_BASIC_RANDOM), Is.is(0));
    }
  }
}