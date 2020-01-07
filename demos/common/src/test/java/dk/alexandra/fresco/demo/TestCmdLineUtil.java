package dk.alexandra.fresco.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCmdLineUtil {

  private static final Logger logger = LoggerFactory.getLogger(TestCmdLineUtil.class);

  @Test
  public void testDummyBoolFromCmdLine() {
    CmdLineUtil<ResourcePoolImpl, ProtocolBuilderBinary> cmd = parseAndCloseNetwork("dummybool",
        "-b", "4048");
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof DummyBooleanProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testDummyBoolFromCmdLineWithLogging() {
    CmdLineUtil<ResourcePoolImpl, ProtocolBuilderBinary> cmd = parseAndCloseNetwork("dummybool",
        "-l");
    assertTrue(cmd.getEvaluator() instanceof EvaluatorLoggingDecorator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof DummyBooleanProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testDummyAritmeticFromCmdLine() {
    CmdLineUtil<DummyArithmeticResourcePool, ProtocolBuilderNumeric> cmd = parseAndCloseNetwork(
        "dummyarithmetic", "-b", "4048");
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof DummyArithmeticProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testSpdzAritmeticDummyFromCmdLine() {
    CmdLineUtil<SpdzResourcePool, ProtocolBuilderNumeric> cmd = parseAndCloseNetwork("spdz", "-b",
        "4048", "-D", "spdz.preprocessingStrategy=DUMMY");
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof SpdzProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSpdzBadBitLength() {
    parseAndCloseNetwork("spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=DUMMY", "-D",
        "spdz.maxBitLength=1");
  }

  @Test
  public void testSpdzAritmeticStaticFromCmdLine() throws IOException {
    InitializeStorage
        .initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()), 2, 1, 1, 1, 1,
            1);
    CmdLineUtil<SpdzResourcePool, ProtocolBuilderNumeric> cmd =
        parseAndCloseNetwork("spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=STATIC");
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof SpdzProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
    InitializeStorage.cleanup();
  }

  @Test
  public void testSpdzAritmeticMascotFromCmdLine() throws IOException {
    CmdLineUtil<SpdzResourcePool, ProtocolBuilderNumeric> cmd =
            parseAndCloseNetwork("spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=MASCOT");
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof SpdzProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
    InitializeStorage.cleanup();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSpdzAritmeticBadStrategyFromCmdLine() {
    parseAndCloseNetwork("spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=NO_STRATEGY");
  }

  @Test
  public void testTinyTablesPreproFromCmdLine() {
    CmdLineUtil<?, ?> cmd = parseAndCloseNetwork("tinytablesprepro", "-b", "4048");
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof TinyTablesPreproProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testTinyTablesFromCmdLine() {
    CmdLineUtil<?, ?> cmd = parseAndCloseNetwork("tinytables", "-b", "4048");
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof TinyTablesProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadProtocolSuiteFromCmdLine() {
    parseIncorrectArgs(getArgs(1, "not-a-protocolsuite", "-b", "4048"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArgs() {
    parseIncorrectArgs("-i", "hej");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectPartyId() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "hej");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPartyIdNonPositive() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "-1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectSuite() {
    parseIncorrectArgs("-s", "fail", "-p", "1:localhost:8080", "-i", "1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectPartyFormatTooShort() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost", "-i", "1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectPartyFormatTooLong() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080:bla:toomuch", "-i", "1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testportNumberFormat() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:fail", "-i", "1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPartyIdNotInList() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "3");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPartyIdIdentical() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-p", "1:localhost:8080", "-i",
        "3");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalBatchSize() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "1", "-b", "fail");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidEvaluationStrat() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "1", "-e", "fail");
  }

  @Test
  public void testCmdLineHelp() {
    @SuppressWarnings("rawtypes")
    CmdLineUtil cmd = new CmdLineUtil<>();
    cmd.addOption(new Option("fancy", "fancy option"));
    CommandLine line = cmd.parse(getArgs(1, "dummybool", "-h"));
    assertNull(line);
    //cmd.closeNetwork();
  }

  //Since network connects immediately, we need it to actually connect to avoid waiting for timeout
  private <ResourcePoolT extends ResourcePool,
      Builder extends ProtocolBuilder> CmdLineUtil<ResourcePoolT, Builder>
  parseAndCloseNetwork(String protocolSuite, String... addedOptions) {

    Thread t1 = new Thread(
        () -> {
          CmdLineUtil<ResourcePoolT, Builder> cmd = new CmdLineUtil<>();
          cmd.parse(getArgs(2, protocolSuite, addedOptions));
          try {
            // Make sure the network is initialized in this thread.
            cmd.getNetwork();
            Thread.sleep(50);
            cmd.closeNetwork();
          } catch (InterruptedException | IOException e) {
            logger.warn("Exception in thread", e);
          }
        });

    t1.start();
    CmdLineUtil<ResourcePoolT, Builder> cmd = new CmdLineUtil<>();
    cmd.parse(getArgs(1, protocolSuite, addedOptions));
    Network network = cmd.getNetwork();
    assertTrue(network instanceof NetworkLoggingDecorator
        || network instanceof CloseableNetwork);
    assertSame(network, cmd.getNetwork());
    try {
      cmd.closeNetwork();
    } catch (IOException e) {
      logger.warn("Exception in network close", e);
    }
    try {
      t1.join(1000);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while waiting for thread", e);
    }
    return cmd;
  }

  private String[] getArgs(int partyId, String protocolSuite, String... addedOptions) {
    List<String> defaultArgs = new ArrayList<>(
        Arrays.asList(
            "-e", "SEQUENTIAL_BATCHED",
            "-i", "" + partyId,
            "-p", "1:localhost:8081",
            "-p", "2:localhost:8082",
            "-s", protocolSuite)
    );
    defaultArgs.addAll(Arrays.asList(addedOptions));
    return defaultArgs.toArray(new String[]{});
  }

  private void parseIncorrectArgs(String... args) {

    @SuppressWarnings("rawtypes")
    CmdLineUtil cmd = new CmdLineUtil<>();
    cmd.parse(args);
  }
}
