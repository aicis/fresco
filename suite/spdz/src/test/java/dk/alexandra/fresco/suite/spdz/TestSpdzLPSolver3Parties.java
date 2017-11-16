package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzLPSolver3Parties extends AbstractSpdzTest {

	@Test
	public void test_LPSolver_3_Sequential() throws Exception {
    runTest(new LPSolverTests.TestLPSolver<>(PivotRule.DANZIG),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 3);
  }
}
