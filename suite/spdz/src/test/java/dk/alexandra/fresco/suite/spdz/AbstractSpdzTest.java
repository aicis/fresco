package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.util.DetermSecureRandom;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.PerformanceLogger.Flag;
import dk.alexandra.fresco.logging.SCELoggingDecorator;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplierImpl;
import dk.alexandra.fresco.suite.spdz.storage.DummyDataSupplierImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageConstants;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractSpdzTest {

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, NetworkingStrategy network,
      PreprocessingStrategy preProStrat, int noOfParties) throws Exception {
    runTest(f, evalStrategy, network, preProStrat, noOfParties, null);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, NetworkingStrategy networkStrategy,
      PreprocessingStrategy preProStrat, int noOfParties, EnumSet<Flag> performanceloggerFlags)
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

      BatchEvaluationStrategy<SpdzResourcePool> batchEvalStrat = EvaluationStrategy.fromEnum(evalStrategy);
      if(performanceloggerFlags != null && performanceloggerFlags.contains(Flag.LOG_NATIVE_BATCH)) {
        batchEvalStrat = new BatchEvaluationLoggingDecorator<>(batchEvalStrat);
        pls.get(playerId).add((PerformanceLogger) batchEvalStrat);
      }
      ProtocolEvaluator<SpdzResourcePool, ProtocolBuilderNumeric> evaluator =
          new BatchedProtocolEvaluator<>(batchEvalStrat);
      
      Network network = new KryoNetNetwork();
      network.init(netConf.get(playerId), 1);
      if(performanceloggerFlags != null && performanceloggerFlags.contains(Flag.LOG_NETWORK)) {
        network = new NetworkLoggingDecorator(network);
        pls.get(playerId).add((PerformanceLogger) network);
      }          
      SpdzResourcePool rp = createResourcePool(playerId, noOfParties, network, new Random(),
          new DetermSecureRandom(), preProStrat);
      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce = 
          new SecureComputationEngineImpl<>(protocolSuite, evaluator);
      if(performanceloggerFlags != null && performanceloggerFlags.contains(Flag.LOG_RUNTIME)) {
        sce = new SCELoggingDecorator<>(sce, protocolSuite);
        pls.get(playerId).add((PerformanceLogger) sce);
      }
      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>(
              sce,
              rp);
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
    for(Integer pId : pls.keySet()) {
      for(PerformanceLogger pl : pls.get(pId)) {
        pl.printPerformanceLog(pId);
      }
    }
  }

  private SpdzResourcePool createResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand, PreprocessingStrategy preproStrat) throws NoSuchAlgorithmException {
    DataSupplier supplier;
    switch (preproStrat) {
      case DUMMY:
        supplier = new DummyDataSupplierImpl(myId, size);
        break;
      case STATIC:
        int noOfThreadsUsed = 1;        
        String storageName =
            SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_" + myId + "_" + 0
            + "_";
        supplier = new DataSupplierImpl(new FilebasedStreamedStorageImpl(new InMemoryStorage()), storageName, size);        
        break;
      default:
        throw new ConfigurationException("Unkonwn preprocessing strategy: " + preproStrat);
    }
    SpdzStorage store = new SpdzStorageImpl(supplier);
    return new SpdzResourcePoolImpl(myId, size, network, rand, secRand, store);
  }
}
