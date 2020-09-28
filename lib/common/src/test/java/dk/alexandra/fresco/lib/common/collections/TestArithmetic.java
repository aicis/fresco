package dk.alexandra.fresco.lib.common.collections;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.io.CloseListTests;
import dk.alexandra.fresco.lib.common.collections.io.CloseMatrixTests;
import dk.alexandra.fresco.lib.common.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.common.collections.permute.PermuteRowsTests;
import dk.alexandra.fresco.lib.common.collections.shuffle.ShuffleRowsTests;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.ArrayList;
import org.junit.Test;

public class TestArithmetic extends AbstractDummyArithmeticTest {

  // lib.collections

  @Test
  public void test_close_empty_list() {
    runTest(new CloseListTests.TestCloseEmptyList<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_list() {
    runTest(new CloseListTests.TestCloseEmptyList<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_empty_matrix() {
    runTest(new CloseMatrixTests.TestCloseEmptyMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_matrix() {
    runTest(new CloseMatrixTests.TestCloseAndOpenMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Test_Is_Sorted() {
    runTest(new SearchingTests.TestIsSorted<>(), new TestParameters());
  }

  @Test
  public void test_permute_empty_rows() {
    runTest(PermuteRowsTests.permuteEmptyRows(), new TestParameters().numParties(2));
  }

  @Test
  public void test_permute_rows() {
    runTest(PermuteRowsTests.permuteRows(), new TestParameters().numParties(2));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test_permute_rows_non_power_of_two() {
    ArrayList<ArrayList<DRes<SInt>>> fakeRows = new ArrayList<>();
    Matrix<DRes<SInt>> fakeMatrix = new Matrix<>(3, 2, fakeRows);
    new PermuteRows(() -> fakeMatrix, new int[]{}, 1, true).buildComputation(null);
  }

  @Test
  public void test_shuffle_rows_two_parties() {
    runTest(ShuffleRowsTests.shuffleRowsTwoParties(), new TestParameters().numParties(2));
  }

  @Test
  public void test_shuffle_rows_three_parties() {
    runTest(ShuffleRowsTests.shuffleRowsThreeParties(), new TestParameters().numParties(3));
  }

  @Test
  public void test_shuffle_rows_empty() {
    runTest(ShuffleRowsTests.shuffleRowsEmpty(), new TestParameters().numParties(2));
  }

  @Test
  public void test_isSorted() {
    runTest(new SortingTests.TestIsSorted<>(), new TestParameters());
  }

  @Test
  public void test_compareAndSwap() {
    runTest(new SortingTests.TestCompareAndSwap<>(), new TestParameters());
  }

  @Test
  public void test_Sort() {
    runTest(new SortingTests.TestSort<>(), new TestParameters());
  }

  @Test
  public void test_Big_Sort() {
    runTest(new SortingTests.TestBigSort<>(), new TestParameters());
  }


}

