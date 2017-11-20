package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.SearchingTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestLookUpProtocol extends AbstractSpdzTest{
	
	@Test
	public void test_lookup_is_sorted() throws Exception {
    runTest(new SearchingTests.TestIsSorted<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }


}
