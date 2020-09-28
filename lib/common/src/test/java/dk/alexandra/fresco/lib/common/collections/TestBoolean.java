package dk.alexandra.fresco.lib.common.collections;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.common.collections.sort.CollectionsSortingTests;
import dk.alexandra.fresco.lib.common.collections.sort.CollectionsSortingTests.TestOddEvenMerge;
import dk.alexandra.fresco.lib.common.compare.CompareTests;
import dk.alexandra.fresco.lib.common.compare.ComparisonBooleanTests;
import dk.alexandra.fresco.lib.common.math.bool.add.AddTests;
import dk.alexandra.fresco.lib.common.math.bool.log.LogTests;
import dk.alexandra.fresco.lib.common.math.bool.mult.MultTests;
import dk.alexandra.fresco.suite.dummy.bool.AbstractDummyBooleanTest;
import org.junit.Test;

/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestBoolean extends AbstractDummyBooleanTest {

  // collections.sort

  @Test
  public void test_Uneven_Odd_Even_Merge_2_parties() {
    runTest(new TestOddEvenMerge<>(false),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Uneven_Odd_Even_Merge_presorted_2_parties() {
    runTest(new TestOddEvenMerge<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Keyed_Compare_And_Swap_2_parties() {
    runTest(new CollectionsSortingTests.TestKeyedCompareAndSwap<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

}
