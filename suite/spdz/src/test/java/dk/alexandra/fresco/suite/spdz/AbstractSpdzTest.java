package dk.alexandra.fresco.suite.spdz;

import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.DUMMY;
import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.MASCOT;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.DefaultPerformancePrinter;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.NumericSuiteLogging;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.logging.PerformanceLoggerCountingAggregate;
import dk.alexandra.fresco.logging.PerformancePrinter;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzMascotDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageConstants;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractSpdzTest {

  protected Map<Integer, PerformanceLogger> performanceLoggers = new HashMap<>();

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, PreprocessingStrategy preProStrat, int noOfParties) {
    runTest(f, evalStrategy, preProStrat, noOfParties, false);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, PreprocessingStrategy preProStrat, int noOfParties,
      boolean logPerformance) {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }

    int maxBitLength;
    if (preProStrat == MASCOT) {
      maxBitLength = 16;
    } else {
      maxBitLength = 150;
    }

    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      PerformanceLoggerCountingAggregate aggregate = new PerformanceLoggerCountingAggregate();

      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite = new SpdzProtocolSuite(maxBitLength);
      if (logPerformance) {
        protocolSuite = new NumericSuiteLogging<>(protocolSuite);
        aggregate.add((PerformanceLogger) protocolSuite);
      }
      BatchEvaluationStrategy<SpdzResourcePool> batchEvalStrat = evalStrategy.getStrategy();

      if (logPerformance) {
        batchEvalStrat = new BatchEvaluationLoggingDecorator<>(batchEvalStrat);
        aggregate.add((PerformanceLogger) batchEvalStrat);
      }
      ProtocolEvaluator<SpdzResourcePool> evaluator =
          new BatchedProtocolEvaluator<>(batchEvalStrat, protocolSuite);
      if (logPerformance) {
        evaluator = new EvaluatorLoggingDecorator<>(evaluator);
        aggregate.add((PerformanceLogger) evaluator);
      }

      SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(protocolSuite, evaluator);

      TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce,
              () -> createResourcePool(playerId, noOfParties, preProStrat, ports), () -> {
            KryoNetNetwork kryoNetwork = new KryoNetNetwork(netConf.get(playerId));
            if (logPerformance) {
              NetworkLoggingDecorator network = new NetworkLoggingDecorator(kryoNetwork);
              aggregate.add(network);
              return network;
            } else {
              return kryoNetwork;
            }
          });
      conf.put(playerId, ttc);
      performanceLoggers.putIfAbsent(playerId, aggregate);
    }
    TestThreadRunner.run(f, conf);
    PerformancePrinter printer = new DefaultPerformancePrinter();
    for (PerformanceLogger pl : performanceLoggers.values()) {
      printer.printPerformanceLog(pl);
    }
  }

  DRes<List<DRes<SInt>>> createPipe(
      int myId, List<Integer> ports, int pipeLength,
      KryoNetNetwork pipeNetwork, SpdzMascotDataSupplier trippleSupplier) {

    ProtocolBuilderNumeric sequential = new SpdzBuilder(
        new BasicNumericContext(128, trippleSupplier.getModulus(), myId, ports.size()))
        .createSequential();
    SpdzResourcePoolImpl tripleResourcePool =
        new SpdzResourcePoolImpl(myId, ports.size(), null, new SpdzStorageImpl(trippleSupplier));

    DRes<List<DRes<SInt>>> exponentiationPipe =
        new DefaultPreprocessedValues(sequential).getExponentiationPipe(pipeLength);
    evaluate(sequential, tripleResourcePool, pipeNetwork);
    return exponentiationPipe;
  }

  private SpdzResourcePool createResourcePool(int myId, int numberOfParties,
      PreprocessingStrategy preproStrat, List<Integer> ports) {
    SpdzDataSupplier supplier;
    if (preproStrat == DUMMY) {
      supplier = new SpdzDummyDataSupplier(myId, numberOfParties);
    } else if (preproStrat == MASCOT) {
      supplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, numberOfParties,
          () -> createExtraNetwork(myId, ports, 457),
          new Function<Integer, SpdzSInt[]>() {

            private SpdzMascotDataSupplier trippleSupplier;
            private KryoNetNetwork pipeNetwork;

            @Override
            public SpdzSInt[] apply(Integer pipeLength) {
              if (pipeNetwork == null) {
                pipeNetwork = createExtraNetwork(myId, ports, 667);
                trippleSupplier = SpdzMascotDataSupplier
                    .createSimpleSupplier(myId, ports.size(), () -> pipeNetwork, null);
              }
              DRes<List<DRes<SInt>>> pipe = createPipe(myId, ports, pipeLength, pipeNetwork,
                  trippleSupplier);
              return computeSInts(pipe);
            }
          });
    } else {
      // case STATIC:
      int noOfThreadsUsed = 1;
      String storageName =
          SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_" + myId + "_" + 0 + "_";
      supplier = new SpdzStorageDataSupplier(
          new FilebasedStreamedStorageImpl(new InMemoryStorage()), storageName, numberOfParties);
    }
    SpdzStorage store = new SpdzStorageImpl(supplier);
    return new SpdzResourcePoolImpl(myId, numberOfParties, new HmacDrbg(), store);
  }

  private SpdzSInt[] computeSInts(DRes<List<DRes<SInt>>> pipe) {
    List<DRes<SInt>> out = pipe.out();
    SpdzSInt[] result = new SpdzSInt[out.size()];
    for (int i = 0; i < out.size(); i++) {
      DRes<SInt> sIntResult = out.get(i);
      result[i] = (SpdzSInt) sIntResult.out();
    }
    return result;
  }

  private KryoNetNetwork createExtraNetwork(int myId, List<Integer> ports, int portOffset) {
    return new KryoNetNetwork(new MascotNetworkConfiguration(myId, ports, portOffset));
  }

  private void evaluate(ProtocolBuilderNumeric spdzBuilder, SpdzResourcePool tripleResourcePool,
      Network network) {
    BatchedStrategy<SpdzResourcePool> batchedStrategy = new BatchedStrategy<>();
    SpdzProtocolSuite spdzProtocolSuite = new SpdzProtocolSuite(128);
    BatchedProtocolEvaluator<SpdzResourcePool> batchedProtocolEvaluator =
        new BatchedProtocolEvaluator<>(batchedStrategy, spdzProtocolSuite);
    batchedProtocolEvaluator.eval(spdzBuilder.build(), tripleResourcePool, network);
  }

  private class MascotNetworkConfiguration implements NetworkConfiguration {

    private final int myId;
    private final List<Integer> usedPorts;
    private final int portOffset;

    private MascotNetworkConfiguration(int myId, List<Integer> usedPorts, int portOffset) {
      this.myId = myId;
      this.usedPorts = usedPorts;
      this.portOffset = portOffset;
    }

    @Override
    public Party getParty(int id) {
      return new Party(id, "localhost", usedPorts.get(id - 1) + portOffset);
    }

    @Override
    public Party getMe() {
      return getParty(myId);
    }

    @Override
    public int getMyId() {
      return myId;
    }

    @Override
    public int noOfParties() {
      return usedPorts.size();
    }
  }
}
