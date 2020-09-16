package dk.alexandra.fresco.lib.common.collections.shuffle;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.DefaultCollections;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.collections.MatrixTestUtils;
import dk.alexandra.fresco.lib.common.collections.MatrixUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ShuffleRowsTests {

  public static class TestShuffleRowsGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final Matrix<BigInteger> input;
    final Matrix<BigInteger> expected;

    TestShuffleRowsGeneric(Matrix<BigInteger> input, Matrix<BigInteger> expected) {
      this.input = input;
      this.expected = expected;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication =
              root -> {
                dk.alexandra.fresco.lib.common.collections.Collections
                    collections = new DefaultCollections(root);
                DRes<Matrix<DRes<SInt>>> closed = collections.closeMatrix(input, 1);
                // use package-private constructor to fix randomness
                DRes<Matrix<DRes<SInt>>> shuffled = collections.shuffle(closed);
                DRes<Matrix<DRes<BigInteger>>> opened = collections.openMatrix(shuffled);
                return () -> new MatrixUtils().unwrapMatrix(opened);
              };
          Matrix<BigInteger> actual = runApplication(testApplication);
          assertTrue(containsSameRows(actual.getRows(), expected.getRows()));
        }
      };
    }
  }

  private static boolean containsSameRows(
      ArrayList<ArrayList<BigInteger>> first, ArrayList<ArrayList<BigInteger>> second) {
    for (ArrayList<BigInteger> row : first) {
      if (!containsRow(second, row)) {
        return false;
      }
    }
    return true;
  }

  private static boolean containsRow(
      ArrayList<ArrayList<BigInteger>> rows, ArrayList<BigInteger> row) {
    for (ArrayList<BigInteger> r : rows) {
      if (row.equals(r)) {
        return true;
      }
    }
    return false;
  }

  private static Matrix<BigInteger> clearTextShuffle(
      int[] pids, int seed, Matrix<BigInteger> input) {
    Random[] rands = new Random[pids.length];
    for (int i = 0; i < pids.length; i++) {
      rands[i] = new Random(seed + pids[i]);
    }
    ArrayList<ArrayList<BigInteger>> rows = input.getRows();
    for (int pid : pids) {
      Collections.shuffle(rows, rands[pid - 1]);
    }
    return new Matrix<>(input.getHeight(), input.getWidth(), rows);
  }

  // result depends on number of parties
  public static <ResourcePoolT extends ResourcePool>
      TestShuffleRowsGeneric<ResourcePoolT> shuffleRowsTwoParties() {
    // define input matrix
    MatrixTestUtils utils = new MatrixTestUtils();
    Matrix<BigInteger> input = utils.getInputMatrix(8, 3);
    Matrix<BigInteger> expected =
        clearTextShuffle(new int[] {1, 2}, 42, utils.getInputMatrix(8, 3));
    return new TestShuffleRowsGeneric<>(input, expected);
  }

  // result depends on number of parties
  public static <ResourcePoolT extends ResourcePool>
      TestShuffleRowsGeneric<ResourcePoolT> shuffleRowsThreeParties() {
    // define input matrix
    MatrixTestUtils utils = new MatrixTestUtils();
    Matrix<BigInteger> input = utils.getInputMatrix(8, 3);
    Matrix<BigInteger> expected =
        clearTextShuffle(new int[] {1, 2, 3}, 42, utils.getInputMatrix(8, 3));
    return new TestShuffleRowsGeneric<>(input, expected);
  }

  public static <ResourcePoolT extends ResourcePool>
      TestShuffleRowsGeneric<ResourcePoolT> shuffleRowsEmpty() {
    // define input matrix
    MatrixTestUtils utils = new MatrixTestUtils();
    Matrix<BigInteger> input = utils.getInputMatrix(0, 0);
    Matrix<BigInteger> expected = utils.getInputMatrix(0, 0);
    return new TestShuffleRowsGeneric<>(input, expected);
  }
}
