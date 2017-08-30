/*
 * Copyright (c) 2015, 2016, 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.conditional;

import java.math.BigInteger;

import org.junit.Assert;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.io.OpenPair;

public class ConditionalSwapTests {

  /**
   * Performs a ConditionalSwapRows computation on matrix.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestSwap<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    final BigInteger swapperOpen;
    final Pair<BigInteger, BigInteger> expected;
    final BigInteger leftOpen;
    final BigInteger rightOpen;

    public TestSwap(BigInteger selectorOpen, BigInteger leftOpen, BigInteger rightOpen,
        Pair<BigInteger, BigInteger> expected) {
      this.swapperOpen = selectorOpen;
      this.expected = expected;
      this.leftOpen = leftOpen;
      this.rightOpen = rightOpen;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Pair<Computation<BigInteger>, Computation<BigInteger>>, SequentialNumericBuilder> testApplication =
              root -> {
                NumericBuilder nb = root.numeric();
                Computation<SInt> left = nb.input(leftOpen, 1);
                Computation<SInt> right = nb.input(rightOpen, 1);
                Computation<SInt> selector = nb.input(swapperOpen, 1);
                return root.par((par) -> {
                  return new ConditionalSwap(selector, left, right).build(par);
                }).par((res, par) -> {
                  return new OpenPair(res).build(par);
                });
              };
          Pair<Computation<BigInteger>, Computation<BigInteger>> output =
              secureComputationEngine.runApplication(testApplication,
                  ResourcePoolCreator.createResourcePool(conf.sceConf));
          Pair<BigInteger, BigInteger> actual =
              new Pair<>(output.getFirst().out(), output.getSecond().out());
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestSwap<ResourcePoolT> testSwapYes() {
    BigInteger swapper = BigInteger.valueOf(1);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    Pair<BigInteger, BigInteger> expected =
        new Pair<>(BigInteger.valueOf(42), BigInteger.valueOf(11));
    return new TestSwap<>(swapper, leftOpen, rightOpen, expected);
  }

  public static <ResourcePoolT extends ResourcePool> TestSwap<ResourcePoolT> testSwapNo() {
    BigInteger swapper = BigInteger.valueOf(0);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    Pair<BigInteger, BigInteger> expected =
        new Pair<>(BigInteger.valueOf(11), BigInteger.valueOf(42));
    return new TestSwap<>(swapper, leftOpen, rightOpen, expected);
  }
}
