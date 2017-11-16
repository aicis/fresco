package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.math.integer.stat.StatisticsTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzStatistics extends AbstractSpdzTest{
	
	@Test
	public void test_Exiting_Variable_2_parties() throws Exception {		
    runTest(new StatisticsTests.TestStatistics<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

	@Test
	public void test_Exiting_Variable_3_parties() throws Exception {
    runTest(new StatisticsTests.TestStatistics<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 3);
  }
}
