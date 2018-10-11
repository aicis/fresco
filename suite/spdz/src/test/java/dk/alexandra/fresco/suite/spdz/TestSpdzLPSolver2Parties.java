package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzLPSolver2Parties extends AbstractSpdzTest {

  @Test
  public void test_LPSolver_2_Sequential_dummy_bland() {
    runTest(new LPSolverTests.TestLPSolver<>(PivotRule.BLAND),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16);
  }

  @Test
  public void test_LPSolver_2() {

    runTest(new LPSolverTests.TestLPSolver<>(PivotRule.DANZIG),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16);
  }

  @Test
  public void test_LPSolver_2_Sequential_batched_dummy_smaller_mod() {
    runTest(new LPSolverTests.TestLPSolver<>(PivotRule.DANZIG),
        PreprocessingStrategy.DUMMY, 2, 128, 30, 8);
  }

}
