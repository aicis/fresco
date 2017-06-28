/*
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
 */
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplicationBigInteger;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;

public class AdvancedNumericTests {

  public static class TestDivision extends TestThreadFactory {

    private int numerator;
    private int denominator;
    private BigInteger modulus;

    public TestDivision(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplicationBigInteger app = new TestApplicationBigInteger() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    modulus = ((BuilderFactoryNumeric) factoryProducer)
                        .getBasicNumericFactory().getModulus();

                    Computation<SInt> p = builder.numeric()
                        .known(BigInteger.valueOf(numerator));
                    Computation<SInt> q = builder.numeric()
                        .known(BigInteger.valueOf(denominator));

                    Computation<SInt> result = builder.createAdvancedNumericBuilder().div(p, q);

                    output = builder.numeric().open(result);
                  }).build();
            }
          };

          BigInteger result = (BigInteger) secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              convertRepresentation(result, modulus));
        }
      };
    }

  }


  private static BigInteger convertRepresentation(BigInteger b, BigInteger modulus) {
    // Stolen from Spdz Util
    BigInteger actual = b.mod(modulus);
    if (actual.compareTo(modulus.divide(BigInteger.valueOf(2))) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }

  public static class TestDivisionWithKnownDenominator extends TestThreadRunner.TestThreadFactory {

    private int numerator;
    private int denominator;
    private BigInteger modulus;

    public TestDivisionWithKnownDenominator(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }

    @Override
    public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplicationBigInteger app = new TestApplicationBigInteger() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    modulus = ((BuilderFactoryNumeric) factoryProducer)
                        .getBasicNumericFactory().getModulus();

                    Computation<SInt> p = builder.numeric()
                        .known(BigInteger.valueOf(numerator));
                    BigInteger q = BigInteger.valueOf(denominator);

                    Computation<SInt> result = builder.createAdvancedNumericBuilder().div(p, q);

                    output = builder.numeric().open(result);
                  }).build();
            }
          };

          BigInteger result = (BigInteger) secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              convertRepresentation(result, modulus));
        }
      };
    }
  }

  public static class TestModulus extends TestThreadRunner.TestThreadFactory {

    static int numerator = 9;
    static int denominator = 4;

    @Override
    public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplicationBigInteger app = new TestApplicationBigInteger() {
            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    Computation<SInt> p = builder.numeric()
                        .known(BigInteger.valueOf(numerator));
                    BigInteger q = BigInteger.valueOf(denominator);

                    Computation<SInt> result = builder.createAdvancedNumericBuilder()
                        .mod(p, q);

                    output = builder.numeric().open(result);
                  }).build();
            }
          };

          BigInteger result = (BigInteger) secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));

          Assert.assertEquals(BigInteger.valueOf(numerator % denominator),
              result);
        }
      };
    }
  }

}
