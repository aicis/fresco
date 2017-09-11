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
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;

public class ConditionalSelectTests {

  /**
   * Performs a ConditionalSelect computation on two SInt.
   * 
   * Should select left value
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestSelect<ResourcePoolT extends ResourcePool> extends TestThreadFactory {

    final BigInteger selectorOpen;
    final BigInteger expected;
    final BigInteger leftOpen;
    final BigInteger rightOpen;

    public TestSelect(BigInteger selectorOpen, BigInteger leftOpen, BigInteger rightOpen,
        BigInteger expected) {
      this.selectorOpen = selectorOpen;
      this.expected = expected;
      this.leftOpen = leftOpen;
      this.rightOpen = rightOpen;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<BigInteger, ProtocolBuilderNumeric> testApplication = root -> {
            Numeric numeric = root.numeric();
            AdvancedNumeric advancedNumeric = root.advancedNumeric(); 
            DRes<SInt> left = numeric.input(leftOpen, 1);
            DRes<SInt> right = numeric.input(rightOpen, 1);
            DRes<SInt> selector = numeric.input(selectorOpen, 1);
            DRes<SInt> selected = advancedNumeric.condSelect(selector, left, right);
            return numeric.open(selected);
          };
          BigInteger output = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertEquals(expected, output);
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestSelect<ResourcePoolT> testSelectLeft() {
    BigInteger selector = BigInteger.valueOf(1);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    return new TestSelect<>(selector, leftOpen, rightOpen, leftOpen);
  }

  public static <ResourcePoolT extends ResourcePool> TestSelect<ResourcePoolT> testSelectRight() {
    BigInteger selector = BigInteger.valueOf(0);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    return new TestSelect<>(selector, leftOpen, rightOpen, rightOpen);
  }
}
