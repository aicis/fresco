package dk.alexandra.fresco.suite.spdz;

import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.DUMMY;
import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.MASCOT;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkTestUtils;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
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
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDataSupplier;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public abstract class AbstractSpdzTest {

  private Map<Integer, PerformanceLogger> performanceLoggers = new HashMap<>();
  // TODO hack hack hack
  private static final int DEFAULT_MOD_BIT_LENGTH = 128;
  private static final int DEFAULT_MAX_BIT_LENGTH = 64;
  private static final int DEFAULT_FIXED_POINT_PRECISION = 16;
  private int modBitLength = DEFAULT_MOD_BIT_LENGTH;
  private int maxBitLength = DEFAULT_MAX_BIT_LENGTH;
  private int fixedPointPrecision = DEFAULT_FIXED_POINT_PRECISION;
  private static final int PRG_SEED_LENGTH = 256;
  private static EvaluationStrategy DEFAULT_EVAL_STRATEGY = EvaluationStrategy.SEQUENTIAL_BATCHED;

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      PreprocessingStrategy preProStrat,
      int noOfParties) {
    runTest(f, DEFAULT_EVAL_STRATEGY, preProStrat, noOfParties,
        false, DEFAULT_MOD_BIT_LENGTH, DEFAULT_MAX_BIT_LENGTH, DEFAULT_FIXED_POINT_PRECISION);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      PreprocessingStrategy preProStrat, int noOfParties, int modBitLength, int maxBitLength,
      int fixedPointPrecision) {
    runTest(f, DEFAULT_EVAL_STRATEGY, preProStrat, noOfParties, modBitLength,
        maxBitLength, fixedPointPrecision);
  }

  private void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, PreprocessingStrategy preProStrat, int noOfParties,
      boolean logPerformance, int modBitLength, int maxBitLength, int fixedPointPrecision) {
    this.modBitLength = modBitLength;
    this.maxBitLength = maxBitLength;
    this.fixedPointPrecision = fixedPointPrecision;

    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }
    NetManager tripleManager = new NetManager(ports);
    NetManager otManager = new NetManager(ports);
    NetManager expPipeManager = new NetManager(ports);

    Map<Integer, NetworkConfiguration> netConf =
        NetworkTestUtils.getNetworkConfigurations(noOfParties, ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<SpdzResourcePool, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int playerId : netConf.keySet()) {
      PerformanceLoggerCountingAggregate aggregate = new PerformanceLoggerCountingAggregate();

      ProtocolSuiteNumeric<SpdzResourcePool> protocolSuite = createProtocolSuite(maxBitLength);
      BatchEvaluationStrategy<SpdzResourcePool> batchEvalStrat = evalStrategy.getStrategy();
      if (logPerformance) {
        protocolSuite = new NumericSuiteLogging<>(protocolSuite);
        aggregate.add((PerformanceLogger) protocolSuite);
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
          new TestThreadRunner.TestThreadConfiguration<>(sce, () -> createResourcePool(playerId,
              noOfParties, preProStrat, otManager, tripleManager, expPipeManager), () -> {
            Network network = new AsyncNetwork(netConf.get(playerId));
            if (logPerformance) {
              network = new NetworkLoggingDecorator(network);
              aggregate.add((NetworkLoggingDecorator) network);
              return network;
            } else {
              return network;
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
    tripleManager.close();
    expPipeManager.close();
  }

  protected SpdzProtocolSuite createProtocolSuite(int maxBitLength) {
    return new SpdzProtocolSuite(maxBitLength);
  }

  protected void runTest(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, PreprocessingStrategy preProStrat, int noOfParties,
      int modBitLength, int maxBitLength, int fixedPointPrecision) {
    runTest(f, evalStrategy, preProStrat, noOfParties, false, modBitLength, maxBitLength,
        fixedPointPrecision);
  }

  // this is here until seq strategy goes away
  void runTestSequential(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f) {
    runTest(f, EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2,
        false, DEFAULT_MOD_BIT_LENGTH, DEFAULT_MAX_BIT_LENGTH, DEFAULT_FIXED_POINT_PRECISION);
  }

  void runTestWithLogging(
      TestThreadRunner.TestThreadFactory<SpdzResourcePool, ProtocolBuilderNumeric> f) {
    runTest(f, DEFAULT_EVAL_STRATEGY, PreprocessingStrategy.DUMMY, 2,
        true, DEFAULT_MOD_BIT_LENGTH, DEFAULT_MAX_BIT_LENGTH, DEFAULT_FIXED_POINT_PRECISION);
  }

  private DRes<List<DRes<SInt>>> createPipe(int myId, int noOfPlayers, int pipeLength,
      CloseableNetwork pipeNetwork, SpdzMascotDataSupplier tripleSupplier) {

    ProtocolBuilderNumeric sequential = new SpdzBuilder(
        new BasicNumericContext(maxBitLength, tripleSupplier.getModulus(), myId, noOfPlayers
        ),
        new RealNumericContext(fixedPointPrecision)).createSequential();
    SpdzResourcePoolImpl tripleResourcePool =
        new SpdzResourcePoolImpl(myId, noOfPlayers, new OpenedValueStoreImpl<>(), tripleSupplier,
            new AesCtrDrbg(new byte[32]));

    DRes<List<DRes<SInt>>> exponentiationPipe =
        new DefaultPreprocessedValues(sequential).getExponentiationPipe(pipeLength);
    evaluate(sequential, tripleResourcePool, pipeNetwork);
    return exponentiationPipe;
  }

  private Drbg getDrbg(int myId, int prgSeedLength) {
    byte[] seed = new byte[prgSeedLength / 8];
    new Random(myId).nextBytes(seed);
    return AesCtrDrbgFactory.fromDerivedSeed(seed);
  }

  private Map<Integer, RotList> getSeedOts(int myId, List<Integer> partyIds, int prgSeedLength,
      Drbg drbg, Network network) {
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (Integer otherId : partyIds) {
      if (myId != otherId) {
        Ot ot = new DummyOt(otherId, network);
        RotList currentSeedOts = new RotList(drbg, prgSeedLength);
        if (myId < otherId) {
          currentSeedOts.send(ot);
          currentSeedOts.receive(ot);
        } else {
          currentSeedOts.receive(ot);
          currentSeedOts.send(ot);
        }
        seedOts.put(otherId, currentSeedOts);
      }
    }
    return seedOts;
  }

  private SpdzResourcePool createResourcePool(int myId,
      int numberOfParties,
      PreprocessingStrategy preProStrat,
      NetManager otGenerator,
      NetManager tripleGenerator,
      NetManager expPipeGenerator) {
    SpdzDataSupplier supplier;
    if (preProStrat == DUMMY) {
      supplier = new SpdzDummyDataSupplier(myId, numberOfParties,
          ModulusFinder.findSuitableModulus(modBitLength));
    } else if (preProStrat == MASCOT) {
      List<Integer> partyIds =
          IntStream.range(1, numberOfParties + 1).boxed().collect(Collectors.toList());
      Drbg drbg = getDrbg(myId, PRG_SEED_LENGTH);
      BigInteger modulus = ModulusFinder.findSuitableModulus(modBitLength);
      Map<Integer, RotList> seedOts =
          getSeedOts(myId, partyIds, PRG_SEED_LENGTH, drbg, otGenerator.createExtraNetwork(myId));
      FieldElement ssk = SpdzMascotDataSupplier.createRandomSsk(modulus, PRG_SEED_LENGTH);
      supplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, numberOfParties,
          () -> tripleGenerator.createExtraNetwork(myId), modBitLength, modulus,
          new Function<Integer, SpdzSInt[]>() {

            private SpdzMascotDataSupplier tripleSupplier;
            private CloseableNetwork pipeNetwork;

            @Override
            public SpdzSInt[] apply(Integer pipeLength) {
              if (pipeNetwork == null) {
                pipeNetwork = expPipeGenerator.createExtraNetwork(myId);
                tripleSupplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, numberOfParties,
                    () -> pipeNetwork, modBitLength, modulus, null, seedOts, drbg, ssk);
              }
              DRes<List<DRes<SInt>>> pipe =
                  createPipe(myId, numberOfParties, pipeLength, pipeNetwork, tripleSupplier);
              return computeSInts(pipe);
            }
          }, seedOts, drbg, ssk);
    } else {
      // case STATIC:
      int noOfThreadsUsed = 1;
      String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_"
          + myId + "_" + 0 + "_";
      FilebasedStreamedStorageImpl storage =
          new FilebasedStreamedStorageImpl(new InMemoryStorage());
      supplier = new SpdzStorageDataSupplier(storage, storageName, numberOfParties);
    }
    return new SpdzResourcePoolImpl(myId, numberOfParties, new OpenedValueStoreImpl<>(), supplier,
        new AesCtrDrbg(new byte[32]));
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

  private void evaluate(ProtocolBuilderNumeric spdzBuilder, SpdzResourcePool tripleResourcePool,
      Network network) {
    BatchedStrategy<SpdzResourcePool> batchedStrategy = new BatchedStrategy<>();
    SpdzProtocolSuite spdzProtocolSuite = createProtocolSuite(maxBitLength);
    BatchedProtocolEvaluator<SpdzResourcePool> batchedProtocolEvaluator =
        new BatchedProtocolEvaluator<>(batchedStrategy, spdzProtocolSuite);
    batchedProtocolEvaluator.eval(spdzBuilder.build(), tripleResourcePool, network);
  }

}
