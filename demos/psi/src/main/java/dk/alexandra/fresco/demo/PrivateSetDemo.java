/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.demo.helpers.ResourcePoolHelper;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;


/**
 * This demonstrates how to the private set intersection problem can be solved.
 *
 * It is designed for two players and requires a protocol suite that supports basic logic
 * operations.
 *
 * Both players input a secret 128-bit AES key (as a 32 char hex string) as well as a list of secret
 * integers. The two AES keys are xored together using 2PC. The resulting key is then used to
 * compute AES encryptions of the two lists of integers. The output (to both players) is the
 * resulting AES encryptions of the lists of integers under the xored AES key. Note that the output
 * is a single list, with the first half being encryptions of P1's inputs and the second half being
 * encryptions of P2's inputs. The players are themselves responsible for computing the actual
 * intersection.
 *
 * Suppose we have two players. P1 has the key 000102030405060708090a0b0c0d0e0f and a list of
 * integers, {1,3,66,1123} and P2 has the key 00112233445566778899aabbccddeeff and a list of
 * integers {2,66,112,1123}. They both want to know if there is an overlap in their lists of
 * integers, but they do not want to reveal the lists to each other.
 *
 * The two players can then run this application with these parameters:
 *
 * P1: $ java -jar privateset.jar -i2 -s dummy -p1:localhost:9994 -p2:localhost:9292
 * -key:000102030405060708090a0b0c0d0e0f -in1,3,66,1123
 *
 * P2: $ java -jar privateset.jar -i1 -s dummy -p1:localhost:9994 -p2:localhost:9292
 * -key:00112233445566778899aabbccddeeff -in2,66,112,1123
 *
 * This results in this output (at both parties):
 *
 * The resulting ciphertexts are result(0): c5cf1e6421d3302430b4c1e1258e23dc result(1):
 * 2f512cbe2004159f2a9f432aa23074fe result(2): a5bb0723dd40d10189b8e7e1ab383aa1 result(3):
 * 687114568afa5846470e5a5e553c639d result(4): 1f4e1f637a388bcb9984cf3d16c9243e result(5):
 * a5bb0723dd40d10189b8e7e1ab383aa1 result(6): 52cd1dbeeb5f1dce0742aebf285e1472 result(7):
 * 687114568afa5846470e5a5e553c639d
 *
 * The results reveal that P1 indexes 3 and 4 (66 and 1123) also exist in Player 2's input list.
 *
 * OBS: Using the dummy protocol suite is not secure!
 */
public class PrivateSetDemo implements Application<List<List<Boolean>>, ProtocolBuilderBinary> {

  private Boolean[] inKey;
  private int[] inSet;
  private int id;

  private final static int BLOCK_SIZE = 128; // 128 bit AES
  private final static int INPUT_LENGTH = 32; // chars for defining 128 bit in hex

  public PrivateSetDemo(int id, Boolean[] in, int[] set) {
    this.inKey = in;
    this.id = id;
    this.inSet = set;
  }


  /**
   * The main method sets up application specific command line parameters, parses command line
   * arguments. Based on the command line arguments it configures the SCE, instantiates the
   * PrivateSetDemo and runs the PrivateSetDemo on the SCE.
   */
  public static void main(String[] args) {
    CmdLineUtil util = new CmdLineUtil();
    NetworkConfiguration networkConfiguration = null;
    Boolean[] key = null;
    int[] inputs = null;
    try {

      util.addOption(Option.builder("key")
          .desc("The key to use for encryption. " + "A " + INPUT_LENGTH
              + " char hex string. Required for player 1 and 2. "
              + "For both players this is interpreted as the AES key. ")
          .longOpt("key").hasArg().build());

      util.addOption(Option.builder("in")
          .desc("The list of integers to use as input for the set intersection problem. "
              + "A comma separated list of integers. Required for player 1 and 2. "
              + "The lists must be of equal length for each player. ")
          .longOpt("input").hasArg().build());

      CommandLine cmd = util.parse(args);
      networkConfiguration = util.getNetworkConfiguration();

      // Get and validate the AES specific input.
      if (networkConfiguration.getMyId() == 1 || networkConfiguration.getMyId() == 2) {
        if (!cmd.hasOption("in") && !cmd.hasOption("key")) {
          throw new ParseException("Player 1 and 2 must submit inputs and keys");
        } else {
          if (cmd.getOptionValue("key").length() != INPUT_LENGTH) {
            throw new IllegalArgumentException(
                "bad key hex string: must be hex string of length " + INPUT_LENGTH);
          }
          key = ByteArithmetic.toBoolean(cmd.getOptionValue("key"));

          for (Option o : cmd.getOptions()) {
            System.out.println("option: " + o.getValue());
          }
          inputs = arrayFromString(cmd.getOptionValue("in"));

        }
      } else {
        if (cmd.hasOption("in")) {
          throw new ParseException("Only player 1 and 2 should submit input");
        }
      }

    } catch (ParseException | IllegalArgumentException e) {
      System.out.println("Error: " + e);
      System.out.println();
      util.displayHelp();
      System.exit(-1);
    }

    // Do the secure computation using config from property files.
    PrivateSetDemo privateSetDemo = new PrivateSetDemo(networkConfiguration.getMyId(), key, inputs);
    ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> psConf = util.getProtocolSuite();
    SecureComputationEngine<ResourcePoolImpl, ProtocolBuilderBinary> sce =
        new SecureComputationEngineImpl<ResourcePoolImpl, ProtocolBuilderBinary>(psConf,
            util.getEvaluator());

    List<List<Boolean>> psiResult = null;
    try {
      ResourcePoolImpl resourcePool = ResourcePoolHelper.createResourcePool(psConf,
          util.getNetworkStrategy(), networkConfiguration);
      psiResult = sce.runApplication(privateSetDemo, resourcePool);
    } catch (Exception e) {
      System.out.println("Error while doing MPC: " + e.getMessage());
      System.exit(-1);
    }

    // Print result.
    System.out.println("The resulting ciphertexts are:");
    boolean[][] res = new boolean[psiResult.size()][BLOCK_SIZE];
    for (int j = 0; j < psiResult.size(); j++) {
      for (int i = 0; i < BLOCK_SIZE; i++) {
        res[j][i] = psiResult.get(j).get(i);
      }
      System.out.println("result(" + j + "): " + ByteArithmetic.toHex(res[j]));
    }

  }

  /**
   * Small helper for parsing inputs.
   */
  private static int[] arrayFromString(String input) {
    String[] strings = input.split(",");
    int[] output = new int[strings.length];
    for (int i = 0; i < strings.length; i++) {
      output[i] = Integer.parseInt(strings[i]);
    }
    return output;
  }


  @Override
  public Computation<List<List<Boolean>>> prepareApplication(ProtocolBuilderBinary producer) {
    return producer.seq(seq -> {
      BinaryBuilder bin = seq.binary();

      List<Computation<SBool>> key1Inputs = new ArrayList<>();
      List<Computation<SBool>> key2Inputs = new ArrayList<>();
      if (this.id == 1) {
        for (boolean b : inKey) {
          key1Inputs.add(bin.input(b, 1));
          key2Inputs.add(bin.input(false, 2));
        }
      } else {
        // Receive inputs
        for (boolean b : inKey) {
          key1Inputs.add(bin.input(false, 1));
          key2Inputs.add(bin.input(b, 2));
        }
      }

      List<Computation<SBool>> commonKey = new ArrayList<>();
      for (int i = 0; i < key1Inputs.size(); i++) {
        commonKey.add(bin.xor(key1Inputs.get(i), key2Inputs.get(i)));
      }

      List<List<Computation<SBool>>> set1 = new ArrayList<>();
      List<List<Computation<SBool>>> set2 = new ArrayList<>();
      // Handle input lists
      int offset = this.inSet.length;
      for (int i = 0; i < offset; i++) {
        BitSet bits = ByteArithmetic.intToBitSet(this.inSet[i]);
        set1.add(new ArrayList<>());
        set2.add(new ArrayList<>());
        for (int j = 0; j < BLOCK_SIZE; j++) {
          if (this.id == 1) {
            set1.get(i).add(bin.input(bits.get(BLOCK_SIZE - 1 - j), 1));
            set2.get(i).add(bin.input(false, 2));
          } else {
            set1.get(i).add(bin.input(false, 1));
            set2.get(i).add(bin.input(bits.get(BLOCK_SIZE - 1 - j), 2));
          }
        }
      }
      PSIInputs inputs = new PSIInputs(set1, set2, commonKey);
      return () -> inputs;
    }).par((inputs, par) -> {
      List<Computation<List<SBool>>> aesResults = new ArrayList<>();
      for (List<Computation<SBool>> set : inputs.set1) {
        aesResults.add(par.bristol().AES(set, inputs.commonKey));
      }
      for (List<Computation<SBool>> set : inputs.set2) {
        aesResults.add(par.bristol().AES(set, inputs.commonKey));
      }
      return () -> aesResults;
    }).seq((aesResults, seq) -> {
      List<List<SBool>> res =
          aesResults.stream().map(Computation::out).collect(Collectors.toList());
      List<List<Computation<Boolean>>> output = new ArrayList<>();
      for (List<SBool> bs : res) {
        List<Computation<Boolean>> innerOut = new ArrayList<>();
        output.add(innerOut);
        for (SBool b : bs) {
          innerOut.add(seq.binary().open(b));
        }
      }
      return () -> output;
    }).seq((output, seq) -> {
      List<List<Boolean>> outs = output.stream()
          .map(row -> row.stream().map(Computation::out).collect(Collectors.toList()))
          .collect(Collectors.toList());
      return () -> outs;
    });
  }

  private static class PSIInputs implements Computation<PSIInputs> {

    private List<List<Computation<SBool>>> set1, set2;
    private List<Computation<SBool>> commonKey;

    public PSIInputs(List<List<Computation<SBool>>> set1, List<List<Computation<SBool>>> set2,
        List<Computation<SBool>> commonKey) {
      super();
      this.set1 = set1;
      this.set2 = set2;
      this.commonKey = commonKey;
    }

    @Override
    public PSIInputs out() {
      return this;
    }
  }

}
