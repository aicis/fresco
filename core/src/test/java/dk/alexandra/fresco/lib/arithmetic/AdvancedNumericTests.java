/*******************************************************************************
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
 *******************************************************************************/
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
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
          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              OmniBuilder builder = new OmniBuilder(factory);
              modulus = ((BasicNumericFactory) factory).getModulus();
              NumericIOBuilder io = builder.getNumericIOBuilder();
              AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

              SInt p = io.input(numerator, 1);
              SInt q = io.input(denominator, 1);
              SInt result = advanced.div(p, q);

              outputs = new OInt[]{io.output(result)};

              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              convertRepresentation(app.getOutputs()[0].getValue(), modulus));
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

  public static class TestDivisionWithPrecision extends TestThreadRunner.TestThreadFactory {

    @Override
    public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              OmniBuilder builder = new OmniBuilder(factory);
              NumericIOBuilder io = builder.getNumericIOBuilder();
              NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
              AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

              SInt p = io.input(9, 1);
              SInt q = io.input(4, 1);
              OInt precision = numeric.knownOInt(4);
              SInt result = advanced.div(p, q, precision);

              outputs = new OInt[]{io.output(result)};

              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(9 / 4),
              app.getOutputs()[0].getValue());
        }
      };
    }
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
          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              OmniBuilder builder = new OmniBuilder(factory);
              NumericIOBuilder io = builder.getNumericIOBuilder();
              modulus = ((BasicNumericFactory) factory).getModulus();
              NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
              AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

              SInt p = io.input(numerator, 1);
              OInt q = numeric.knownOInt(denominator);
              SInt result = advanced.div(p, q);

              outputs = new OInt[]{io.output(result)};

              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              convertRepresentation(app.getOutputs()[0].getValue(), modulus));
        }
      };
    }
  }

  public static class TestDivisionWithRemainder extends TestThreadRunner.TestThreadFactory {

    public static int numerator = 9;
    public static int denominator = 4;

    @Override
    public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              OmniBuilder builder = new OmniBuilder(factory);
              NumericIOBuilder io = builder.getNumericIOBuilder();
              NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
              AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

              SInt p = io.input(numerator, 1);
              OInt q = numeric.knownOInt(denominator);
              SInt[] results = advanced.divWithRemainder(p, q);

              outputs = io.outputArray(results);

              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              app.getOutputs()[0].getValue());
          Assert.assertEquals(BigInteger.valueOf(numerator % denominator),
              app.getOutputs()[1].getValue());
        }
      };
    }
  }

  public static class TestModulus extends TestThreadRunner.TestThreadFactory {

    @Override
    public TestThreadRunner.TestThread next(TestThreadRunner.TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {
            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              OmniBuilder builder = new OmniBuilder(factory);
              NumericIOBuilder io = builder.getNumericIOBuilder();
              NumericProtocolBuilder numeric = builder.getNumericProtocolBuilder();
              AdvancedNumericBuilder advanced = builder.getAdvancedNumericBuilder();

              SInt p = io.input(9, 1);
              OInt q = numeric.knownOInt(4);
              SInt result = advanced.mod(p, q);

              outputs = new OInt[]{io.output(result)};

              return builder.getProtocol();
            }
          };

          secureComputationEngine.runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(9 % 4),
              app.getOutputs()[0].getValue());
        }
      };
    }
  }
}
