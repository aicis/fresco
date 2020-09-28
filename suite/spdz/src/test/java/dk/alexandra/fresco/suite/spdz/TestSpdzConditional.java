package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSwapNeighborsTests;
import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSwapRowsTests;
import dk.alexandra.fresco.lib.common.math.integer.conditional.SwapIfTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzConditional extends AbstractSpdzTest {

  @Test
  public void test_conditional_select_left() {
    runTest(ConditionalSelectTests.testSelectLeft(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_conditional_select_right() {
    runTest(ConditionalSelectTests.testSelectRight(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_yes() {
    runTest(SwapIfTests.testSwapYes(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_no() {
    runTest(SwapIfTests.testSwapNo(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_rows_yes() {
    runTest(ConditionalSwapRowsTests.testSwapYes(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_rows_no() {
    runTest(ConditionalSwapRowsTests.testSwapNo(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_neighbors_yes() {
    runTest(ConditionalSwapNeighborsTests.testSwapYes(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_swap_neighbors_no() {
    runTest(ConditionalSwapNeighborsTests.testSwapNo(),
        PreprocessingStrategy.DUMMY, 2);
  }
}
