package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.dummy.bool.AbstractDummyBooleanTest;
import org.junit.Test;

/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestBoolean extends AbstractDummyBooleanTest {

  @Test
  public void test_compare_and_swap() {
    runTest(new ComparisonBooleanTests.CompareAndSwapTest<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_comparison() {
    runTest(new ComparisonBooleanTests.TestGreaterThan<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_comparison_unequal_length() {
    runTest(new ComparisonBooleanTests.TestGreaterThanUnequalLength<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_equality() {
    runTest(new ComparisonBooleanTests.TestEquality<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

}
