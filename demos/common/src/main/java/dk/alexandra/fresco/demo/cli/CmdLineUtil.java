package dk.alexandra.fresco.demo.cli;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
public class CmdLineUtil<ResourcePoolT extends ResourcePool, BuilderT extends ProtocolBuilder> {

  private static final Logger logger = LoggerFactory.getLogger(CmdLineUtil.class);

  private final Options options;
  private Options appOptions;
  private CommandLine cmd;
  private NetworkConfiguration networkConfiguration;
  private Network network;
  private boolean logPerformance;
  private ProtocolSuite<ResourcePoolT, BuilderT> protocolSuite;
  private ProtocolEvaluator<ResourcePoolT, BuilderT> evaluator;
  private ResourcePoolT resourcePool;
  private SecureComputationEngine<ResourcePoolT, BuilderT> sce;

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

  public ProtocolEvaluator<ResourcePoolT, BuilderT> getEvaluator() {
    return evaluator;
  }

  public ProtocolSuite<ResourcePoolT, BuilderT> getProtocolSuite() {
    return this.protocolSuite;
  }

  public SecureComputationEngine<ResourcePoolT, BuilderT> getSce() {
    return this.sce;
  }

  /**
   * Adds standard options.
   *
   * <p>TODO: Move standard options to SCE.
   * 
   * <p>For instance, options for setting player id and protocol suite.
   */
  private Options buildStandardOptions() {
    Options options = new Options();

    options.addOption(
        Option.builder("i").desc("The id of this player. Must be a unique positive integer.")
            .longOpt("id").required(true).hasArg().build());

    options.addOption(Option.builder("s")
        .desc("The name of the protocol suite to use. Must be one of these: "
            + CmdLineProtocolSuite.getSupportedProtocolSuites())
        .longOpt("suite").required(true).hasArg().build());

    options.addOption(Option.builder("p")
        .desc(
            "Connection data for a party. Use -p multiple times to specify many players. "
            + "You must always at least include yourself. Must be on the form "
            + "[id]:[hostname]:[port] or [id]:[hostname]:[port]:[shared key]. "
            + "id is a unique positive integer for the player, host and port is where to "
            + "find the player, shared key is an optional string defining a secret key "
            + "that is shared by you and the other player (the other player must submit "
            + "the same key for you as you do for him). ")
        .longOpt("party").required(true).hasArgs().build());

    options.addOption(Option.builder("e")
        .desc("The strategy for evaluation. Can be one of: "
            + Arrays.toString(EvaluationStrategy.values()) + ". Defaults to "
            + EvaluationStrategy.SEQUENTIAL)
        .longOpt("evaluator").required(false).hasArg(true).build());

    options.addOption(Option.builder("b")
        .desc(
            "The maximum number of native protocols kept in memory at any point in time. "
            + "Defaults to 4096")
        .longOpt("max-batch").required(false).hasArg(true).build());

    options.addOption(Option.builder("D").argName("property=value")
        .desc("Used to set properties of protocol suite and other customizable components.")
        .required(false).hasArg().numberOfArgs(2).valueSeparator().build());

    options.addOption(
        Option.builder("l").desc("Informs FRESCO that performance logging should be triggered")
            .required(false).hasArg(false).build());
    return options;
  }

  private int parseNonzeroInt(String optionId) throws ParseException {
    int res;
    String opStr = this.cmd.getOptionValue(optionId);
    try {
      res = Integer.parseInt(opStr);
      if (res < 1) {
        throw new ParseException(optionId + " must be a positive integer larger than 0");
      }
    } catch (NumberFormatException e) {
      throw new ParseException(
          "Cannot parse '" + this.cmd.getOptionValue(optionId) + "' as an integer");
    }
    return res;
  }

  private String validateAndGetProtocolSuite() throws ParseException {
    Object suiteObj = this.cmd.getParsedOptionValue("s");
    String suite = ((String) suiteObj).toLowerCase();

    if (!CmdLineProtocolSuite.getSupportedProtocolSuites().contains(suite)) {
      throw new ParseException("Unknown protocol suite: " + suite);
    }
    return suite;
  }
  
  private void parseAndSetupNetwork() throws ParseException {
    int myId = parseNonzeroInt("i");
    final Map<Integer, Party> parties = new HashMap<>();
    
    for (String partyOptions : this.cmd.getOptionValues("p")) {
      String[] p = partyOptions.split(":");
      if (p.length < 3 || p.length > 4) {
        throw new ParseException("Could not parse '" + partyOptions
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
        throw new ParseException("Could not parse '" + partyOptions + "': " + e.getMessage());
      }
    }
    if (!parties.containsKey(myId)) {
      throw new ParseException("This party is given the id " + myId
          + " but this id is not present in the list of parties: " + parties.keySet());
    }

    this.networkConfiguration = new NetworkConfigurationImpl(myId, parties);
  }
  
  /**
   * Create a network and connect to the other parties.
   */
  public void startNetwork() {
    this.network = new KryoNetNetwork(networkConfiguration);
    if (logPerformance) {
      this.network = new NetworkLoggingDecorator(this.network);
    }
  }

  private int getMaxBatchSize() throws ParseException {
    int maxBatchSize = 4096;
    if (this.cmd.hasOption("b")) {
      try {
        maxBatchSize = Integer.parseInt(this.cmd.getOptionValue("b"));
      } catch (NumberFormatException e) {
        throw new ParseException("Batch size has to be an integer");
      }
    }
    return maxBatchSize;
  }

  /**
   * For adding application specific options.
   */
  public void addOption(Option option) {
    this.appOptions.addOption(option);
  }

  /**
   * Parses the arguments. Also constructs various configuration objects such as ProtocolSuite
   * and ResourcePool. 
   * @param args The arguments
   * @return CommandLine
   */
  @SuppressWarnings("unchecked")
  public CommandLine parse(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      Options allOpts = getOptions();
      
      this.cmd = parser.parse(allOpts, args);
      if (this.cmd.hasOption("h")) {
        displayHelp();
        return null;
      }

      if (this.cmd.hasOption("l")) {
        this.logPerformance = true;
      }

      
      String protocolSuiteName = validateAndGetProtocolSuite();
      parseAndSetupNetwork();

      CmdLineProtocolSuite protocolSuiteParser = new CmdLineProtocolSuite(protocolSuiteName,
          cmd.getOptionProperties("D"), this.networkConfiguration.getMyId(),
          this.networkConfiguration.noOfParties());
      protocolSuite = (ProtocolSuite<ResourcePoolT, BuilderT>)
          protocolSuiteParser.getProtocolSuite();
      resourcePool = (ResourcePoolT) protocolSuiteParser.getResourcePool();
      BatchEvaluationStrategy<ResourcePoolT> batchEvalStrat = evaluationStrategyFromString(
          this.cmd.getOptionValue("e", EvaluationStrategy.SEQUENTIAL.name()));
      if (logPerformance) {
        batchEvalStrat = new BatchEvaluationLoggingDecorator<>(batchEvalStrat);
      }
      int maxBatchSize = getMaxBatchSize();
      this.evaluator = new BatchedProtocolEvaluator<>(batchEvalStrat, protocolSuite,
          maxBatchSize);
    } catch (Exception e) {
      ExceptionConverter.safe(() -> {
        closeNetwork(); 
        return null; 
      },
          "Failed to close the network");

      System.err.println("Error while parsing arguments / instantiating protocol suite: "
          + e.getLocalizedMessage());
      System.err.println();
      displayHelp();
      throw new IllegalArgumentException("Error while parsing arguments: "
          + e.getLocalizedMessage(), e);
    }

    if (logPerformance) {
      evaluator = new EvaluatorLoggingDecorator<>(evaluator);
    }
    this.sce = new SecureComputationEngineImpl<>(protocolSuite, evaluator);

    return this.cmd;
  }

  private Options getOptions() {
    Options allOpts = new Options();
    
    Option helpOpt = Option.builder("h").desc("Displays this help message").longOpt("help")
        .required(false).hasArg(false).build();
    allOpts.addOption(helpOpt);
    
    for (Option o : options.getOptions()) {
      allOpts.addOption(o);
    }
    
    for (Option o : appOptions.getOptions()) {
      allOpts.addOption(o);
    }
    return allOpts;
  }
  
  private BatchEvaluationStrategy<ResourcePoolT> evaluationStrategyFromString(String evalStr) {
    EvaluationStrategy evalStrategy = EvaluationStrategy.valueOf(evalStr.toUpperCase());
    return evalStrategy.getStrategy();
  }

  /**
   * Print a help text, displaying the various options. 
   */
  public void displayHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setSyntaxPrefix("");
    formatter.printHelp("General SCE options are:", this.options);
    formatter.setSyntaxPrefix("");
    formatter.printHelp("Application specific options are:", this.appOptions);
  }

  /**
   * Attempts to close the network.
   * @throws IOException If the networks fails to close
   */
  public void closeNetwork() throws IOException {
    if (this.network != null) {
      ((Closeable) this.network).close();
    }
  }
}
