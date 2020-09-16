package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.common.arithmetic.SortingTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzSorting extends AbstractSpdzTest {

  @Test
  public void test_isSorted() {
    runTest(new SortingTests.TestIsSorted<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_compareAndSwap() {
    runTest(new SortingTests.TestCompareAndSwap<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Sort() {
    runTest(new SortingTests.TestSort<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Big_Sort() {
    runTest(new SortingTests.TestBigSort<>(),
        PreprocessingStrategy.DUMMY, 2);
  }
}
