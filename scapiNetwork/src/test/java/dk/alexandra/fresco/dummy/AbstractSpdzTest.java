package dk.alexandra.fresco.dummy;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.PerformanceLogger.Flag;
import dk.alexandra.fresco.logging.SecureComputationEngineLoggingDecorator;
import dk.alexandra.fresco.network.ScapiNetworkImpl;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.DummyDataSupplierImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractSpdzTest {

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, int noOfParties, EnumSet<Flag> performanceLoggerFlags)
      throws Exception {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    Map<Integer, List<PerformanceLogger>> pls = new HashMap<>();
    for (int playerId : netConf.keySet()) {
      pls.put(playerId, new ArrayList<>());
      SpdzProtocolSuite protocolSuite = new SpdzProtocolSuite(150);

      BatchEvaluationStrategy<SpdzResourcePool> batchStrat = evalStrategy.getStrategy();
      if (performanceLoggerFlags != null && performanceLoggerFlags
          .contains(Flag.LOG_NATIVE_BATCH)) {
        batchStrat = new BatchEvaluationLoggingDecorator<>(batchStrat);
        pls.get(playerId).add((PerformanceLogger) batchStrat);
      }
      ProtocolEvaluator<SpdzResourcePool, ProtocolBuilderNumeric> evaluator =
          new BatchedProtocolEvaluator<>(batchStrat, protocolSuite);

      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(protocolSuite, evaluator);
      if (performanceLoggerFlags != null && performanceLoggerFlags.contains(Flag.LOG_RUNTIME)) {
        sce = new SecureComputationEngineLoggingDecorator<>(sce, protocolSuite);
        pls.get(playerId).add((PerformanceLogger) sce);
      }

      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(
              sce,
              () -> createResourcePool(playerId, noOfParties),
              () -> {
                ScapiNetworkImpl scapiNetwork = new ScapiNetworkImpl();
                scapiNetwork.init(netConf.get(playerId), 1);
                scapiNetwork.connect(10000);
                if (performanceLoggerFlags != null && performanceLoggerFlags
                    .contains(Flag.LOG_NETWORK)) {
                  NetworkLoggingDecorator network = new NetworkLoggingDecorator(scapiNetwork);
                  pls.get(playerId).add(network);
                  return network;
                } else {
                  return scapiNetwork;
                }
              });

      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
    for (Integer pId : pls.keySet()) {
      for (PerformanceLogger pl : pls.get(pId)) {
        pl.printPerformanceLog(pId);
      }
    }
  }

  private SpdzResourcePool createResourcePool(int myId, int size) {
    SpdzStorageImpl store = new SpdzStorageImpl(new DummyDataSupplierImpl(myId, size));
    try {
      return new SpdzResourcePoolImpl(myId, size, new HmacDrbg(), store);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Your system does not support the necessary hash function.", e);
    }
  }
}
