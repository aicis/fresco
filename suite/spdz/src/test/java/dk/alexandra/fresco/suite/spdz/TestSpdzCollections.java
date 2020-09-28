package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.collections.SearchingTests;
import dk.alexandra.fresco.lib.common.collections.io.CloseListTests;
import dk.alexandra.fresco.lib.common.collections.io.CloseMatrixTests;
import dk.alexandra.fresco.lib.common.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.common.collections.permute.PermuteRowsTests;
import dk.alexandra.fresco.lib.common.collections.shuffle.ShuffleRowsTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.ArrayList;
import org.junit.Test;

public class TestSpdzCollections extends AbstractSpdzTest {

  @Test
  public void test_close_empty_list() {
    runTest(new CloseListTests.TestCloseEmptyList<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_list() {
    runTest(new CloseListTests.TestCloseAndOpenList<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_empty_matrix() {
    runTest(new CloseMatrixTests.TestCloseEmptyMatrix<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_matrix() {
    runTest(new CloseMatrixTests.TestCloseAndOpenMatrix<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Test_Is_Sorted() {
    runTest(new SearchingTests.TestIsSorted<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_permute_empty_rows() {
    runTest(PermuteRowsTests.permuteEmptyRows(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_permute_rows() {
    runTest(PermuteRowsTests.permuteRows(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test_permute_rows_non_power_of_two() {
    ArrayList<ArrayList<DRes<SInt>>> fakeRows = new ArrayList<>();
    Matrix<DRes<SInt>> fakeMatrix = new Matrix<>(3, 2, fakeRows);
    new PermuteRows(() -> fakeMatrix, new int[]{}, 1, true).buildComputation(null);
  }

  @Test
  public void test_shuffle_rows_two_parties() {
    runTest(ShuffleRowsTests.shuffleRowsTwoParties(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_shuffle_rows_three_parties() {
    runTest(ShuffleRowsTests.shuffleRowsThreeParties(),
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_shuffle_rows_empty() {
    runTest(ShuffleRowsTests.shuffleRowsEmpty(),
        PreprocessingStrategy.DUMMY, 2);
  }
}
