package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzLPSolver3Parties extends AbstractSpdzTest {

  @Test
  public void test_LPSolver_3_Sequential() {
    runTest(new LPSolverTests.TestLPSolver<>(PivotRule.DANZIG),
        PreprocessingStrategy.DUMMY, 3, 512, 150, 16);
  }
}
