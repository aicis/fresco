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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the ConditionalSwapRowsTests protocol.
 */
public class ConditionalSwapNeighborsTests {

  public static class TestSwapGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final List<BigInteger> openSwappers;
    final Matrix<BigInteger> expected;
    final Matrix<BigInteger> input;

    public TestSwapGeneric(List<BigInteger> openSwappers, Matrix<BigInteger> expected,
        Matrix<BigInteger> input) {
      this.openSwappers = openSwappers;
      this.expected = expected;
      this.input = input;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = root -> {
            Collections collections = root.collections();
            DRes<List<DRes<SInt>>> swappers = collections.closeList(openSwappers, 1);
            DRes<Matrix<DRes<SInt>>> closed = collections.closeMatrix(input, 1);
            DRes<Matrix<DRes<SInt>>> swapped = collections.swapNeighborsIf(swappers, closed);
            DRes<Matrix<DRes<BigInteger>>> opened = collections.openMatrix(swapped);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };
          Matrix<BigInteger> output = runApplication(testApplication);
          assertThat(output.getRows(), is(expected.getRows()));
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestSwapGeneric<ResourcePoolT> testSwapYes() {
    Matrix<BigInteger> input = new MatrixTestUtils().getInputMatrix(8, 3);
    Matrix<BigInteger> expected = new Matrix<>(input);
    List<BigInteger> swappers = new ArrayList<>();
    int numSwappers = 4;
    for (int s = 0; s < numSwappers; s++) {
      BigInteger swapper = BigInteger.valueOf(s % 2);
      if (swapper.equals(BigInteger.ONE)) {
        ArrayList<BigInteger> leftRow = expected.getRow(s * 2);
        ArrayList<BigInteger> rightRow = expected.getRow(s * 2 + 1);
        expected.setRow(s * 2, rightRow);
        expected.setRow(s * 2 + 1, leftRow);
      }
      swappers.add(swapper);
    }
    return new TestSwapGeneric<>(swappers, input, expected);
  }

  public static <ResourcePoolT extends ResourcePool> TestSwapGeneric<ResourcePoolT> testSwapNo() {
    Matrix<BigInteger> input = new MatrixTestUtils().getInputMatrix(8, 3);
    List<BigInteger> swappers = new ArrayList<>();
    int numSwappers = 4;
    for (int s = 0; s < numSwappers; s++) {
      swappers.add(BigInteger.valueOf(0));
    }
    return new TestSwapGeneric<>(swappers, input, input);
  }
}
