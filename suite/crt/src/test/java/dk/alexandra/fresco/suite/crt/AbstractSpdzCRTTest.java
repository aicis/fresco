package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
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
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTDataSupplier;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTDummyDataSupplier;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePoolImpl;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTSequentialStrategy;
import dk.alexandra.fresco.suite.spdz.*;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.DUMMY;
import static dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy.MASCOT;

/**
 * Abstract class which handles a lot of boiler plate testing code. This makes running a single test
 * using different parameters quite easy.
 */
public class AbstractSpdzCRTTest {

  protected static final FieldDefinition DEFAULT_FIELD_LEFT =
      MersennePrimeFieldDefinition.find(64);

  // q = p^3 + 139p + 1 where q = DEFAULT_FIELD_RIGHT and p = DEFAULT_FIELD_LEFT.
  protected static final FieldDefinition DEFAULT_FIELD_RIGHT = new BigIntegerFieldDefinition(
      new BigInteger("6277101735386680703605810478201558570393289253487848005721")); //152 + 40, new Random(1234)).nextProbablePrime());
  private static final int PRG_SEED_LENGTH = 256;

  private static Drbg getDrbg(int myId) {
    byte[] seed = new byte[AbstractSpdzCRTTest.PRG_SEED_LENGTH / 8];
    new Random(myId).nextBytes(seed);
    return AesCtrDrbgFactory.fromDerivedSeed(seed);
  }

  private static Map<Integer, RotList> getSeedOts(int myId, List<Integer> partyIds,
      Drbg drbg, Network network) {
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (Integer otherId : partyIds) {
      if (myId != otherId) {
        Ot ot = new DummyOt(otherId, network);
        RotList currentSeedOts = new RotList(drbg, AbstractSpdzCRTTest.PRG_SEED_LENGTH);
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

      BatchEvaluationStrategy<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>> strategy = new CRTSequentialStrategy<>();

      CRTProtocolSuite<SpdzResourcePool, SpdzResourcePool> ps =
          new CRTProtocolSuite<>(
          new SpdzBuilder(new BasicNumericContext(DEFAULT_FIELD_LEFT.getBitLength() - 24,
                  playerId, noOfParties, DEFAULT_FIELD_LEFT, 16, 40)),
      new SpdzBuilder(new BasicNumericContext(DEFAULT_FIELD_RIGHT.getBitLength() - 40,
              playerId, noOfParties, DEFAULT_FIELD_RIGHT, 16, 40)));

      ProtocolEvaluator<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>> evaluator =
          new BatchedProtocolEvaluator<CRTResourcePool<SpdzResourcePool, SpdzResourcePool>>(strategy, ps);

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
                noOfParties, preProStrat, otManager, tripleManager, expPipeManager
            );
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
      Drbg drbg = getDrbg(myId);
      Map<Integer, RotList> seedOts =
          getSeedOts(myId, partyIds, drbg, otGenerator.createExtraNetwork(myId));
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
                  createPipe(myId, numberOfParties, pipeLength, pipeNetwork, tripleSupplier,
                      definition.getBitLength());
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
      NetManager expPipeGenerator) {

    SpdzDataSupplier supplierLeft = getSupplier(myId, numberOfParties, preProStrat, otGenerator,
        tripleGenerator, expPipeGenerator, AbstractSpdzCRTTest.DEFAULT_FIELD_LEFT);
    SpdzDataSupplier supplierRight = getSupplier(myId, numberOfParties, preProStrat, otGenerator,
        tripleGenerator, expPipeGenerator, AbstractSpdzCRTTest.DEFAULT_FIELD_RIGHT);

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
