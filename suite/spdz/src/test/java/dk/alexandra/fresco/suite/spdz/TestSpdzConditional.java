package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapNeighborsTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapRowsTests;
import dk.alexandra.fresco.lib.conditional.SwapIfTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzConditional extends AbstractSpdzTest {

  @Test
  public void test_conditional_select_left() throws Exception {
    runTest(ConditionalSelectTests.testSelectLeft(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_select_right() throws Exception {
    runTest(ConditionalSelectTests.testSelectRight(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_yes() throws Exception {
    runTest(SwapIfTests.testSwapYes(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_no() throws Exception {
    runTest(SwapIfTests.testSwapNo(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_rows_yes() throws Exception {
    runTest(ConditionalSwapRowsTests.testSwapYes(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_rows_no() throws Exception {
    runTest(ConditionalSwapRowsTests.testSwapNo(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_neighbors_yes() throws Exception {
    runTest(ConditionalSwapNeighborsTests.testSwapYes(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_neighbors_no() throws Exception {
    runTest(ConditionalSwapNeighborsTests.testSwapNo(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }
}
