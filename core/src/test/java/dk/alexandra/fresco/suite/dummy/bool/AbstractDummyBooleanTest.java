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
import dk.alexandra.fresco.framework.util.DeterministicRandomBitGenerator;
import dk.alexandra.fresco.framework.util.HmacDeterministicRandomBitGeneratorImpl;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.PerformanceLogger.Flag;
import dk.alexandra.fresco.logging.SecureComputationEngineLoggingDecorator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractDummyBooleanTest {

  protected void runTest(
      TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy) throws Exception {
    runTest(f, evalStrategy, null);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy, EnumSet<Flag> performanceFlags) throws Exception {

    // The dummy protocol suite has the nice property that it can be run by just one player.
    int noOfParties = 1;
    runTest(f, evalStrategy, performanceFlags, noOfParties);

  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<ResourcePoolImpl, ProtocolBuilderBinary> f,
      EvaluationStrategy evalStrategy, EnumSet<Flag> performanceFlags, int noOfParties)
          throws Exception {

    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary>> conf =
        new HashMap<>();
    Map<Integer, List<PerformanceLogger>> pls = new HashMap<>();
    for (int playerId : netConf.keySet()) {
      pls.put(playerId, new ArrayList<>());
      NetworkConfiguration partyNetConf = netConf.get(playerId);

      DummyBooleanProtocolSuite ps = new DummyBooleanProtocolSuite();

      BatchEvaluationStrategy<ResourcePoolImpl> strat = evalStrategy.getStrategy();
      if (performanceFlags != null && performanceFlags.contains(Flag.LOG_NATIVE_BATCH)) {
        strat = new BatchEvaluationLoggingDecorator<>(strat);
        pls.get(playerId).add((PerformanceLogger) strat);
      }
      ProtocolEvaluator<ResourcePoolImpl, ProtocolBuilderBinary> evaluator =
          new BatchedProtocolEvaluator<>(strat, ps);

      SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);
      if (performanceFlags != null && performanceFlags.contains(Flag.LOG_RUNTIME)) {
        sce = new SecureComputationEngineLoggingDecorator<>(sce, ps);
        pls.get(playerId).add((PerformanceLogger) sce);
      }

      DeterministicRandomBitGenerator drbg = new HmacDeterministicRandomBitGeneratorImpl();
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderBinary> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> new ResourcePoolImpl(playerId, noOfParties, drbg), () -> {
            Network network;
            KryoNetNetwork kryoNetwork = new KryoNetNetwork(partyNetConf);
            if (performanceFlags != null && performanceFlags.contains(Flag.LOG_NETWORK)) {
              network = new NetworkLoggingDecorator(kryoNetwork);
              pls.get(playerId).add((PerformanceLogger) network);
            } else {
              network = kryoNetwork;
            }
            return network;
          });
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
    for (Integer id : pls.keySet()) {
      for (PerformanceLogger pl : pls.get(id)) {
        pl.printPerformanceLog(id);
        pl.reset();
      }
    }
  }
}
