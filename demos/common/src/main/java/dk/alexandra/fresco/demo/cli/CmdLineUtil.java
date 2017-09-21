/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.demo.cli;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.PerformanceLogger.Flag;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDummyImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for reading all configuration from command line.
 * <p>
 * A set of default configurations are used when parameters are not specified at runtime.
 * </p>
 */
public class CmdLineUtil<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> {

  private final static Logger logger = LoggerFactory.getLogger(CmdLineUtil.class);

  private final Options options;
  private Options appOptions;
  private CommandLine cmd;
  private NetworkConfiguration networkConfiguration;
  private Network network;
  private ProtocolSuite<ResourcePoolT, Builder> protocolSuite;
  private ProtocolEvaluator<ResourcePoolT, Builder> evaluator;
  private ResourcePoolT resourcePool;

  public CmdLineUtil() {
    this.appOptions = new Options();
    this.options = buildStandardOptions();
  }

  public NetworkConfiguration getNetworkConfiguration() {
    return this.networkConfiguration;
  }

  public Network getNetwork() {
    return this.network;
  }

  public ResourcePoolT getResourcePool() {
    return resourcePool;
  }

  public NetworkingStrategy getNetworkStrategy() {
    return NetworkingStrategy.KRYONET;
  }

  public ProtocolEvaluator<ResourcePoolT, Builder> getEvaluator() {
    return evaluator;
  }

  public ProtocolSuite<ResourcePoolT, Builder> getProtocolSuite() {
    return this.protocolSuite;
  }

  /**
   * Adds standard options.
   *
   * TODO: Move standard options to SCE.
   *
   * For instance, options for setting player id and protocol suite.
   */
  private static Options buildStandardOptions() {
    Options options = new Options();

    options.addOption(
        Option.builder("i").desc("The id of this player. Must be a unique positive integer.")
            .longOpt("id").required(true).hasArg().build());

    options.addOption(Option.builder("s")
        .desc("The name of the protocol suite to use. Must be one of these: "
            + getSupportedProtocolSuites() + ". " + "The default value is: bgw")
        .longOpt("suite").required(true).hasArg().build());

    options.addOption(Option.builder("p")
        .desc(
            "Connection data for a party. Use -p multiple times to specify many players. You must always at least include yourself."
                + "Must be on the form [id]:[hostname]:[port] or [id]:[hostname]:[port]:[shared key]. "
                + "id is a unique positive integer for the player, host and port is where to find the player, "
                + " shared key is an optional string defining a secret key that is shared by you and the other player "
                + " (the other player must submit the same key for you as you do for him). ")
        .longOpt("party").required(true).hasArgs().build());

    options.addOption(Option.builder("e")
        .desc("The strategy for evaluation. Can be one of: "
            + Arrays.toString(EvaluationStrategy.values()) + ". Defaults to "
            + EvaluationStrategy.SEQUENTIAL)
        .longOpt("evaluator").required(false).hasArg(true).build());

    options.addOption(Option.builder("b")
        .desc(
            "The maximum number of native protocols kept in memory at any point in time. Defaults to 4096")
        .longOpt("max-batch").required(false).hasArg(true).build());

    options.addOption(Option.builder("D").argName("property=value")
        .desc("Used to set properties of protocol suite and other customizable components.")
        .required(false).hasArg().numberOfArgs(2).valueSeparator().build());

    options.addOption(
        Option.builder("l").desc("Informs FRESCO that performance logging should be triggered")
            .required(false).hasArg(false).build());
    return options;
  }

  private static String getSupportedProtocolSuites() {
    String[] strings = {"dummybool", "dummyarithmetic", "spdz", "tinytables", "tinytablesprepro"};
    return Arrays.toString(strings);
  }

  private int parseNonzeroInt(String optionId) throws ParseException {
    int res;
    String opStr = this.cmd.getOptionValue(optionId);
    if (opStr == null) {
      throw new ParseException("No value for option: " + optionId);
    }
    try {
      res = Integer.parseInt(opStr);
      if (res < 0) {
        throw new ParseException(optionId + " must be a positive integer");
      }
    } catch (NumberFormatException e) {
      throw new ParseException(
          "Cannot parse '" + this.cmd.getOptionValue(optionId) + "' as an integer");
    }
    return res;
  }

  private void validateStandardOptions() throws ParseException {
    int myId;

    Object suiteObj = this.cmd.getParsedOptionValue("s");
    if (suiteObj == null) {
      throw new ParseException("Cannot parse '" + this.cmd.getOptionValue("s") + "' as a string");
    }

    final Map<Integer, Party> parties = new HashMap<>();
    final String suite = (String) suiteObj;

    if (!getSupportedProtocolSuites().contains(suite.toLowerCase())) {
      throw new ParseException("Unknown protocol suite: " + suite);
    }

    myId = parseNonzeroInt("i");
    if (myId == 0) {
      throw new ParseException("Player id must be positive, non-zero integer");
    }

    for (String pStr : this.cmd.getOptionValues("p")) {
      String[] p = pStr.split(":");
      if (p.length < 3 || p.length > 4) {
        throw new ParseException("Could not parse '" + pStr
            + "' as [id]:[host]:[port] or [id]:[host]:[port]:[shared key]");
      }
      try {
        int id = Integer.parseInt(p[0]);
        InetAddress.getByName(p[1]); // Check that hostname is valid.
        int port = Integer.parseInt(p[2]);
        Party party;
        if (p.length == 3) {
          party = new Party(id, p[1], port);
        } else {
          party = new Party(id, p[1], port, p[3]);
        }
        if (parties.containsKey(id)) {
          throw new ParseException("Party ids must be unique");
        }
        parties.put(id, party);
      } catch (NumberFormatException | UnknownHostException e) {
        throw new ParseException("Could not parse '" + pStr + "': " + e.getMessage());
      }
    }
    if (!parties.containsKey(myId)) {
      throw new ParseException("This party is given the id " + myId
          + " but this id is not present in the list of parties " + parties.keySet());
    }

    if (this.cmd.hasOption("e")) {
      try {
        this.evaluator = EvaluationStrategy.fromString(this.cmd.getOptionValue("e"));
      } catch (ConfigurationException e) {
        throw new ParseException("Invalid evaluation strategy: " + this.cmd.getOptionValue("e"));
      }
    } else {
      this.evaluator = new SequentialEvaluator<ResourcePoolT, Builder>();
    }

    if (this.cmd.hasOption("b")) {
      try {
        evaluator.setMaxBatchSize(Integer.parseInt(this.cmd.getOptionValue("b")));
      } catch (Exception e) {
        throw new ParseException("");
      }
    }

    logger.info("Player id          : " + myId);
    logger.info("NativeProtocol suite     : " + suite);
    logger.info("Players            : " + parties);
    logger.info("Evaluation strategy: " + evaluator);

    this.networkConfiguration = new NetworkConfigurationImpl(myId, parties);
    this.network = new KryoNetNetwork();
    this.network.init(networkConfiguration, 1);
  }

  /**
   * For adding application specific options.
   */
  public void addOption(Option option) {
    this.appOptions.addOption(option);
  }

  @SuppressWarnings("unchecked")
  public CommandLine parse(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      Options helpOpt = new Options();
      helpOpt.addOption(Option.builder("h").desc("Displays this help message").longOpt("help")
          .required(false).hasArg(false).build());

      cmd = parser.parse(helpOpt, args, true);
      if (cmd.hasOption("h")) {
        displayHelp();
        System.exit(0);
      }
      Options allOpts = new Options();
      for (Option o : options.getOptions()) {
        allOpts.addOption(o);
      }
      for (Option o : appOptions.getOptions()) {
        allOpts.addOption(o);
      }
      cmd = parser.parse(allOpts, args);

      validateStandardOptions();
      EnumSet<Flag> performanceLoggerFlags = parseFlags(this.cmd);
      PerformanceLogger pl = null;
      if (performanceLoggerFlags != null) {
        pl = new PerformanceLogger(this.networkConfiguration.getMyId(), performanceLoggerFlags);
      }
      String protocolSuiteName = ((String) this.cmd.getParsedOptionValue("s")).toLowerCase();
      switch (protocolSuiteName) {
        case "dummybool":
          this.protocolSuite =
              (ProtocolSuite<ResourcePoolT, Builder>) new DummyBooleanProtocolSuite();
          this.resourcePool =
              (ResourcePoolT) new ResourcePoolImpl(this.networkConfiguration.getMyId(),
                  this.networkConfiguration.noOfParties(), network, new Random(),
                  new SecureRandom(), pl);
          break;
        case "dummyarithmetic":
          this.protocolSuite =
              (ProtocolSuite<ResourcePoolT, Builder>) dummyArithmeticFromCmdLine(cmd);
          Properties p = cmd.getOptionProperties("D");
          BigInteger mod = new BigInteger(p.getProperty("modulus",
              "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329"));
          this.resourcePool = (ResourcePoolT) new DummyArithmeticResourcePoolImpl(
              this.networkConfiguration.getMyId(), this.networkConfiguration.noOfParties(), network,
              new Random(), new SecureRandom(), mod, pl);
          break;
        case "spdz":
          this.protocolSuite =
              (ProtocolSuite<ResourcePoolT, Builder>) SpdzConfigurationFromCmdLine(cmd);
          this.resourcePool =
              (ResourcePoolT) createSpdzResourcePool(this.networkConfiguration.getMyId(),
                  this.networkConfiguration.noOfParties(), network, new Random(),
                  new SecureRandom(), cmd, pl);
          break;
        case "tinytablesprepro":
          this.protocolSuite =
              (ProtocolSuite<ResourcePoolT, Builder>) tinyTablesPreProFromCmdLine(cmd,
                  this.networkConfiguration.getMyId());
          this.resourcePool =
              (ResourcePoolT) new ResourcePoolImpl(this.networkConfiguration.getMyId(),
                  this.networkConfiguration.noOfParties(), network, new Random(),
                  new SecureRandom(), pl);
          break;
        case "tinytables":
          this.protocolSuite = (ProtocolSuite<ResourcePoolT, Builder>) tinyTablesFromCmdLine(cmd,
              this.networkConfiguration.getMyId());
          this.resourcePool =
              (ResourcePoolT) new ResourcePoolImpl(this.networkConfiguration.getMyId(),
                  this.networkConfiguration.noOfParties(), network, new Random(),
                  new SecureRandom(), pl);
          break;
        default:
          throw new ParseException("Unknown protocol suite: " + protocolSuiteName);
      }
    } catch (ParseException e) {
      System.out.println("Error while parsing arguments: " + e.getLocalizedMessage());
      System.out.println();
      displayHelp();
      System.exit(-1); // TODO: Consider moving to top level.
    }
    return this.cmd;
  }

  private EnumSet<Flag> parseFlags(CommandLine cmd) {
    if (!cmd.hasOption("l")) {
      return null;
    } else {
      return Flag.ALL_OPTS;
    }
  }

  private static ProtocolSuite<?, ?> dummyArithmeticFromCmdLine(CommandLine cmd) {
    Properties p = cmd.getOptionProperties("D");
    BigInteger mod = new BigInteger(p.getProperty("modulus",
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329"));
    int maxBitLength = Integer.parseInt(p.getProperty("maxbitlength", "150"));
    return new DummyArithmeticProtocolSuite(mod, maxBitLength);
  }

  private static ProtocolSuite<?, ?> SpdzConfigurationFromCmdLine(CommandLine cmd) {
    Properties p = cmd.getOptionProperties("D");
    // TODO: Figure out a meaningful default for the below
    final int maxBitLength = Integer.parseInt(p.getProperty("spdz.maxBitLength", "64"));
    if (maxBitLength < 2) {
      throw new RuntimeException("spdz.maxBitLength must be > 1");
    }
    return new SpdzProtocolSuite(maxBitLength);
  }

  private SpdzResourcePool createSpdzResourcePool(int myId, int size, Network network, Random rand,
      SecureRandom secRand, CommandLine cmd, PerformanceLogger pl) {
    Properties p = cmd.getOptionProperties("D");
    final String fuelStationBaseUrl = p.getProperty("spdz.fuelStationBaseUrl", null);
    String strat = p.getProperty("spdz.preprocessingStrategy");
    final PreprocessingStrategy strategy = PreprocessingStrategy.fromString(strat);
    SpdzStorage store;
    switch (strategy) {
      case DUMMY:
        store = new SpdzStorageDummyImpl(myId, size);
        break;
      case STATIC:
        store = new SpdzStorageImpl(0, size, myId,
            new FilebasedStreamedStorageImpl(new InMemoryStorage()));
        break;
      case FUELSTATION:
        store = new SpdzStorageImpl(0, size, myId, fuelStationBaseUrl);
      default:
        throw new ConfigurationException("Unkonwn preprocessing strategy: " + strategy);
    }
    return new SpdzResourcePoolImpl(myId, size, network, rand, secRand, store, pl);
  }

  private static ProtocolSuite<?, ?> tinyTablesPreProFromCmdLine(CommandLine cmd, int myId)
      throws ParseException, IllegalArgumentException {

    Properties p = cmd.getOptionProperties("D");
    String tinytablesFileOption = "tinytables.file";
    String tinyTablesFilePath = p.getProperty(tinytablesFileOption, "tinytables");
    return new TinyTablesPreproProtocolSuite(myId, new File(tinyTablesFilePath));
  }

  private static ProtocolSuite<?, ?> tinyTablesFromCmdLine(CommandLine cmd, int myId)
      throws ParseException, IllegalArgumentException {

    Properties p = cmd.getOptionProperties("D");
    String tinytablesFileOption = "tinytables.file";
    String tinyTablesFilePath = p.getProperty(tinytablesFileOption, "tinytables");
    return new TinyTablesProtocolSuite(myId, new File(tinyTablesFilePath));
  }

  public void displayHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setSyntaxPrefix("");
    formatter.printHelp("General SCE options are:", this.options);
    formatter.setSyntaxPrefix("");
    formatter.printHelp("Application specific options are:", this.appOptions);
  }

}
