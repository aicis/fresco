package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.common.collections.sort.NumericSortingTests.TestOddEvenMergeSort;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzSorting extends AbstractSpdzTest {

  @Test
  public void test_Sort() {
    runTest(new TestOddEvenMergeSort<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Big_Sort() {
    runTest(new TestOddEvenMergeSort<>(83, 4, 8),
        PreprocessingStrategy.DUMMY, 2);
  }
}
