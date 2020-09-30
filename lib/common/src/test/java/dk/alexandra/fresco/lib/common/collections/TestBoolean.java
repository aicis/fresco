package dk.alexandra.fresco.lib.common.collections;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.collections.sort.CollectionsSortingTests;
import dk.alexandra.fresco.lib.common.collections.sort.CollectionsSortingTests.TestOddEvenMerge;
import dk.alexandra.fresco.suite.dummy.bool.AbstractDummyBooleanTest;
import org.junit.Test;

public class TestBoolean extends AbstractDummyBooleanTest {

  @Test
  public void test_Uneven_Odd_Even_Merge_2_parties() {
    runTest(new TestOddEvenMerge<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Keyed_Compare_And_Swap_2_parties() {
    runTest(new CollectionsSortingTests.TestKeyedCompareAndSwap<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test(expected = RuntimeException.class)
  public void test_Uneven_Odd_Even_Merge_leak_lengths() {
    runTest(new CollectionsSortingTests.TestOddEvenMergeDifferentPayloadLength<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

}
