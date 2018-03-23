package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.arithmetic.SearchingTests;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.io.CloseListTests;
import dk.alexandra.fresco.lib.collections.io.CloseMatrixTests;
import dk.alexandra.fresco.lib.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.collections.permute.PermuteRowsTests;
import dk.alexandra.fresco.lib.collections.relational.LeakyAggregationTests;
import dk.alexandra.fresco.lib.collections.shuffle.ShuffleRowsTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.ArrayList;
import org.junit.Test;

public class TestSpdzCollections extends AbstractSpdzTest {

  @Test
  public void test_close_empty_list() throws Exception {
    runTest(new CloseListTests.TestCloseEmptyList<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_list() throws Exception {
    runTest(new CloseListTests.TestCloseAndOpenList<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_empty_matrix() throws Exception {
    runTest(new CloseMatrixTests.TestCloseEmptyMatrix<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_close_matrix() throws Exception {
    runTest(new CloseMatrixTests.TestCloseAndOpenMatrix<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Test_Is_Sorted() throws Exception {
    runTest(new SearchingTests.TestIsSorted<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_permute_empty_rows() throws Exception {
    runTest(PermuteRowsTests.permuteEmptyRows(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_permute_rows() throws Exception {
    runTest(PermuteRowsTests.permuteRows(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test_permute_rows_non_power_of_two() throws Throwable {
    ArrayList<ArrayList<DRes<SInt>>> fakeRows = new ArrayList<>();
    Matrix<DRes<SInt>> fakeMatrix = new Matrix<>(3, 2, fakeRows);
    new PermuteRows(() -> fakeMatrix, new int[] {}, 1, true).buildComputation(null);
  }

  @Test
  public void test_shuffle_rows_two_parties() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsTwoParties(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_shuffle_rows_three_parties() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsThreeParties(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_shuffle_rows_empty() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsEmpty(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MiMC_aggregate_two() throws Exception {
    runTest(LeakyAggregationTests.aggregate(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MiMC_aggregate_three() throws Exception {
    runTest(LeakyAggregationTests.aggregate(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_MiMC_aggregate_empty() throws Exception {
    runTest(LeakyAggregationTests.aggregateEmpty(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }
}
