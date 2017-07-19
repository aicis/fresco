/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
import dk.alexandra.fresco.demo.helpers.ResourcePoolHelper;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
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
import dk.alexandra.fresco.suite.ProtocolSuite;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;


/**
 * This demonstrates how to aggregate generic protocols to form an application.
 *
 * It is designed for two players and requires a protocol suite that supports basic logic
 * operations.
 *
 * Player 1 inputs a secret 128-bit AES key (as a 32 char hex string), player 2 inputs a secret
 * plaintext (also a 32 char hex string). The output (to both players) is the resulting AES
 * encryption of the plaintext under the given AES key.
 *
 * Suppose we have two players. P2 has the plaintext block 000102030405060708090a0b0c0d0e0f and P1
 * has the key 00112233445566778899aabbccddeeff. They both want to know the ciphertext, i.e., the
 * result of encrypting 000102030405060708090a0b0c0d0e0f under the key
 * 00112233445566778899aabbccddeeff, but they do not want to reveal the key and the plaintext to
 * each other.
 *
 * The two players can then run this application with these parameters:
 *
 * P1: $ java -jar aes.jar -i1 -s dummy -p1:localhost:9292 -p2:localhost:9994 -in
 * 000102030405060708090a0b0c0d0e0f
 *
 * P2: $ java -jar aes.jar -i2 -s dummy -p1:localhost:9292 -p2:localhost:9994 -in
 * 00112233445566778899aabbccddeeff
 *
 * This results in this output (at both parties):
 *
 * The resulting ciphertext is: 69c4e0d86a7b0430d8cdb78070b4c55a
 *
 * OBS: Using the dummy protocol suite is not secure!
 */
public class AESDemo extends DemoBinaryApplication<OBool[]> {

  /**
   * Applications can be uploaded to fresco dynamically and are therefore
   * Serializable's. This means that each application must have a unique
   * serialVersionUID.
   */


  private boolean[] in;
  private int id;

  public OBool[] result;

  private final static int BLOCK_SIZE = 128; // 128 bit AES
  private final static int INPUT_LENGTH = 32; // chars for defining 128 bit in hex

  public AESDemo(int id, boolean[] in) {
    this.in = in;
    this.id = id;
  }


  /**
   * The main method sets up application specific command line parameters,
   * parses command line arguments. Based on the command line arguments it
   * configures the SCE, instantiates the TestAESDemo and runs the TestAESDemo on the
   * SCE.
   */
  public static void main(String[] args) {
    CmdLineUtil util = new CmdLineUtil();
    SCEConfiguration sceConf = null;
    boolean[] input = null;
    try {

      util.addOption(Option.builder("in")
          .desc("The input to use for encryption. "
              + "A " + INPUT_LENGTH + " char hex string. Required for player 1 and 2. "
              + "For player 1 this is interpreted as the AES key. "
              + "For player 2 this is interpreted as the plaintext block to encrypt.")
          .longOpt("input")
          .hasArg()
          .build());

      CommandLine cmd = util.parse(args);
      sceConf = util.getSCEConfiguration();

      // Get and validate the AES specific input.
      if (sceConf.getMyId() == 1 || sceConf.getMyId() == 2) {
        if (!cmd.hasOption("in")) {
          throw new ParseException("Player 1 and 2 must submit input");
        } else {
          if (cmd.getOptionValue("in").length() != INPUT_LENGTH) {
            throw new IllegalArgumentException(
                "bad input hex string: must be hex string of length " + INPUT_LENGTH);
          }
          input = ByteArithmetic.toBoolean(cmd.getOptionValue("in"));
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
    AESDemo aes = new AESDemo(sceConf.getMyId(), input);
    ProtocolSuite<?, ?> ps = util
        .getProtocolSuite();
    SecureComputationEngine sce = new SecureComputationEngineImpl(ps,
        sceConf.getEvaluator(), sceConf.getLogLevel());

    try {
      sce.runApplication(aes, ResourcePoolHelper.createResourcePool(sceConf, ps));
    } catch (Exception e) {
      System.out.println("Error while doing MPC: " + e.getMessage());
      System.exit(-1);
    } finally {
      ResourcePoolHelper.shutdown();
    }

    // Print result.
    boolean[] res = new boolean[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      res[i] = aes.result[i].getValue();
    }
    System.out.println("The resulting ciphertext is: " + ByteArithmetic.toHex(res));
    
  }


  /**
   * This is where the actual computation is defined. The method builds up a
   * protocol that does one evaluation of an AES block encryption. This
   * involves protocols for 'closing' the plaintext and key, i.e., converting
   * them from something that one of the players knows to secret values. It
   * also involves a protocol for AES that works on secret values, and
   * protocols for opening up the resulting ciphertext.
   *
   * The final protocol is build from smaller protocols using the
   * ParallelProtocolProducer and SequentialProtocolProducer. The open and
   * closed values (OBool and SBool) are used to 'glue' the subprotocols
   * together.
   * @param producer
   */
  @Override
  public ProtocolProducer prepareApplication(BuilderFactory producer) {

    if (!(producer.getProtocolFactory() instanceof BasicLogicFactory)) {
      throw new MPCException(producer.getClass().getSimpleName()
          + " is not a BasicLogicFactory. This AES demo requires a protocol suite that implements the BasicLogicFactory.");
    }
    BasicLogicFactory boolFactory = (BasicLogicFactory) producer.getProtocolFactory();

    OBool[] plainOpen = new OBool[BLOCK_SIZE];
    OBool[] keyOpen = new OBool[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      keyOpen[i] = boolFactory.getOBool();
      plainOpen[i] = boolFactory.getOBool();
      if (this.id == 1) {
        keyOpen[i].setValue(this.in[i]);
      } else if (this.id == 2) {
        plainOpen[i].setValue(this.in[i]);
      } else {
        // OK, there might be more players, but they don't have input.
      }
    }

    // Establish some secure values.
    SBool[] keyClosed = boolFactory.getSBools(BLOCK_SIZE);
    SBool[] plainClosed = boolFactory.getSBools(BLOCK_SIZE);
    SBool[] outClosed = boolFactory.getSBools(BLOCK_SIZE);

    // Build protocol where player 1 closes his key.
    NativeProtocol[] closeKeyBits = new NativeProtocol[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      closeKeyBits[i] = boolFactory.getCloseProtocol(1, keyOpen[i], keyClosed[i]);
    }
    ProtocolProducer closeKey = new ParallelProtocolProducer(closeKeyBits);

    // Buil protocol where player 2 closes his plaintext.
    NativeProtocol[] closePlainBits = new NativeProtocol[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      closePlainBits[i] = boolFactory.getCloseProtocol(2, plainOpen[i], plainClosed[i]);
    }
    ProtocolProducer closePlain = new ParallelProtocolProducer(closePlainBits);

    // We can close key and plaintext in parallel.
    ProtocolProducer closeKeyAndPlain = new ParallelProtocolProducer(closeKey, closePlain);

    // Build an AES protocol.
    ProtocolProducer doAES = new BristolCryptoFactory(boolFactory)
        .getAesProtocol(plainClosed, keyClosed, outClosed);

    // Create wires that glue together the AES to the following open of the result.
    this.result = boolFactory.getOBools(BLOCK_SIZE);
    this.output = () -> this.result;
    
    // Construct protocol for opening up the result.
    NativeProtocol[] opens = new NativeProtocol[BLOCK_SIZE];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      opens[i] = boolFactory.getOpenProtocol(outClosed[i], result[i]);
    }
    ProtocolProducer openCipher = new ParallelProtocolProducer(opens);

    // First we close key and plaintext, then we do the AES, then we open the resulting ciphertext.
    return new SequentialProtocolProducer(closeKeyAndPlain, doAES,
        openCipher);

  }


}
