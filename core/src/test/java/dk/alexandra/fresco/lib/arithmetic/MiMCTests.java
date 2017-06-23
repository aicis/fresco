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
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCDecryptionProtocolNaiveImpl4;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCEncryptionProtocolNaiveImpl4;
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
  public static class TestMiMCEncryptsDeterministically<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT> {

    @Override
    public TestThread<ResourcePoolT> next(TestThreadConfiguration<ResourcePoolT> conf) {

      abstract class MyTestApplication extends TestApplication {

        private BigInteger modulus;

        BigInteger getModulus() {
          return modulus;
        }

        void setModulus(BigInteger modulus) {
          this.modulus = modulus;
        }

      }

      return new TestThread<ResourcePoolT>() {

        private Computation<OInt> result;

        @Override
        public void test() throws Exception {
          MyTestApplication app = new MyTestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    setModulus(builder.getBasicNumericFactory().getModulus());

                    SIntFactory intFactory = builder.getSIntFactory();
                    SInt encryptionKey = intFactory.getSInt(BigInteger.valueOf(527618));
                    SInt plainText = intFactory.getSInt(BigInteger.valueOf(10));
                    Computation<SInt> cipherText = builder.createSequentialSub(
                        new MiMCEncryptionProtocolNaiveImpl4(plainText, encryptionKey)
                    );
                    result = builder.createOpenBuilder().open(cipherText);
                  }).build();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          BigInteger expectedModulus = new BigInteger(
              "2582249878086908589655919172003011874329705792829223512830659356540647622016841194629645353280137831435903171972747493557");
          Assert.assertEquals(expectedModulus, app.getModulus());
          BigInteger expectedCipherText = new BigInteger(
              "10388336824440235723309131431891968131690383663436711590309818298349333623568340591094832870178074855376232596303647115");
          Assert.assertEquals(expectedCipherText, result.out().getValue());
        }
      };
    }
  }

  public static class TestMiMCEncSameEnc<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT> {

    @Override
    public TestThread<ResourcePoolT> next(TestThreadConfiguration<ResourcePoolT> conf) {
      return new TestThread<ResourcePoolT>() {
        private Computation<OInt> result2;
        private Computation<OInt> result1;

        @Override
        public void test() throws Exception {

          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    SIntFactory intFactory = builder.getSIntFactory();
                    SInt encryptionKey = intFactory.getSInt(BigInteger.valueOf(527618));
                    SInt plainText = intFactory.getSInt(BigInteger.valueOf(10));
                    Computation<SInt> cipherText = builder.createSequentialSub(
                        new MiMCEncryptionProtocolNaiveImpl4(plainText, encryptionKey)
                    );
                    Computation<SInt> cipherText2 = builder.createSequentialSub(
                        new MiMCEncryptionProtocolNaiveImpl4(plainText, encryptionKey)
                    );
                    result1 = builder.createOpenBuilder().open(cipherText);
                    result2 = builder.createOpenBuilder().open(cipherText2);
                  }).build();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(result1.out().getValue(), result2.out().getValue());
        }
      };
    }
  }

  public static class TestMiMCDifferentPlainTexts<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT> {

    @Override
    public TestThread<ResourcePoolT> next(TestThreadConfiguration<ResourcePoolT> conf) {
      return new TestThread<ResourcePoolT>() {
        private Computation<OInt> resultB;
        private Computation<OInt> resultA;

        @Override
        public void test() throws Exception {

          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    SIntFactory intFactory = builder.getSIntFactory();
                    SInt encryptionKey = intFactory.getSInt(BigInteger.valueOf(527618));
                    SInt plainTextA = intFactory.getSInt(BigInteger.valueOf(10));
                    SInt plainTextB = intFactory.getSInt(BigInteger.valueOf(11));
                    Computation<SInt> cipherTextA = builder.createSequentialSub(
                        new MiMCEncryptionProtocolNaiveImpl4(plainTextA, encryptionKey)
                    );
                    Computation<SInt> cipherTextB = builder.createSequentialSub(
                        new MiMCEncryptionProtocolNaiveImpl4(plainTextB, encryptionKey)
                    );
                    resultA = builder.createOpenBuilder().open(cipherTextA);
                    resultB = builder.createOpenBuilder().open(cipherTextB);
                  }).build();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertNotEquals(resultA.out().getValue(), resultB.out().getValue());
        }
      };
    }
  }

  public static class TestMiMCEncDec<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT> {

    @Override
    public TestThread<ResourcePoolT> next(TestThreadConfiguration<ResourcePoolT> conf) {
      return new TestThread<ResourcePoolT>() {
        private Computation<OInt> result;

        @Override
        public void test() throws Exception {
          BigInteger x_big = BigInteger.valueOf(10);
          TestApplication app = new TestApplication() {


            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    SIntFactory intFactory = builder.getSIntFactory();
                    SInt encryptionKey = intFactory.getSInt(BigInteger.valueOf(10));
                    SInt plainText = intFactory.getSInt(x_big);
                    Computation<SInt> cipherText = builder.createSequentialSub(
                        new MiMCEncryptionProtocolNaiveImpl4(plainText, encryptionKey)
                    );
                    Computation<SInt> decrypted = builder.createSequentialSub(
                        new MiMCDecryptionProtocolNaiveImpl4(cipherText, encryptionKey)
                    );
                    result = builder.createOpenBuilder().open(decrypted);
                  }).build();
            }
          };

          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          Assert.assertEquals(x_big, result.out().getValue());
        }
      };
    }
  }
}
