package dk.alexandra.fresco.demo.cli;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzExponentiationPipeProtocol;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzMascotDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDataSupplier;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesNaorPinkasOt;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesOt;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproResourcePool;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import dk.alexandra.fresco.tools.ot.base.AbstractNaorPinkasOT;
import dk.alexandra.fresco.tools.ot.base.BouncyCastleNaorPinkas;
import dk.alexandra.fresco.tools.ot.otextension.RotList;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility for reading all configuration from command line.
 * <p>
 * A set of default configurations are used when parameters are not specified at runtime.
 * </p>
 */
public class CmdLineProtocolSuite {

  private final int myId;
  private final int noOfPlayers;
  private final ProtocolSuite<?, ?> protocolSuite;
  private final ResourcePool resourcePool;

  static String getSupportedProtocolSuites() {
    String[] strings = {"dummybool", "dummyarithmetic", "spdz", "tinytables", "tinytablesprepro"};
    return Arrays.toString(strings);
  }

  CmdLineProtocolSuite(String protocolSuiteName, Properties properties, int myId,
      int noOfParties, Supplier<Network> networkSupplier) {

    this.myId = myId;
    this.noOfPlayers = noOfParties;

    if (protocolSuiteName.equals("dummybool")) {
      this.protocolSuite = new DummyBooleanProtocolSuite();
      this.resourcePool =
          new ResourcePoolImpl(myId, noOfPlayers);
    } else if (protocolSuiteName.equals("dummyarithmetic")) {
      this.protocolSuite = dummyArithmeticFromCmdLine(properties);
      String mod = properties.getProperty("modulus",
          "67039039649712985497870124991238141152738485774711365274259660130265015367064643"
              + "54255445443244279389455058889493431223951165286470575994074291745908195329");
      this.resourcePool =
          new DummyArithmeticResourcePoolImpl(myId, noOfPlayers,
              new BigIntegerFieldDefinition(mod));
    } else if (protocolSuiteName.equals("spdz")) {
      this.protocolSuite = getSpdzProtocolSuite(properties);
      this.resourcePool = createSpdzResourcePool(properties, networkSupplier);
    } else if (protocolSuiteName.equals("tinytablesprepro")) {
      String tinytablesFileOption = "tinytables.file";
      String tinyTablesFilePath = properties.getProperty(tinytablesFileOption, "tinytables");
      this.protocolSuite = tinyTablesPreProFromCmdLine(properties);
      Drbg random = new AesCtrDrbg();
      TinyTablesOt baseOt = new TinyTablesNaorPinkasOt(Util.otherPlayerId(myId), random);
      this.resourcePool = new TinyTablesPreproResourcePool(myId, baseOt,
          random, 128, 40, 16000, new File(
              tinyTablesFilePath), networkSupplier);
    } else {
      this.protocolSuite = tinyTablesFromCmdLine(properties);
      this.resourcePool = new ResourcePoolImpl(myId, noOfPlayers);
    }
  }

  public ResourcePool getResourcePool() {
    return resourcePool;
  }

  public ProtocolSuite<?, ?> getProtocolSuite() {
    return this.protocolSuite;
  }

  private ProtocolSuite<?, ?> dummyArithmeticFromCmdLine(Properties properties) {
    String mod = properties.getProperty("modulus",
        "67039039649712985497870124991238141152738485774711365274259660130265015367064643"
            + "54255445443244279389455058889493431223951165286470575994074291745908195329");
    int maxBitLength = Integer.parseInt(properties.getProperty("maxbitlength", "150"));
    int fixedPointPrecision = Integer.parseInt(properties.getProperty("fixedPointPrecision", "16"));
    return new DummyArithmeticProtocolSuite(new BigIntegerFieldDefinition(mod), maxBitLength,
        fixedPointPrecision);
  }

  private ProtocolSuite<?, ?> getSpdzProtocolSuite(Properties properties) {
    Properties p = getProperties(properties);
    // TODO: Figure out a meaningful default for the below
    final int maxBitLength = Integer.parseInt(p.getProperty("spdz.maxBitLength", "64"));
    if (maxBitLength < 2) {
      throw new RuntimeException("spdz.maxBitLength must be > 1");
    }
    return new SpdzProtocolSuite(maxBitLength);
  }

  private Properties getProperties(Properties properties) {
    return properties;
  }

  private SpdzResourcePool createSpdzResourcePool(Properties properties,
      Supplier<Network> networkSupplier) {

    String strat = properties.getProperty("spdz.preprocessingStrategy", "DUMMY");
    final PreprocessingStrategy strategy = PreprocessingStrategy.valueOf(strat);

    final int modBitLength = Integer.parseInt(properties.getProperty("spdz.modBitLength", "128"));
    final BigInteger modulus = ModulusFinder.findSuitableModulus(modBitLength);
    final BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(modulus);
    SpdzDataSupplier supplier = null;

    if (strategy == PreprocessingStrategy.DUMMY) {
      supplier = new SpdzDummyDataSupplier(myId, noOfPlayers, definition, modulus);
    } else if (strategy == PreprocessingStrategy.STATIC) {
      int noOfThreadsUsed = 1;
      String storageName = SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_"
              + myId + "_" + 0 + "_";
      supplier = new SpdzStorageDataSupplier(
              new FilebasedStreamedStorageImpl(new InMemoryStorage()), storageName, noOfPlayers);
    } else {
      // MASCOT preprocessing
      int prgSeedLength = 256;

      Network network = networkSupplier.get();
      Drbg drbg = getDrbg(myId, prgSeedLength);
      Map<Integer, RotList> seedOts =
              getSeedOts(myId, noOfPlayers, prgSeedLength, drbg, network);
      FieldElement ssk = SpdzMascotDataSupplier.createRandomSsk(definition, prgSeedLength);

      SpdzDataSupplier preprocessedValuesDataSupplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, noOfPlayers,
                      () -> network, modBitLength, definition, null, seedOts, drbg, ssk);
      SpdzResourcePool preprocessedValuesResourcePool = new SpdzResourcePoolImpl(myId, noOfPlayers, new SpdzOpenedValueStoreImpl(),
                      preprocessedValuesDataSupplier, AesCtrDrbg::new);

      supplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, noOfPlayers, () -> network,
              modBitLength, definition, new Function<Integer, SpdzSInt[]>() {

                @Override
                public SpdzSInt[] apply(Integer pipeLength) {
                  DRes<List<DRes<SInt>>> pipe = createPipe(pipeLength, network, preprocessedValuesResourcePool);
                  return computeSInts(pipe);
                }

              }, seedOts, drbg, ssk);
    }

    return new SpdzResourcePoolImpl(myId, noOfPlayers, new SpdzOpenedValueStoreImpl(), supplier,
        AesCtrDrbg::new);
  }

  private DRes<List<DRes<SInt>>> createPipe(int pipeLength, Network network,
      SpdzResourcePool resourcePool) {
    SpdzProtocolSuite spdzProtocolSuite = (SpdzProtocolSuite) protocolSuite;
    ProtocolBuilderNumeric sequential = spdzProtocolSuite.init(resourcePool).createSequential();
    Application<List<DRes<SInt>>, ProtocolBuilderNumeric> expPipe = builder ->
            new DefaultPreprocessedValues(builder).getExponentiationPipe(pipeLength);
    DRes<List<DRes<SInt>>> exponentiationPipe = expPipe.buildComputation(sequential);
    evaluate(sequential, resourcePool, network);
    return exponentiationPipe;
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
    SpdzProtocolSuite spdzProtocolSuite = (SpdzProtocolSuite) this.protocolSuite;
    BatchedProtocolEvaluator<SpdzResourcePool> batchedProtocolEvaluator =
        new BatchedProtocolEvaluator<>(batchedStrategy, spdzProtocolSuite);
    batchedProtocolEvaluator.eval(spdzBuilder.build(), tripleResourcePool, network);
  }

  private Drbg getDrbg(int myId, int prgSeedLength) {
    byte[] seed = new byte[prgSeedLength / 8];
    new Random(myId).nextBytes(seed);
    return AesCtrDrbgFactory.fromDerivedSeed(seed);
  }

  private Map<Integer, RotList> getSeedOts(int myId, int parties, int prgSeedLength, Drbg drbg,
      Network network) {
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (int otherId = 1; otherId <= parties; otherId++) {
      if (myId != otherId) {
        AbstractNaorPinkasOT ot = new BouncyCastleNaorPinkas(otherId, drbg, network);
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

  private ProtocolSuite<?, ?> tinyTablesPreProFromCmdLine(Properties properties) {
    return new TinyTablesPreproProtocolSuite();
  }

  private ProtocolSuite<?, ?> tinyTablesFromCmdLine(Properties properties) {
    String tinytablesFileOption = "tinytables.file";
    String tinyTablesFilePath = properties.getProperty(tinytablesFileOption, "tinytables");
    return new TinyTablesProtocolSuite(myId, new File(tinyTablesFilePath));
  }

}
