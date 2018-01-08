package dk.alexandra.fresco.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.junit.Ignore;
import org.junit.Test;

public class TestCmdLineUtil {

  @Test
  public void testDummyBoolFromCmdLine() throws InterruptedException {
    CmdLineUtil<ResourcePoolImpl, ProtocolBuilderBinary> cmd = parseAndCloseNetwork(getArgs(1, "dummybool", "-b", "4048"));
    assertTrue(cmd.getNetwork() instanceof KryoNetNetwork);
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof DummyBooleanProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testDummyBoolFromCmdLineWithLogging() throws InterruptedException {
    CmdLineUtil<ResourcePoolImpl, ProtocolBuilderBinary> cmd = parseAndCloseNetwork(getArgs(1, "dummybool", "-l"));
    assertTrue(cmd.getNetwork() instanceof NetworkLoggingDecorator);
    assertTrue(cmd.getEvaluator() instanceof EvaluatorLoggingDecorator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof DummyBooleanProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testDummyAritmeticFromCmdLine() throws InterruptedException {
    CmdLineUtil<DummyArithmeticResourcePool, ProtocolBuilderNumeric> cmd = parseAndCloseNetwork(getArgs(1, "dummyarithmetic", "-b", "4048"));
    assertTrue(cmd.getNetwork() instanceof KryoNetNetwork);
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof DummyArithmeticProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testSpdzAritmeticDummyFromCmdLine() throws InterruptedException {
    CmdLineUtil<SpdzResourcePool, ProtocolBuilderNumeric> cmd = parseAndCloseNetwork(getArgs(1, "spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=DUMMY"));
    assertTrue(cmd.getNetwork() instanceof KryoNetNetwork);
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof SpdzProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testSpdzBadBitLength() throws InterruptedException {
    parseAndCloseNetwork(getArgs(1, "spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=DUMMY", "-D", "spdz.maxBitLength=1"));
  }
  
  @Ignore
  @Test
  public void testSpdzAritmeticStaticFromCmdLine() throws InterruptedException {
    CmdLineUtil<SpdzResourcePool, ProtocolBuilderNumeric> cmd = 
        parseAndCloseNetwork(getArgs(1, "spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=STATIC"));
    assertTrue(cmd.getNetwork() instanceof KryoNetNetwork);
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof SpdzProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testSpdzAritmeticBadStrategyFromCmdLine() throws InterruptedException {
    parseAndCloseNetwork(getArgs(1, "spdz", "-b", "4048", "-D", "spdz.preprocessingStrategy=NO_STRATEGY"));
  }

  @Test
  public void testTinyTablesPreproFromCmdLine() throws InterruptedException {
    CmdLineUtil<?, ?> cmd = parseAndCloseNetwork(getArgs(1, "tinytablesprepro", "-b", "4048"));
    assertTrue(cmd.getNetwork() instanceof KryoNetNetwork);
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof TinyTablesPreproProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test
  public void testTinyTablesFromCmdLine() throws InterruptedException {
    CmdLineUtil<?, ?> cmd = parseAndCloseNetwork(getArgs(1, "tinytables", "-b", "4048"));
    assertTrue(cmd.getNetwork() instanceof KryoNetNetwork);
    assertTrue(cmd.getEvaluator() instanceof BatchedProtocolEvaluator);
    assertEquals(1, cmd.getNetworkConfiguration().getMyId());
    assertEquals(2, cmd.getNetworkConfiguration().noOfParties());
    assertTrue(cmd.getProtocolSuite() instanceof TinyTablesProtocolSuite);
    assertTrue(cmd.getResourcePool() instanceof ResourcePoolImpl);
    assertTrue(cmd.getSce() instanceof SecureComputationEngineImpl);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testBadProtocolSuiteFromCmdLine() throws InterruptedException {
    parseIncorrectArgs(getArgs(1, "not-a-protocolsuite", "-b", "4048"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIncorrectArgs() {
    parseIncorrectArgs("-i", "hej");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIncorrectPartyId() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "hej");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testPartyIdNonPositive() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "-1");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIncorrectSuite() {
    parseIncorrectArgs("-s", "fail", "-p", "1:localhost:8080", "-i", "1");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIncorrectPartyFormatTooShort() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost", "-i", "1");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIncorrectPartyFormatTooLong() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080:bla:toomuch", "-i", "1");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testportNumberFormat() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:fail", "-i", "1");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testPartyIdNotInList() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "3");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testPartyIdIdentical() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-p", "1:localhost:8080", "-i", "3");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testIllegalBatchSize() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "1", "-b", "fail");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidEvaluationStrat() {
    parseIncorrectArgs("-s", "dummybool", "-p", "1:localhost:8080", "-i", "1", "-e", "fail");
  }

  @Test
  public void testCmdLineHelp() throws IOException {
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
  parseAndCloseNetwork(String[] mainArgs) {

    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        CmdLineUtil<ResourcePoolT, Builder> cmd = new CmdLineUtil<>();
        cmd.parse(getArgs(2, "dummybool"));
        cmd.startNetwork();
        try {
          this.wait(50);
          cmd.closeNetwork();
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
      }
    });

    t1.start();
    CmdLineUtil<ResourcePoolT, Builder> cmd = new CmdLineUtil<>();
    cmd.parse(mainArgs);
    cmd.startNetwork();
    try {
      cmd.closeNetwork();
    } catch (IOException e) {
    }
    try {
      t1.join(1000);
    } catch (InterruptedException e) {
    }
    return cmd;
  }

  private String[] getArgs(int partyId, String protocolSuite, String...addedOptions) {
    List<String> defaultArgs = new ArrayList<>();
    defaultArgs.addAll(Arrays.asList(new String[] { "-e", "SEQUENTIAL_BATCHED", "-i", "" + partyId, "-p", "1:localhost:8081:secret",
        "-p", "2:localhost:8082:secret", "-s", protocolSuite }));
    if(addedOptions.length > 0) {
      for(String opt : addedOptions) {
        defaultArgs.add(opt);
      }
    }
    return defaultArgs.toArray(new String[] {});
  }

  private void parseIncorrectArgs(String...args) {
    
    @SuppressWarnings("rawtypes")
    CmdLineUtil cmd = new CmdLineUtil<>();
    cmd.parse(args);
  }

}
