package dk.alexandra.fresco.suite.spdz;

import org.junit.Test;

import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.ParallelAndSequenceTests;

/**
 * Tests the SCE's methods to evaluate multiple applications in either sequence
 * or parallel.
 * 
 * @author Kasper Damgaard
 *
 */
public class TestParallelAndSequenceEval extends AbstractSpdzTest{

	@Test
	public void test_Sequential_evaluation() throws Exception {
		runTest(new ParallelAndSequenceTests.TestSequentialEvaluation(), EvaluationStrategy.SEQUENTIAL,
				NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
	}

	@Test
	public void test_parallel_evaluation() throws Exception {
		runTest(new ParallelAndSequenceTests.TestParallelEvaluation(), EvaluationStrategy.SEQUENTIAL,
				NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
	}
}
