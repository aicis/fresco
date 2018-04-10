package dk.alexandra.fresco.demo.cli;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzBuilder;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzMascotDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.io.File;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.cli.ParseException;

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
      int noOfPlayers, KryoNetManager networkManager)
      throws ParseException, NoSuchAlgorithmException {
    this.myId = myId;
    this.noOfPlayers = noOfPlayers;
    if (protocolSuiteName.equals("dummybool")) {
      this.protocolSuite = new DummyBooleanProtocolSuite();
      this.resourcePool =
          new ResourcePoolImpl(myId, noOfPlayers);
    } else if (protocolSuiteName.equals("dummyarithmetic")) {
      this.protocolSuite = dummyArithmeticFromCmdLine(properties);
      BigInteger mod = new BigInteger(properties.getProperty("modulus",
          "67039039649712985497870124991238141152738485774711365274259660130265015367064643"
              + "54255445443244279389455058889493431223951165286470575994074291745908195329"));
      this.resourcePool =
          new DummyArithmeticResourcePoolImpl(myId, noOfPlayers, mod);
    } else if (protocolSuiteName.equals("spdz")) {
      this.protocolSuite = getSpdzProtocolSuite(properties);
      this.resourcePool =
          createSpdzResourcePool(properties, networkManager);
    } else if (protocolSuiteName.equals("tinytablesprepro")) {
      this.protocolSuite = tinyTablesPreProFromCmdLine(properties);
      this.resourcePool =
          new ResourcePoolImpl(myId, noOfPlayers);
    } else {
      this.protocolSuite = tinyTablesFromCmdLine(properties);
      this.resourcePool =
          new ResourcePoolImpl(myId, noOfPlayers);
    }
  }

  public ResourcePool getResourcePool() {
    return resourcePool;
  }

  public ProtocolSuite<?, ?> getProtocolSuite() {
    return this.protocolSuite;
  }


  private ProtocolSuite<?, ?> dummyArithmeticFromCmdLine(Properties properties) {
    BigInteger mod = new BigInteger(properties.getProperty("modulus",
        "67039039649712985497870124991238141152738485774711365274259660130265015367064643"
            + "54255445443244279389455058889493431223951165286470575994074291745908195329"));
    int maxBitLength = Integer.parseInt(properties.getProperty("maxbitlength", "150"));
    int fixedPointPrecision = Integer.parseInt(properties.getProperty("fixedPointPrecision", "16"));
    return new DummyArithmeticProtocolSuite(mod, maxBitLength, fixedPointPrecision);
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

  DRes<List<DRes<SInt>>> createPipe(int myId, int noOfPlayers, int pipeLength,
      KryoNetNetwork pipeNetwork, SpdzMascotDataSupplier tripleSupplier, int maxBitLength,
      int fixedPointPrecision) {

    ProtocolBuilderNumeric sequential = new SpdzBuilder(
        new BasicNumericContext(maxBitLength, tripleSupplier.getModulus(), myId, noOfPlayers),
        new RealNumericContext(fixedPointPrecision)).createSequential();
    SpdzResourcePoolImpl tripleResourcePool =
        new SpdzResourcePoolImpl(myId, noOfPlayers, new SpdzStorageImpl(tripleSupplier));

    DRes<List<DRes<SInt>>> exponentiationPipe =
        new DefaultPreprocessedValues(sequential).getExponentiationPipe(pipeLength);
    evaluate(sequential, tripleResourcePool, pipeNetwork, maxBitLength);
    return exponentiationPipe;
  }

  private Drbg getDrbg(int myId, int prgSeedLength) {
    byte[] seed = new byte[prgSeedLength / 8];
    new Random(myId).nextBytes(seed);
    return new PaddingAesCtrDrbg(seed);
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

  private SpdzResourcePool createSpdzResourcePool(Properties properties,
      KryoNetManager networkManager) {
    String strat = properties.getProperty("spdz.preprocessingStrategy");
    final PreprocessingStrategy strategy = PreprocessingStrategy.valueOf(strat);
    SpdzDataSupplier supplier = null;
    if (strategy == PreprocessingStrategy.DUMMY) {
      supplier = new SpdzDummyDataSupplier(myId, noOfPlayers);
    } else if (strategy == PreprocessingStrategy.STATIC) {
      int noOfThreadsUsed = 1;
      String storageName = properties.getProperty("spdz.storage");
      storageName =
          SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_" + myId + "_" + 0
          + "_";
      supplier = new SpdzStorageDataSupplier(
          new FilebasedStreamedStorageImpl(new InMemoryStorage()), storageName, noOfPlayers);
    } else if (strategy == PreprocessingStrategy.MASCOT) {
      int fixedPointPrecision = Integer.parseInt(properties.getProperty("fixedPointPrecision", "16"));
      // TODO: Figure out a meaningful default for the below
      int maxBitLength = Integer.parseInt(properties.getProperty("spdz.maxBitLength", "64"));
      int modBitLength = Integer.parseInt(properties.getProperty("spdz.modBitLength", "64"));
      // maxBitLength is already tested in getSpdzProtocolSuite.
      if (modBitLength < 2) {
        throw new RuntimeException("spdz.modBitLength must be > 1");
      }

      List<Integer> partyIds =
          IntStream.range(1, noOfPlayers + 1).boxed().collect(Collectors.toList());
      int prgSeedLength = 256;
      BigInteger modulus = ModulusFinder.findSuitableModulus(modBitLength);
      Drbg drbg = getDrbg(myId, prgSeedLength);
      Map<Integer, RotList> seedOts =
          getSeedOts(myId, partyIds, prgSeedLength, drbg, networkManager.createNetwork());
      FieldElement ssk = SpdzMascotDataSupplier.createRandomSsk(modulus, prgSeedLength);
      supplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, noOfPlayers,
          () -> networkManager.createNetwork(), modBitLength, modulus,
          new Function<Integer, SpdzSInt[]>() {

            private SpdzMascotDataSupplier tripleSupplier;
            private KryoNetNetwork pipeNetwork;

            @Override
            public SpdzSInt[] apply(Integer pipeLength) {
              if (pipeNetwork == null) {
                pipeNetwork = networkManager.createNetwork();
                tripleSupplier = SpdzMascotDataSupplier.createSimpleSupplier(myId, noOfPlayers,
                    () -> pipeNetwork, modBitLength, modulus, null, seedOts, drbg, ssk);
              }
              DRes<List<DRes<SInt>>> pipe =
                  createPipe(myId, noOfPlayers, pipeLength, pipeNetwork, tripleSupplier,
                      maxBitLength, fixedPointPrecision);
              return computeSInts(pipe);
            }
          }, seedOts, drbg, ssk);
    }

    SpdzStorage store = new SpdzStorageImpl(supplier);
    return new SpdzResourcePoolImpl(myId, noOfPlayers, store);
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

  private ProtocolSuite<?, ?> tinyTablesPreProFromCmdLine(Properties properties) {
    String tinytablesFileOption = "tinytables.file";
    String tinyTablesFilePath = properties.getProperty(tinytablesFileOption, "tinytables");
    return new TinyTablesPreproProtocolSuite(myId, new File(tinyTablesFilePath));
  }


  private ProtocolSuite<?, ?> tinyTablesFromCmdLine(Properties properties) {
    String tinytablesFileOption = "tinytables.file";
    String tinyTablesFilePath = properties.getProperty(tinytablesFileOption, "tinytables");
    return new TinyTablesProtocolSuite(myId, new File(tinyTablesFilePath));
  }
}
