package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.ParallelAndSequenceTests.TestSumAndProduct;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

/**
 * Tests the SCE's methods to evaluate multiple applications in either sequence
 * or parallel.
 * 
 * @author Kasper Damgaard
 *
 */
public class TestParallelAndSequenceEval extends AbstractSpdzTest{

	@Test
  public void testSumAndProduct() throws Exception {
    runTest(new TestSumAndProduct<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }
}
