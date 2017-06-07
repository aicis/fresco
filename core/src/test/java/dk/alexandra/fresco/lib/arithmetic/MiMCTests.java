/*******************************************************************************
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
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.value.KnownSIntProtocol;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.SymmetricEncryptionBuilder;
import java.math.BigInteger;
import org.junit.Assert;

public class MiMCTests {

  /*
   * Note: This unit test is a rather ugly workaround for the following issue:
   * MiMC encryption is deterministic, however its results depend on the modulus
   * used by the backend arithmetic suite. So in order to assert
   * that a call to the encryption functionality always produces the same result
   * is to ensure that the modulus we use is the one we expect to see. I put in
   * an explicit assertion on the modulus because each suite that provides
   * concrete implementations for this test will do its own set up and if the
   * modulus is not set correctly this test will fail (rather mysteriously).
   */
  public static class TestMiMCEncryptsDeterministically extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {

      abstract class MyTestApplication extends TestApplication {

        private static final long serialVersionUID = 1L;
        private BigInteger modulus;

        public BigInteger getModulus() {
          return modulus;
        }

        public void setModulus(BigInteger modulus) {
          this.modulus = modulus;
        }

      }

      return new TestThread() {
        @Override
        public void test() throws Exception {
          MyTestApplication app = new MyTestApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory factory) {
              BasicNumericFactory bnf = (BasicNumericFactory) factory;
              this.setModulus(bnf.getModulus());
              OmniBuilder builder = new OmniBuilder(factory);
              SymmetricEncryptionBuilder seb = builder.getSymmetricEncryptionBuilder();
              NumericIOBuilder niob = builder.getNumericIOBuilder();

              SInt mimcKey = niob.input(BigInteger.valueOf(527618), 2);
              SInt plainText = niob.input(BigInteger.valueOf(10), 1);

              SInt cipherText = seb.mimcEncrypt(plainText, mimcKey);
              OInt cipherTextOpen = niob.output(cipherText);

              this.outputs = new OInt[]{cipherTextOpen};
              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          BigInteger expectedModulus = new BigInteger(
              "2582249878086908589655919172003011874329705792829223512830659356540647622016841194629645353280137831435903171972747493557");
          Assert.assertEquals(expectedModulus, app.getModulus());
          BigInteger expectedCipherText = new BigInteger(
              "10388336824440235723309131431891968131690383663436711590309818298349333623568340591094832870178074855376232596303647115");
          Assert.assertEquals(expectedCipherText, app.getOutputs()[0].getValue());

          secureComputationEngine.shutdownSCE();
        }
      };
    }
  }

  public static class TestMiMCEncSameEnc extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory factory) {
              OmniBuilder builder = new OmniBuilder(factory);
              SInt k = builder.getNumericIOBuilder().input(BigInteger.valueOf(527618), 2);
              SInt x = builder.getNumericIOBuilder().input(BigInteger.valueOf(10), 1);

              SInt encX1 = builder.getSymmetricEncryptionBuilder().mimcEncrypt(x, k);
              SInt encX2 = builder.getSymmetricEncryptionBuilder().mimcEncrypt(x, k);

              OInt out1 = builder.getNumericIOBuilder().output(encX1);
              OInt out2 = builder.getNumericIOBuilder().output(encX2);

              this.outputs = new OInt[]{out1, out2};
              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          Assert.assertEquals(app.getOutputs()[0].getValue(), app.getOutputs()[1].getValue());
          secureComputationEngine.shutdownSCE();
        }
      };
    }
  }

  public static class TestMiMCDifferentPlainTexts extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory factory) {
              OmniBuilder builder = new OmniBuilder(factory);
              SymmetricEncryptionBuilder seb = builder.getSymmetricEncryptionBuilder();
              NumericIOBuilder niob = builder.getNumericIOBuilder();

              SInt mimcKey = niob.input(BigInteger.valueOf(527618), 2);

              SInt plainTextA = niob.input(BigInteger.valueOf(10), 1);
              SInt plainTextB = niob.input(BigInteger.valueOf(11), 1);

              SInt cipherTextA = seb.mimcEncrypt(plainTextA, mimcKey);
              SInt cipherTextB = seb.mimcEncrypt(plainTextB, mimcKey);

              OInt cipherTextAOpen = builder.getNumericIOBuilder().output(cipherTextA);
              OInt cipherTextBOpen = builder.getNumericIOBuilder().output(cipherTextB);

              this.outputs = new OInt[]{cipherTextAOpen, cipherTextBOpen};
              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          Assert.assertNotEquals(app.getOutputs()[0].getValue(), app.getOutputs()[1].getValue());
          secureComputationEngine.shutdownSCE();
        }
      };
    }
  }

  public static class TestMiMCEncDec extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          BigInteger x_big = BigInteger.valueOf(10);
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                ProtocolFactory factory) {
              BasicNumericFactory fac = (BasicNumericFactory) factory;
              SymmetricEncryptionBuilder symBuilder = new SymmetricEncryptionBuilder(fac);
              SInt k = fac.getSInt();
              KnownSIntProtocol knownKProtocol = fac.getSInt(20, k);

              SInt x = fac.getSInt();
              KnownSIntProtocol knownXProtocol = fac.getSInt(x_big, x);
              symBuilder.addProtocolProducer(SingleProtocolProducer.wrap(knownKProtocol));
              symBuilder.addProtocolProducer(SingleProtocolProducer.wrap(knownXProtocol));
              SInt encX = symBuilder.mimcEncrypt(x, k);
              SInt decX = symBuilder.mimcDecrypt(encX, k);

              OInt outEnc = fac.getOInt();
              OInt out1 = fac.getOInt();
              symBuilder.addProtocolProducer(
                  SingleProtocolProducer.wrap(fac.getOpenProtocol(encX, outEnc)));
              symBuilder.addProtocolProducer(
                  SingleProtocolProducer.wrap(fac.getOpenProtocol(decX, out1)));

              this.outputs = new OInt[]{outEnc, out1};
              return symBuilder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);
          Assert.assertEquals(x_big, app.getOutputs()[1].getValue());
        }
      };
    }
  }
}
