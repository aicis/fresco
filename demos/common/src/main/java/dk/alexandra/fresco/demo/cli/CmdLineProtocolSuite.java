package dk.alexandra.fresco.demo.cli;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDataSupplier;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesNaorPinkasOt;
import dk.alexandra.fresco.suite.tinytables.ot.TinyTablesOt;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproResourcePool;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import dk.alexandra.fresco.tools.ot.base.DhParameters;
import java.io.File;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.cli.ParseException;

/**
 * Utility for reading all configuration from command line. <p> A set of default configurations are
 * used when parameters are not specified at runtime. </p>
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
      int noOfPlayers) throws ParseException, NoSuchAlgorithmException {
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
          createSpdzResourcePool(properties);
    } else if (protocolSuiteName.equals("tinytablesprepro")) {
      String tinytablesFileOption = "tinytables.file";
      String tinyTablesFilePath = properties.getProperty(tinytablesFileOption, "tinytables");
      this.protocolSuite = tinyTablesPreProFromCmdLine(properties);
      Drbg random = new AesCtrDrbg();
      TinyTablesOt baseOt = new TinyTablesNaorPinkasOt(Util.otherPlayerId(myId), random,
          DhParameters
          .getStaticDhParams());
      this.resourcePool =
          new TinyTablesPreproResourcePool(myId, baseOt, random, 128, 40, 16000, new File(
              tinyTablesFilePath));
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

  private SpdzResourcePool createSpdzResourcePool(Properties properties) {
    String strat = properties.getProperty("spdz.preprocessingStrategy");
    final PreprocessingStrategy strategy = PreprocessingStrategy.valueOf(strat);
    SpdzDataSupplier supplier = null;
    if (strategy == PreprocessingStrategy.DUMMY) {
      supplier = new SpdzDummyDataSupplier(myId, noOfPlayers);
    }
    if (strategy == PreprocessingStrategy.STATIC) {
      int noOfThreadsUsed = 1;
      String storageName = properties.getProperty("spdz.storage");
      storageName =
          SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_" + myId + "_" + 0
              + "_";
      supplier = new SpdzStorageDataSupplier(
          new FilebasedStreamedStorageImpl(new InMemoryStorage()), storageName, noOfPlayers);
    }
    return new SpdzResourcePoolImpl(myId, noOfPlayers, new SpdzOpenedValueStoreImpl(), supplier,
        new AesCtrDrbg(new byte[32]));
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
