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
package dk.alexandra.fresco.lib.collections.relational;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.Collections;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;
import dk.alexandra.fresco.lib.collections.MatrixUtils;

public class MiMCAggregationTests {

  /**
   * Performs a MiMCAggregation computation on a matrix of SInts.
   * 
   * @param <ResourcePoolT>
   */
  public static class TestMiMCAggregationGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory {

    final Matrix<BigInteger> input;
    final Matrix<BigInteger> expected;

    TestMiMCAggregationGeneric(Matrix<BigInteger> input, Matrix<BigInteger> expected) {
      this.input = input;
      this.expected = expected;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<Matrix<DRes<SInt>>> closed = root.collections().closeMatrix(input, 1);
            DRes<Matrix<DRes<SInt>>> aggregated =
                root.collections().leakyAggregateSum(closed, 0, 1);
            DRes<Matrix<DRes<BigInteger>>> opened = root.collections().openMatrix(aggregated);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };
          Matrix<BigInteger> actual = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          // sort by key to undo shuffling
          // (keys are guaranteed to be unique)
          Collections.sort(actual.getRows(), (r1, r2) -> r1.get(0).compareTo(r2.get(0)));
          assertThat(actual.getRows(), is(expected.getRows()));
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestMiMCAggregationGeneric<ResourcePoolT> aggregate() {
    MatrixTestUtils utils = new MatrixTestUtils();
    BigInteger[][] rawRows = {{BigInteger.valueOf(1), BigInteger.valueOf(7), BigInteger.valueOf(8)},
        {BigInteger.valueOf(1), BigInteger.valueOf(19), BigInteger.valueOf(20)},
        {BigInteger.valueOf(1), BigInteger.valueOf(10), BigInteger.valueOf(11)},
        {BigInteger.valueOf(1), BigInteger.valueOf(4), BigInteger.valueOf(5)},
        {BigInteger.valueOf(2), BigInteger.valueOf(13), BigInteger.valueOf(14)},
        {BigInteger.valueOf(2), BigInteger.valueOf(1), BigInteger.valueOf(2)},
        {BigInteger.valueOf(2), BigInteger.valueOf(22), BigInteger.valueOf(23)},
        {BigInteger.valueOf(2), BigInteger.valueOf(16), BigInteger.valueOf(17)}};
    Matrix<BigInteger> input = utils.getInputMatrix(rawRows);
    BigInteger[][] expectedRows = {{BigInteger.valueOf(1), BigInteger.valueOf(40)},
        {BigInteger.valueOf(2), BigInteger.valueOf(52)}};
    Matrix<BigInteger> expected = utils.getInputMatrix(expectedRows);
    return new TestMiMCAggregationGeneric<>(input, expected);
  }

  public static <ResourcePoolT extends ResourcePool> TestMiMCAggregationGeneric<ResourcePoolT> aggregateUniqueKeys() {
    MatrixTestUtils utils = new MatrixTestUtils();
    BigInteger[][] rawRows = {{BigInteger.valueOf(1), BigInteger.valueOf(7), BigInteger.valueOf(8)},
        {BigInteger.valueOf(2), BigInteger.valueOf(19), BigInteger.valueOf(20)},
        {BigInteger.valueOf(3), BigInteger.valueOf(10), BigInteger.valueOf(11)},
        {BigInteger.valueOf(4), BigInteger.valueOf(4), BigInteger.valueOf(5)},
        {BigInteger.valueOf(5), BigInteger.valueOf(13), BigInteger.valueOf(14)},
        {BigInteger.valueOf(6), BigInteger.valueOf(1), BigInteger.valueOf(2)},
        {BigInteger.valueOf(7), BigInteger.valueOf(22), BigInteger.valueOf(23)},
        {BigInteger.valueOf(8), BigInteger.valueOf(16), BigInteger.valueOf(17)}};
    Matrix<BigInteger> input = utils.getInputMatrix(rawRows);
    BigInteger[][] expectedRows = {{BigInteger.valueOf(1), BigInteger.valueOf(7)},
        {BigInteger.valueOf(2), BigInteger.valueOf(19)},
        {BigInteger.valueOf(3), BigInteger.valueOf(10)},
        {BigInteger.valueOf(4), BigInteger.valueOf(4)},
        {BigInteger.valueOf(5), BigInteger.valueOf(13)},
        {BigInteger.valueOf(6), BigInteger.valueOf(1)},
        {BigInteger.valueOf(7), BigInteger.valueOf(22)},
        {BigInteger.valueOf(8), BigInteger.valueOf(16)}};
    Matrix<BigInteger> expected = utils.getInputMatrix(expectedRows);
    return new TestMiMCAggregationGeneric<>(input, expected);
  }

  public static <ResourcePoolT extends ResourcePool> TestMiMCAggregationGeneric<ResourcePoolT> aggregateEmpty() {
    MatrixTestUtils utils = new MatrixTestUtils();
    Matrix<BigInteger> input = utils.getInputMatrix(0, 0);
    Matrix<BigInteger> expected = utils.getInputMatrix(0, 0);
    return new TestMiMCAggregationGeneric<>(input, expected);
  }
}
