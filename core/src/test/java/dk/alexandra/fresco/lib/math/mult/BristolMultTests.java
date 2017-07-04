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
package dk.alexandra.fresco.lib.math.mult;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.network.NetworkCreator;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.crypto.BristolCryptoFactory;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuit;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;


/**
 * Some generic tests for basic crypto primitives a la AES and SHA1.
 *
 * Can be used to test any protocol suite that supports BasicLogicFactory.
 */
public class BristolMultTests {


  /**
   * Convert hex string to boolean array.
   *
   * // 1 --> true, 0 --> false
   */
  private static boolean[] toBoolean(String hex) throws IllegalArgumentException {
    if (hex.length() % 2 != 0) {
      throw new IllegalArgumentException("Illegal hex string");
    }
    boolean[] res = new boolean[hex.length() * 4];
    for (int i = 0; i < hex.length() / 2; i++) {
      String sub = hex.substring(2 * i, 2 * i + 2);
      int value = Integer.parseInt(sub, 16);
      int numOfBits = 8;
      for (int j = 0; j < numOfBits; j++) {
        boolean val = (value & 1 << j) != 0;
        res[8 * i + (numOfBits - j - 1)] = val;
      }
    }
    return res;
  }


  @Test
  public void testToBoolean() throws Exception {
    boolean[] res = toBoolean("2b7e151628aed2a6abf7158809cf4f3c");
    assertTrue(
        Arrays.equals(
            new boolean[]{false, false, true, false, true, false, true, true, false, true, true,
                true}, Arrays.copyOf(res, 12)));
  }


  /**
   * Testing circuit for mult of two 32-bit numbers.
   *
   * TODO: Include more test vectors. Oddly enough, 1x1=2 :-)
   */
  public static class Mult32x32Test extends TestThreadFactory {

    private boolean assertAsExpected;

    public Mult32x32Test(boolean assertAsExpected) {
      this.assertAsExpected = assertAsExpected;
    }
    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        SBool[] in1, in2, out;
        OBool[] openedOut;

        String inv1 = "00000000";
        String inv2 = "00000000";
        String outv = "0000000000000000";

        @Override
        public void test() throws Exception {
          TestBoolApplication md5App = new TestBoolApplication() {


            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory bool = (AbstractBinaryFactory) provider;
              BasicLogicBuilder builder = new BasicLogicBuilder(bool);

              boolean[] in1_val = toBoolean(inv1);
              in1 = builder.knownSBool(in1_val);
              boolean[] in2_val = toBoolean(inv2);
              in2 = builder.knownSBool(in2_val);

              out = bool.getSBools(64);

              // Create mult circuit.
              BristolCryptoFactory multFac = new BristolCryptoFactory(bool);
              BristolCircuit mult = multFac.getMult32x32Circuit(in1, in2, out);
              builder.addProtocolProducer(mult);
              openedOut = builder.output(out);

              // Create circuits for opening result of 32x32 bit mult.
              /*
              ProtocolProducer[] opens = new ProtocolProducer[out.length];
							openedOut = new OBool[out.length];
							for (int i=0; i<out.length; i++) {
								openedOut[i] = bool.getOBool();
								opens[i] = bool.getOpenProtocol(out[i], openedOut[i]);
							}
							ProtocolProducer open_all = new ParallelProtocolProducer(opens);
							*/
              return builder.getProtocol();
            }
          };

          secureComputationEngine
              .runApplication(md5App, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          if (!assertAsExpected) {
            return;
          }
          boolean[] expected = toBoolean(outv);
          boolean[] actual = new boolean[out.length];
          for (int i = 0; i < out.length; i++) {
            actual[i] = openedOut[i].getValue();
          }

          //					System.out.println("IN1        : " + Arrays.toString(toBoolean(inv1)));
          //					System.out.println("IN2        : " + Arrays.toString(toBoolean(inv2)));
          //					System.out.println("EXPECTED   : " + Arrays.toString(expected));
          //					System.out.println("ACTUAL     : " + Arrays.toString(actual));

          Assert.assertTrue(Arrays.equals(expected, actual));
        }
      };
    }
  }


}
