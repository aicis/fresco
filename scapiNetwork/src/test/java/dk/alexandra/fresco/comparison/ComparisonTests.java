/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.comparison;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.network.ResourcePoolCreator;
import java.math.BigInteger;
import org.junit.Assert;

public class ComparisonTests {

  /**
   * Compares the two numbers 3 and 5 and checks that 3 < 5. Also checks that 5 is not < 3
   *
   * @author Kasper Damgaard
   */
  public static class TestCompareLT<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            BigInteger three = BigInteger.valueOf(3);
            BigInteger five = BigInteger.valueOf(5);
            Numeric numeric = builder.numeric();
            DRes<SInt> x = numeric.input(three, 1);
            DRes<SInt> y = numeric.input(five, 2);
            Comparison comparison = builder.comparison();
            DRes<SInt> compResult1 = comparison.compareLEQ(x, y);
            DRes<SInt> compResult2 = comparison.compareLEQ(y, x);

            DRes<BigInteger> res1 = numeric.open(compResult1);
            DRes<BigInteger> res2 = numeric.open(compResult2);
            return () -> new Pair<>(res1.out(), res2.out());
          };
          ResourcePoolT resourcePool = ResourcePoolCreator.createResourcePool(conf.sceConf);
          Pair<BigInteger, BigInteger> pair =
              secureComputationEngine.runApplication(app, resourcePool);
          Assert.assertEquals(BigInteger.ONE, pair.getFirst());
          Assert.assertEquals(BigInteger.ZERO, pair.getSecond());
        }
      };
    }
  }

  /**
   * Compares the two numbers 3 and 5 and checks that 3 == 3. Also checks that 3 != 5
   *
   * @author Kasper Damgaard
   */
  public static class TestCompareEQ<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();

            BigInteger three = BigInteger.valueOf(3);
            BigInteger five = BigInteger.valueOf(5);
            DRes<SInt> x = input.input(three, 2);
            DRes<SInt> y = input.input(five, 1);
            Comparison comparison = builder.comparison();
            DRes<SInt> compResult1 = comparison.equals(x, x);
            DRes<SInt> compResult2 = comparison.equals(x, y);

            Numeric open = builder.numeric();
            DRes<BigInteger> res1 = open.open(compResult1);
            DRes<BigInteger> res2 = open.open(compResult2);
            return () -> new Pair<>(res1.out(), res2.out());
          };

          ResourcePoolT resourcePool = ResourcePoolCreator.createResourcePool(conf.sceConf);
          Pair<BigInteger, BigInteger> pair =
              secureComputationEngine.runApplication(app, resourcePool);
          Assert.assertEquals(BigInteger.ONE, pair.getFirst());
          Assert.assertEquals(BigInteger.ZERO, pair.getSecond());
        }
      };
    }
  }
}
