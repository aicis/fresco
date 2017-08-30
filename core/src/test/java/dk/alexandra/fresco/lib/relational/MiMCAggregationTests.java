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
package dk.alexandra.fresco.lib.relational;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.collections.io.CloseMatrix;
import dk.alexandra.fresco.lib.collections.io.OpenMatrix;

public class MiMCAggregationTests {

  /**
   * Performs a MiMCAggregation computation on a matrix of SInts.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestMiMCAggregationGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    final Matrix<BigInteger> input;
    final Matrix<BigInteger> expected;

    TestMiMCAggregationGeneric(Matrix<BigInteger> input, Matrix<BigInteger> expected) {
      this.input = input;
      this.expected = expected;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Matrix<BigInteger>, SequentialNumericBuilder> testApplication = root -> {
            final int pid = conf.getMyId();
            // sort of hacky
            final int pids[] = new int[conf.getNoOfParties()];
            for (int i = 0; i < pids.length; i++) {
              pids[i] = i + 1;
            }
            return root.par(par -> {
              // close inputs
              return new CloseMatrix(input, 1).build(par);
            }).seq((closed, seq) -> {
              // shuffle
              return new MiMCAggregation(closed, new Random(42), 0, 1, pid, pids).build(seq);
            }).par((swapped, par) -> {
              // open result
              Computation<Matrix<Computation<BigInteger>>> opened =
                  new OpenMatrix(swapped).build(par);
              return () -> new MatrixUtils().unwrapMatrix(opened.out());
            });
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
    // define input matrix
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

  public static <ResourcePoolT extends ResourcePool> TestMiMCAggregationGeneric<ResourcePoolT> aggregateEmpty() {
    // define input matrix
    MatrixTestUtils utils = new MatrixTestUtils();
    Matrix<BigInteger> input = utils.getInputMatrix(0, 0);
    Matrix<BigInteger> expected = utils.getInputMatrix(0, 0);
    return new TestMiMCAggregationGeneric<>(input, expected);
  }
}
