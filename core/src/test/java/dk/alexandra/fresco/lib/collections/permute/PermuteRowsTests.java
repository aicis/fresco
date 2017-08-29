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
package dk.alexandra.fresco.lib.collections.permute;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.collections.io.CloseMatrix;
import dk.alexandra.fresco.lib.collections.io.OpenMatrix;

public class PermuteRowsTests {

  /**
   * Performs a PermuteRows computation on a matrix of SInts.
   * 
   * @author nv
   *
   * @param <ResourcePoolT>
   */
  public static class TestPermuteRowsGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialNumericBuilder> {

    final Matrix<BigInteger> input;
    final Matrix<BigInteger> expected;
    final int[] idxPerm;

    TestPermuteRowsGeneric(Matrix<BigInteger> input, Matrix<BigInteger> expected, int[] idxPerm) {
      this.input = input;
      this.expected = expected;
      this.idxPerm = idxPerm;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialNumericBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialNumericBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialNumericBuilder>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Matrix<BigInteger>, SequentialNumericBuilder> testApplication = root -> {
            return root.par(par -> {
              // close inputs
              return new CloseMatrix(input, 1).build(par);
            }).seq((closed, seq) -> {
              // apply permutation
              if (conf.getMyId() == 1) {
                return new PermuteRows(closed, idxPerm, 1).build(seq);
              } else {
                return new PermuteRows(closed, 1).build(seq);
              }
            }).par((swapped, par) -> {
              // open result
              Computation<Matrix<Computation<BigInteger>>> opened =
                  new OpenMatrix(swapped).build(par);
              return () -> new MatrixUtils().unwrapMatrix(opened.out());
            });
          };
          Matrix<BigInteger> actual = secureComputationEngine.runApplication(testApplication,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          assertThat(actual.getRows(), is(expected.getRows()));
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestPermuteRowsGeneric<ResourcePoolT> permuteRows() {
    // define input matrix
    Matrix<BigInteger> input = new MatrixUtils().getInputMatrix(8, 3);
    // define permutation
    int[] idxPerm = new int[8];
    idxPerm[0] = 0;
    idxPerm[1] = 7;
    idxPerm[2] = 1;
    idxPerm[3] = 3;
    idxPerm[4] = 4;
    idxPerm[5] = 2;
    idxPerm[6] = 5;
    idxPerm[7] = 6;
    // define expected result
    ArrayList<ArrayList<BigInteger>> expectedRows = new ArrayList<>(8);
    int[] inverted = new WaksmanUtils().invert(idxPerm);
    for (int i = 0; i < idxPerm.length; i++) {
      expectedRows.add(input.getRow(inverted[i]));
    }
    Matrix<BigInteger> expected = new Matrix<>(8, 3, expectedRows);
    return new TestPermuteRowsGeneric<>(input, expected, idxPerm);
  }

  public static <ResourcePoolT extends ResourcePool> TestPermuteRowsGeneric<ResourcePoolT> permuteEmptyRows() {
    // define empty input matrix
    Matrix<BigInteger> input = new MatrixUtils().getInputMatrix(0, 0);
    // define empty permutation
    int[] idxPerm = new int[0];
    // define expected result
    Matrix<BigInteger> expected = new MatrixUtils().getInputMatrix(0, 0);
    return new TestPermuteRowsGeneric<>(input, expected, idxPerm);
  }

}
