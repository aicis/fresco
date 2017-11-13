package dk.alexandra.fresco.lib.collections.permute;

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

public class PermuteRowsTests {

  public static class TestPermuteRowsGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final Matrix<BigInteger> input;
    final Matrix<BigInteger> expected;
    final int[] idxPerm;

    TestPermuteRowsGeneric(Matrix<BigInteger> input, Matrix<BigInteger> expected, int[] idxPerm) {
      this.input = input;
      this.expected = expected;
      this.idxPerm = idxPerm;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = root -> {
            Collections collections = root.collections();
            DRes<Matrix<DRes<SInt>>> closed = collections.closeMatrix(input, 1);
            DRes<Matrix<DRes<SInt>>> permuted = null;
            if (root.getBasicNumericContext().getMyId() == 1) {
              permuted = collections.permute(closed, idxPerm);
            } else {
              permuted = collections.permute(closed, 1);
            }
            DRes<Matrix<DRes<BigInteger>>> opened = collections.openMatrix(permuted);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };
          Matrix<BigInteger> actual = runApplication(testApplication);
          assertThat(actual.getRows(), is(expected.getRows()));
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestPermuteRowsGeneric<ResourcePoolT> permuteRows() {
    // define input matrix
    Matrix<BigInteger> input = new MatrixTestUtils().getInputMatrix(8, 3);
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
    Matrix<BigInteger> input = new MatrixTestUtils().getInputMatrix(0, 0);
    // define empty permutation
    int[] idxPerm = new int[0];
    // define expected result
    Matrix<BigInteger> expected = new MatrixTestUtils().getInputMatrix(0, 0);
    return new TestPermuteRowsGeneric<>(input, expected, idxPerm);
  }

  public static <ResourcePoolT extends ResourcePool> TestPermuteRowsGeneric<ResourcePoolT> permuteRowsNonPowerOfTwo() {
    // define invalid input matrix
    Matrix<BigInteger> input = new MatrixTestUtils().getInputMatrix(3, 2);
    // define permutation
    int[] idxPerm = {0, 1, 2};
    // define expected result
    Matrix<BigInteger> expected = new MatrixTestUtils().getInputMatrix(3, 2);
    return new TestPermuteRowsGeneric<>(input, expected, idxPerm);
  }
}
