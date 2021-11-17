package dk.alexandra.fresco.suite.crt;

import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.DUMMY;
import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.MASCOT;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.field.MersennePrimeFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkUtil;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
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
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.logging.PerformanceLogger;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTDataSupplier;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTDummyDataSupplier;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePoolImpl;
import dk.alexandra.fresco.suite.crt.suites.SpdzProtocolSupplier;
import dk.alexandra.fresco.suite.spdz.NetManager;
import dk.alexandra.fresco.suite.spdz.SpdzBuilder;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzMascotDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDataSupplier;
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
public class AbstractSpdzCRTTest {

  protected static final FieldDefinition DEFAULT_FIELD_LEFT =
      MersennePrimeFieldDefinition.find(64);
  protected static final FieldDefinition DEFAULT_FIELD_RIGHT =
      MersennePrimeFieldDefinition.find(128);
  private static final int PRG_SEED_LENGTH = 256;
  protected Map<Integer, PerformanceLogger> performanceLoggers = new HashMap<>();

  private static Drbg getDrbg(int myId, int prgSeedLength) {
    byte[] seed = new byte[prgSeedLength / 8];
    new Random(myId).nextBytes(seed);
    return AesCtrDrbgFactory.fromDerivedSeed(seed);
  }

  private static Map<Integer, RotList> getSeedOts(int myId, List<Integer> partyIds,
      int prgSeedLength,
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

  public void runTest(
      TestThreadRunner.TestThreadFactory<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>, ProtocolBuilderNumeric> f,
      EvaluationStrategy evalStrategy, PreprocessingStrategy preProStrat, int noOfParties) {
    List<Integer> ports = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      ports.add(9000 + i * (noOfParties - 1));
    }
    NetManager tripleManager = new NetManager(ports);
    NetManager otManager = new NetManager(ports);
    NetManager expPipeManager = new NetManager(ports);

    Map<Integer, NetworkConfiguration> netConf =
        NetworkUtil.getNetworkConfigurations(ports);
    Map<Integer, TestThreadRunner.TestThreadConfiguration<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>, ProtocolBuilderNumeric>> conf =
        new HashMap<>();

    for (int playerId : netConf.keySet()) {

      BatchEvaluationStrategy<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>> batchEvalStrat = evalStrategy
          .getStrategy();

      CRTProtocolSuite<SpdzResourcePool, SpdzResourcePool> ps = new CRTProtocolSuite<>(
          new SpdzProtocolSupplier(), new SpdzProtocolSupplier());

      ProtocolEvaluator<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>> evaluator =
          new BatchedProtocolEvaluator<>(batchEvalStrat, ps);

      SecureComputationEngine<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>, ProtocolBuilderNumeric> sce =
          new SecureComputationEngineImpl<>(ps, evaluator);

      CRTDataSupplier dataSupplier = new CRTDummyDataSupplier(playerId, noOfParties,
          DEFAULT_FIELD_LEFT, DEFAULT_FIELD_RIGHT,
          x -> toSpdzSInt(x, playerId, noOfParties, DEFAULT_FIELD_LEFT, new Random(1234),
              new BigInteger(DEFAULT_FIELD_LEFT.getModulus().bitLength(), new Random(0))
                  .mod(DEFAULT_FIELD_LEFT.getModulus())),
          x -> toSpdzSInt(x, playerId, noOfParties, DEFAULT_FIELD_RIGHT, new Random(1234),
              new BigInteger(DEFAULT_FIELD_RIGHT.getModulus().bitLength(), new Random(0))
                  .mod(DEFAULT_FIELD_RIGHT.getModulus())));

      TestThreadRunner.TestThreadConfiguration<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>, ProtocolBuilderNumeric> ttc =
          new TestThreadRunner.TestThreadConfiguration<>(sce, () -> {
            Pair<SpdzResourcePool, SpdzResourcePool> rps = createResourcePools(playerId,
                noOfParties, preProStrat, otManager, tripleManager, expPipeManager,
                DEFAULT_FIELD_LEFT, DEFAULT_FIELD_RIGHT);
            return new CRTResourcePoolImpl<>(playerId, noOfParties, dataSupplier, rps.getFirst(),
                rps.getSecond());
          }, () -> new SocketNetwork(netConf.get(playerId)));
      conf.put(playerId, ttc);
    }
    TestThreadRunner.run(f, conf);
    tripleManager.close();
    expPipeManager.close();
  }

  private SpdzDataSupplier getSupplier(int myId,
      int numberOfParties,
      PreprocessingStrategy preProStrat,
      NetManager otGenerator,
      NetManager tripleGenerator,
      NetManager expPipeGenerator, FieldDefinition definition) {

    SpdzDataSupplier supplier;
    if (preProStrat == DUMMY) {
      supplier = new SpdzDummyDataSupplier(myId, numberOfParties,
          definition,
          new BigInteger(definition.getModulus().bitLength(), new Random(0))
              .mod(definition.getModulus()));
    } else if (preProStrat == MASCOT) {
      List<Integer> partyIds =
          IntStream.range(1, numberOfParties + 1).boxed().collect(Collectors.toList());
      Drbg drbg = getDrbg(myId, PRG_SEED_LENGTH);
      Map<Integer, RotList> seedOts =
          getSeedOts(myId, partyIds, PRG_SEED_LENGTH, drbg, otGenerator.createExtraNetwork(myId));
      FieldElement ssk = SpdzMascotDataSupplier.createRandomSsk(definition, PRG_SEED_LENGTH);
      supplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, numberOfParties,
          () -> tripleGenerator.createExtraNetwork(myId), definition.getModulus().bitLength(),
          definition,
          new Function<Integer, SpdzSInt[]>() {

            private SpdzMascotDataSupplier tripleSupplier;
            private CloseableNetwork pipeNetwork;

            @Override
            public SpdzSInt[] apply(Integer pipeLength) {
              if (pipeNetwork == null) {
                pipeNetwork = expPipeGenerator.createExtraNetwork(myId);
                tripleSupplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, numberOfParties,
                    () -> pipeNetwork, definition.getModulus().bitLength(), definition, null,
                    seedOts, drbg, ssk);
              }
              DRes<List<DRes<SInt>>> pipe =
                  createPipe(myId, numberOfParties, pipeLength, pipeNetwork, tripleSupplier, 0);
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
    return supplier;
  }

  private Pair<SpdzResourcePool, SpdzResourcePool> createResourcePools(int myId,
      int numberOfParties,
      PreprocessingStrategy preProStrat,
      NetManager otGenerator,
      NetManager tripleGenerator,
      NetManager expPipeGenerator, FieldDefinition definitionLeft,
      FieldDefinition definitionRight) {

    SpdzDataSupplier supplierLeft = getSupplier(myId, numberOfParties, preProStrat, otGenerator,
        tripleGenerator, expPipeGenerator, definitionLeft);
    SpdzDataSupplier supplierRight = getSupplier(myId, numberOfParties, preProStrat, otGenerator,
        tripleGenerator, expPipeGenerator, definitionRight);

    SpdzResourcePool rpLeft = new SpdzResourcePoolImpl(myId, numberOfParties,
        new OpenedValueStoreImpl<>(), supplierLeft, AesCtrDrbg::new);
    SpdzResourcePool rpRight = new SpdzResourcePoolImpl(myId, numberOfParties,
        new OpenedValueStoreImpl<>(), supplierRight, AesCtrDrbg::new);

    return new Pair<>(rpLeft, rpRight);
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
      Network network, int maxBitLength) {
    BatchedStrategy<SpdzResourcePool> batchedStrategy = new BatchedStrategy<>();
    SpdzProtocolSuite spdzProtocolSuite = new SpdzProtocolSuite(maxBitLength);
    BatchedProtocolEvaluator<SpdzResourcePool> batchedProtocolEvaluator =
        new BatchedProtocolEvaluator<>(batchedStrategy, spdzProtocolSuite);
    batchedProtocolEvaluator.eval(spdzBuilder.build(), tripleResourcePool, network);
  }

  private DRes<List<DRes<SInt>>> createPipe(int myId, int noOfPlayers, int pipeLength,
      CloseableNetwork pipeNetwork, SpdzMascotDataSupplier tripleSupplier, int maxBitLength) {

    ProtocolBuilderNumeric sequential = new SpdzBuilder(
        new BasicNumericContext(maxBitLength, myId, noOfPlayers,
            tripleSupplier.getFieldDefinition(), 0)).createSequential();
    SpdzResourcePoolImpl tripleResourcePool =
        new SpdzResourcePoolImpl(myId, noOfPlayers, new OpenedValueStoreImpl<>(), tripleSupplier,
            AesCtrDrbg::new);

    DRes<List<DRes<SInt>>> exponentiationPipe =
        new DefaultPreprocessedValues(sequential).getExponentiationPipe(pipeLength);
    evaluate(sequential, tripleResourcePool, pipeNetwork, maxBitLength);
    return exponentiationPipe;
  }

  private SpdzSInt toSpdzSInt(BigInteger x, int myId, int players, FieldDefinition field,
      Random random, BigInteger secretSharedKey) {
    List<BigInteger> shares = new ArrayList<>();
    List<BigInteger> macShares = new ArrayList<>();
    BigInteger s = BigInteger.ZERO;
    BigInteger m = BigInteger.ZERO;
    for (int i = 1; i <= players - 1; i++) {
      BigInteger share = Util.randomBigInteger(random, field.getModulus());
      s = s.add(share).mod(field.getModulus());
      shares.add(share);

      BigInteger macShare = Util.randomBigInteger(random, field.getModulus());
      m = m.add(macShare).mod(field.getModulus());
      macShares.add(macShare);
    }
    BigInteger share = x.subtract(s).mod(field.getModulus());
    shares.add(share);

    BigInteger macShare = x.multiply(secretSharedKey).subtract(m).mod(field.getModulus());
    macShares.add(macShare);

    return new SpdzSInt(
        field.createElement(shares.get(myId - 1)),
        field.createElement(macShares.get(myId - 1))
    );
  }

}
