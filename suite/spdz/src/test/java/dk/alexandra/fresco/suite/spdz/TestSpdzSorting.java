package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.SortingTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class TestSpdzSorting extends AbstractSpdzTest {

  @Test
  public void test_isSorted() throws Exception {
    runTest(new SortingTests.TestIsSorted<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_compareAndSwap() throws Exception {
    runTest(new SortingTests.TestCompareAndSwap<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Sort() throws Exception {
    runTest(new SortingTests.TestSort<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  @Category(IntegrationTest.class)
  public void test_Big_Sort() throws Exception {
    runTest(new SortingTests.TestBigSort<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }
}
