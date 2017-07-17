/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.demo.helpers.DemoBinaryApplication;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.crypto.BristolCryptoFactory;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import java.util.BitSet;
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
public class PrivateSetDemo extends DemoBinaryApplication<OBool[][]> {

  private boolean[] inKey;
  private int[] inSet;
  private int id;

  public OBool[][] result;

  private final static int BLOCK_SIZE = 128; // 128 bit AES
  private final static int INPUT_LENGTH = 32; // chars for defining 128 bit in hex

  public PrivateSetDemo(int id, boolean[] in, int[] set) {
    this.inKey = in;
    this.id = id;
    this.inSet = set;
  }


  /**
   * The main method sets up application specific command line parameters,
   * parses command line arguments. Based on the command line arguments it
   * configures the SCE, instantiates the PrivateSetDemo and runs the PrivateSetDemo on the
   * SCE.
   */
  public static void main(String[] args) {
    CmdLineUtil util = new CmdLineUtil();
    SCEConfiguration sceConf = null;
    boolean[] key = null;
    int[] inputs = null;
    try {

      util.addOption(Option.builder("key")
          .desc("The key to use for encryption. "
              + "A " + INPUT_LENGTH + " char hex string. Required for player 1 and 2. "
              + "For both players this is interpreted as the AES key. ")
          .longOpt("key")
          .hasArg()
          .build());

      util.addOption(Option.builder("in")
          .desc("The list of integers to use as input for the set intersection problem. "
              + "A comma separated list of integers. Required for player 1 and 2. "
              + "The lists must be of equal length for each player. ")
          .longOpt("input")
          .hasArg()
          .build());

      CommandLine cmd = util.parse(args);
      sceConf = util.getSCEConfiguration();

      // Get and validate the AES specific input.
      if (sceConf.getMyId() == 1 || sceConf.getMyId() == 2) {
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
    PrivateSetDemo privateSetDemo = new PrivateSetDemo(sceConf.getMyId(), key, inputs);
    dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration psConf = util
        .getProtocolSuiteConfiguration();
    SecureComputationEngine sce = new SecureComputationEngineImpl(psConf,
        sceConf.getEvaluator(), sceConf.getLogLevel(), sceConf.getMyId());

    try {
      sce.runApplication(privateSetDemo, SecureComputationEngineImpl.createResourcePool(sceConf,
          psConf));
    } catch (Exception e) {
      System.out.println("Error while doing MPC: " + e.getMessage());
      System.exit(-1);
    }

    // Print result.
    System.out.println("The resulting ciphertexts are:");
    boolean[][] res = new boolean[privateSetDemo.result.length][BLOCK_SIZE];
    for (int j = 0; j < privateSetDemo.result.length; j++) {
      for (int i = 0; i < BLOCK_SIZE; i++) {
        res[j][i] = privateSetDemo.result[j][i].getValue();
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


  /**
   * This is where the actual computation is defined. The method builds up a
   * protocol that does one evaluation of an AES block encryption for each
   * of the provided input integers. This
   * involves protocols for 'closing' the plaintexts and keys, i.e., converting
   * them from something that one of the players knows to secret values. It
   * also involves a protocol for AES that works on secret values, and
   * protocols for opening up the resulting ciphertext.
   *
   * The final protocol is build from smaller protocols using the
   * ParallelProtocolProducer and SequentialProtocolProducer. The open and
   * closed values (OBool and SBool) are used to 'glue' the subprotocols
   * together.
   * @param builderFactory
   */
  //May cause problems if more than 2 parties and if both insets are not of
  //Equal length
  @Override
  public ProtocolProducer prepareApplication(BuilderFactory builderFactory) {
    ProtocolFactory producer = builderFactory.getProtocolFactory();
    if (!(producer instanceof BasicLogicFactory)) {
      throw new MPCException(producer.getClass().getSimpleName()
          + " is not a BasicLogicFactory. This Private Set demo requires a protocol suite that implements the BasicLogicFactory.");
    }
    BasicLogicFactory boolFactory = (BasicLogicFactory) producer;

    OBool[] key2Open = new OBool[BLOCK_SIZE];
    OBool[] key1Open = new OBool[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      key1Open[i] = boolFactory.getOBool();
      key2Open[i] = boolFactory.getOBool();
      if (this.id == 1) {
        key1Open[i].setValue(this.inKey[i]);
      } else if (this.id == 2) {
        key2Open[i].setValue(this.inKey[i]);
      } else {
        // OK, there might be more players, but they don't have input.
      }
    }

    // Establish some secure values.
    SBool[] key1Closed = boolFactory.getSBools(BLOCK_SIZE);
    SBool[] key2Closed = boolFactory.getSBools(BLOCK_SIZE);

    // Build protocol where player 1 closes his key.
    NativeProtocol[] closeKey1Bits = new NativeProtocol[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      closeKey1Bits[i] = boolFactory.getCloseProtocol(1, key1Open[i], key1Closed[i]);
    }
    ProtocolProducer closeKey1 = new ParallelProtocolProducer(closeKey1Bits);

    // Build protocol where player 2 closes his key.
    NativeProtocol[] closeKey2Bits = new NativeProtocol[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      closeKey2Bits[i] = boolFactory.getCloseProtocol(2, key2Open[i], key2Closed[i]);
    }
    ProtocolProducer closeKey2 = new ParallelProtocolProducer(closeKey2Bits);

    // We can close both keys in parallel.
    ProtocolProducer closeKeys = new ParallelProtocolProducer(closeKey1, closeKey2);

    // XOR the keys together.
    SBool[] combinedKey = boolFactory.getSBools(BLOCK_SIZE);
    NativeProtocol[] combineKeyBits = new NativeProtocol[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      combineKeyBits[i] = boolFactory.getXorProtocol(key1Closed[i], key2Closed[i], combinedKey[i]);
    }
    ProtocolProducer combineKeys = new ParallelProtocolProducer(combineKeyBits);

    // Initialize various arrays
    OBool[][] inputsOpen = new OBool[2 * this.inSet.length][BLOCK_SIZE]; //Arrays of open inputs
    SBool[][] inputsClosed = new SBool[this.inSet.length * 2][BLOCK_SIZE]; //Arrays of closed inputs
    SBool[][] outClosed = new SBool[this.inSet.length * 2][BLOCK_SIZE]; //Arrays of closed result
    this.result = new OBool[this.inSet.length * 2][]; //Arrays of resulting ciphertexts
    for (int i = 0; i < this.inSet.length * 2; i++) {
      inputsOpen[i] = boolFactory.getOBools(BLOCK_SIZE);
      inputsClosed[i] = boolFactory.getSBools(BLOCK_SIZE);
      outClosed[i] = boolFactory.getSBools(BLOCK_SIZE);
      this.result[i] = boolFactory.getOBools(BLOCK_SIZE);      
    }
    this.output = () -> this.result;

    // Handle input lists
    for (int i = 0; i < this.inSet.length; i++) {
      if (this.id != 1 && this.id != 2) {
        continue;
      }
      int offset = this.inSet.length;
      BitSet bits = ByteArithmetic.intToBitSet(this.inSet[i]);
      for (int j = 0; j < BLOCK_SIZE; j++) {
        if (this.id == 1) {
          inputsOpen[i][j].setValue(bits.get(BLOCK_SIZE - 1 - j));
        } else {
          inputsOpen[i + offset][j].setValue(bits.get(BLOCK_SIZE - 1 - j));
        }
      }
    }

    // Build protocols where the inputs are closed.
    NativeProtocol[][] closeInputBits = new NativeProtocol[this.inSet.length * 2][BLOCK_SIZE];
    for (int j = 0; j < this.inSet.length; j++) {
      for (int i = 0; i < BLOCK_SIZE; i++) {
        closeInputBits[j][i] = boolFactory
            .getCloseProtocol(1, inputsOpen[j][i], inputsClosed[j][i]);
        closeInputBits[j + this.inSet.length][i] = boolFactory
            .getCloseProtocol(2, inputsOpen[j + this.inSet.length][i],
                inputsClosed[j + this.inSet.length][i]);
      }
    }

    //Build the 2*list AES protocols and put the closing protocols into a single producer
    ProtocolProducer[] tmp = new ProtocolProducer[this.inSet.length
        * 2]; //Each protocolproducer closes an input bit string
    ProtocolProducer[] aesProtocols = new ProtocolProducer[this.inSet.length * 2];

    for (int i = 0; i < this.inSet.length * 2; i++) {
      tmp[i] = new ParallelProtocolProducer(closeInputBits[i]);
      aesProtocols[i] = new BristolCryptoFactory(boolFactory)
          .getAesProtocol(inputsClosed[i], combinedKey, outClosed[i]);
    }

    ProtocolProducer closeInputs = new ParallelProtocolProducer(tmp);
    ProtocolProducer compute = new ParallelProtocolProducer(aesProtocols);

    // Construct protocol for opening up the result.
    NativeProtocol[][] opens = new NativeProtocol[this.inSet.length * 2][BLOCK_SIZE];
    ProtocolProducer[] openInputs = new ProtocolProducer[this.inSet.length * 2];
    for (int j = 0; j < this.inSet.length * 2; j++) {
      for (int i = 0; i < BLOCK_SIZE; i++) {
        opens[j][i] = boolFactory.getOpenProtocol(outClosed[j][i], result[j][i]);
      }
      openInputs[j] = new ParallelProtocolProducer(opens[j]);
    }

    ProtocolProducer openCipher = new ParallelProtocolProducer(openInputs);

    // First we close key and plaintext, then we do the AES, then we open the resulting ciphertexts.

    return new SequentialProtocolProducer(closeKeys, combineKeys,
        closeInputs, compute, openCipher);

  }


}
