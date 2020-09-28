package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.common.math.integer.stat.StatisticsTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzStatistics extends AbstractSpdzTest{
	
	@Test
	public void test_Exiting_Variable_2_parties() {
    runTest(new StatisticsTests.TestStatistics<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

	@Test
	public void test_Exiting_Variable_3_parties() {
    runTest(new StatisticsTests.TestStatistics<>(),
        PreprocessingStrategy.DUMMY, 3);
  }
}
