package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.BinarySuiteLogging;
import dk.alexandra.fresco.logging.DefaultPerformancePrinter;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.PerformanceLoggerCountingAggregate;
import dk.alexandra.fresco.logging.PerformancePrinter;
import dk.alexandra.fresco.suite.ProtocolSuiteBinary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractDummyBooleanTest {

  protected Map<Integer, List<PerformanceLogger>> performanceLoggers = new HashMap<>();
  
  protected void runTest(
      TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy) throws Exception {
    runTest(f, evalStrategy, false);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy, boolean logPerformance) throws Exception {

    // The dummy protocol suite has the nice property that it can be run by just one player.
    int noOfParties = 1;
    runTest(f, evalStrategy, logPerformance, noOfParties);

  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy, boolean logPerformance, int noOfParties)
          throws Exception {

    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      PerformanceLoggerCountingAggregate aggregate 
        = new PerformanceLoggerCountingAggregate();
      
      NetworkConfiguration partyNetConf = netConf.get(playerId);

      ProtocolSuiteBinary<ResourcePoolImpl> ps = new DummyBooleanProtocolSuite();
      if(logPerformance) {
        BinarySuiteLogging<ResourcePoolImpl> decoratedSuite =
            new BinarySuiteLogging<>(new DummyBooleanProtocolSuite());
        aggregate.add(decoratedSuite);
        ps = decoratedSuite;
      }
      
      BatchEvaluationStrategy<ResourcePoolImpl> strat = evalStrategy.getStrategy();
      if (logPerformance) {
        strat = new BatchEvaluationLoggingDecorator<>(strat);
        aggregate.add((PerformanceLogger) strat);
      }
      
      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new BatchedProtocolEvaluator<>(strat, ps);
      if (logPerformance) {
        evaluator = new EvaluatorLoggingDecorator<>(evaluator);
        aggregate.add((PerformanceLogger) evaluator);
      }
      
      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);

       

      Drbg drbg = new HmacDrbg();
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new ResourcePoolImpl(playerId, noOfParties, drbg), () -> {
            Network network;
            KryoNetNetwork kryoNetwork = new KryoNetNetwork(partyNetConf);
            if (logPerformance) {
              network = new NetworkLoggingDecorator(kryoNetwork);
              aggregate.add((PerformanceLogger) network);
            } else {
              network = kryoNetwork;
            }
            return network;
          });
      conf.put(playerId, ttc);
      performanceLoggers.putIfAbsent(playerId, new ArrayList<>());
      performanceLoggers.get(playerId).add(aggregate);
    }
    TestThreadRunner.run(f, conf);
    for (Integer id : conf.keySet()) {
      PerformancePrinter printer = new DefaultPerformancePrinter();
      for (PerformanceLogger pl : performanceLoggers.get(id)) {
        printer.printPerformanceLog(pl);
      }
    }
  }
}
