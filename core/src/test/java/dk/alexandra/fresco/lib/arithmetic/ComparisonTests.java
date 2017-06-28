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
package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;

public class ComparisonTests {

  /**
   * Compares the two numbers 3 and 5 and checks that 3 < 5. Also checks that 5 is not < 3
   *
   * @author Kasper Damgaard
   */
  public static class TestCompareLT extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {

        private Computation<BigInteger> res1;
        private Computation<BigInteger> res2;

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    NumericBuilder input = builder.numeric();
                    Computation<SInt> x = input.known(three);
                    Computation<SInt> y = input.known(five);
                    ComparisonBuilder comparison = builder.comparison();
                    Computation<SInt> compResult1 = comparison.compare(x, y);
                    Computation<SInt> compResult2 = comparison.compare(y, x);
                    NumericBuilder open = builder.numeric();
                    res1 = open.open(compResult1);
                    res2 = open.open(compResult2);
                  }).build();
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          Assert.assertEquals(BigInteger.ONE, res1.out());
          Assert.assertEquals(BigInteger.ZERO, res2.out());
        }
      };
    }
  }

  /**
   * Compares the two numbers 3 and 5 and checks that 3 == 3. Also checks that 3 != 5
   *
   * @author Kasper Damgaard
   */
  public static class TestCompareEQ extends TestThreadFactory {

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {

        private Computation<BigInteger> res1;
        private Computation<BigInteger> res2;

        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private BigInteger three = BigInteger.valueOf(3);
            private BigInteger five = BigInteger.valueOf(5);

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    NumericBuilder input = builder.numeric();
                    Computation<SInt> x = input.known(three);
                    Computation<SInt> y = input.known(five);
                    ComparisonBuilder comparison = builder.comparison();
                    Computation<SInt> compResult1 = comparison.equals(x, x);
                    Computation<SInt> compResult2 = comparison.equals(x, y);
                    NumericBuilder open = builder.numeric();
                    res1 = open.open(compResult1);
                    res2 = open.open(compResult2);
                  }).build();
            }
          };
          secureComputationEngine
              .runApplication(app, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          Assert.assertEquals(BigInteger.ONE, res1.out());
          Assert.assertEquals(BigInteger.ZERO, res2.out());
        }
      };
    }
  }
}
