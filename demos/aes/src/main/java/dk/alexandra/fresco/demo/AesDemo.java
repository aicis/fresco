package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;


/**
 * This demonstrates how to aggregate generic protocols to form an application.
 *
 * <p>It is designed for two players and requires a protocol suite that supports basic logic
 * operations.
 * 
 * <p>Player 1 inputs a secret 128-bit AES key (as a 32 char hex string), player 2 inputs a secret
 * plaintext (also a 32 char hex string). The output (to both players) is the resulting AES
 * encryption of the plaintext under the given AES key.
 * 
 * <p>Suppose we have two players. P2 has the plaintext block 000102030405060708090a0b0c0d0e0f
 * and P1 has the key 00112233445566778899aabbccddeeff. They both want to know the ciphertext,
 * i.e., the result of encrypting 000102030405060708090a0b0c0d0e0f under the key
 * 00112233445566778899aabbccddeeff, but they do not want to reveal the key and the plaintext to
 * each other.
 * 
 * <p>The two players can then run this application with these parameters:
 * 
 * <p>P1: $ java -jar aes.jar -i1 -s dummy -p1:localhost:9292 -p2:localhost:9994 -in
 * 000102030405060708090a0b0c0d0e0f
 * 
 * <p>P2: $ java -jar aes.jar -i2 -s dummy -p1:localhost:9292 -p2:localhost:9994 -in
 * 00112233445566778899aabbccddeeff
 * 
 * <p>This results in this output (at both parties):
 * 
 * <p>The resulting ciphertext is: 69c4e0d86a7b0430d8cdb78070b4c55a
 * 
 * <p>OBS: Using the dummy protocol suite is not secure!
 */
public class AesDemo implements Application<List<Boolean>, ProtocolBuilderBinary> {

  private Boolean[] in;
  private int id;

  private static final int BLOCK_SIZE = 128; // 128 bit AES
  private static final int INPUT_LENGTH = 32; // chars for defining 128 bit in hex

  public AesDemo(int id, Boolean[] in) {
    this.in = in;
    this.id = id;
  }


  /**
   * The main method sets up application specific command line parameters, parses command line
   * arguments. Based on the command line arguments it configures the SCE, instantiates the
   * TestAESDemo and runs the TestAESDemo on the SCE.
   */
  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderBinary> util = new CmdLineUtil<>();

    util.addOption(Option.builder("in")
        .desc("The input to use for encryption. " + "A " + INPUT_LENGTH
            + " char hex string. Required for player 1 and 2. "
            + "For player 1 this is interpreted as the AES key. "
            + "For player 2 this is interpreted as the plaintext block to encrypt.")
        .longOpt("input").hasArg().build());
    
    CommandLine cmd = util.parse(args);
    
    // Get and validate the AES specific input.
    Boolean[] input = null;
    int myId = util.getNetworkConfiguration().getMyId();
    if (myId == 1 || myId == 2) {

      if (!cmd.hasOption("in")) {
        throw new IllegalArgumentException("Player 1 and 2 must submit input");
      } else {
        if (cmd.getOptionValue("in").length() != INPUT_LENGTH) {
          throw new IllegalArgumentException(
              "bad input hex string: must be hex string of length " + INPUT_LENGTH);
        }
        input = ByteAndBitConverter.toBoolean(cmd.getOptionValue("in"));
      }
    } else {
      if (cmd.hasOption("in")) {
        throw new IllegalArgumentException("Only player 1 and 2 should submit input");
      }
      input = ByteAndBitConverter.toBoolean("00000000000000000000000000000000");
    }

    
    ProtocolSuite<ResourcePoolT, ProtocolBuilderBinary> psConf = util.getProtocolSuite();

    SecureComputationEngine<ResourcePoolT, ProtocolBuilderBinary> sce =
        new SecureComputationEngineImpl<>(psConf, util.getEvaluator());

    ResourcePoolT resourcePool = util.getResourcePool();
    util.startNetwork();
    AesDemo aes = new AesDemo(util.getNetworkConfiguration().getMyId(), input);
    List<Boolean> aesResult = sce.runApplication(aes, resourcePool, util.getNetwork());
    
    util.closeNetwork();
    sce.shutdownSCE();

    // Print result.
    boolean[] res = new boolean[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      res[i] = aesResult.get(i);
    }
    System.out.println("The resulting ciphertext is: " + ByteAndBitConverter.toHex(res));

  }
  
  @Override
  public DRes<List<Boolean>> buildComputation(ProtocolBuilderBinary producer) {
    return producer.seq(seq -> {
      Binary bin = seq.binary();
      List<DRes<SBool>> keyInputs = new ArrayList<>();
      List<DRes<SBool>> plainInputs = new ArrayList<>();
      if (this.id == 1) {
        for (boolean b : in) {
          keyInputs.add(bin.input(b, 1));
          plainInputs.add(bin.input(false, 2));
        }
      } else {
        // Receive inputs
        for (boolean b : in) {
          keyInputs.add(bin.input(false, 1));
          plainInputs.add(bin.input(b, 2));
        }
      }
      DRes<List<SBool>> res = seq.bristol().AES(plainInputs, keyInputs);
      return res;
    }).seq((seq, aesRes) -> {
      List<DRes<Boolean>> outs = new ArrayList<>();
      for (SBool toOpen : aesRes) {
        outs.add(seq.binary().open(toOpen));
      }
      return () -> outs;
    }).seq((seq, opened) -> () -> opened.stream().map(DRes::out).collect(Collectors.toList()));
  }


}
