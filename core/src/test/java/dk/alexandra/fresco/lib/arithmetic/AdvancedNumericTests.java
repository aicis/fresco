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

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;

public class AdvancedNumericTests {

  public static class TestDivision<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private int numerator;
    private int denominator;
    private BigInteger modulus;

    public TestDivision(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              builder -> {
                modulus = builder.getBasicNumericContext().getModulus();

                DRes<SInt> p = builder.numeric()
                    .known(BigInteger.valueOf(numerator));
                DRes<SInt> q = builder.numeric()
                    .known(BigInteger.valueOf(denominator));

                DRes<SInt> result = builder.advancedNumeric().div(p, q);

                return builder.numeric().open(result);
              };

          BigInteger result = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

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

  public static class TestDivisionWithKnownDenominator<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private int numerator;
    private int denominator;
    private BigInteger modulus;

    public TestDivisionWithKnownDenominator(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              builder -> {
                modulus = builder.getBasicNumericContext().getModulus();

                DRes<SInt> p = builder.numeric()
                    .known(BigInteger.valueOf(numerator));
                BigInteger q = BigInteger.valueOf(denominator);

                DRes<SInt> result = builder.advancedNumeric().div(p, q);

                return builder.numeric().open(result);
              };

          BigInteger result = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              convertRepresentation(result, modulus));
        }
      };
    }
  }

  public static class TestModulus<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    static int numerator = 9;
    static int denominator = 4;

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              builder -> {
                DRes<SInt> p = builder.numeric()
                    .known(BigInteger.valueOf(numerator));
                BigInteger q = BigInteger.valueOf(denominator);

                DRes<SInt> result = builder.advancedNumeric()
                    .mod(p, q);

                return builder.numeric().open(result);
              };

          BigInteger result = secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(BigInteger.valueOf(numerator % denominator),
              result);
        }
      };
    }
  }
}
