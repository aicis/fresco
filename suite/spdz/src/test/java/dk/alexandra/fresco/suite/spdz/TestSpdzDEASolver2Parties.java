package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.statistics.DeaSolver;
import dk.alexandra.fresco.lib.statistics.DeaSolver.AnalysisType;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.TestDeaFixed1;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.TestDeaFixed2;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;


/**
 * Tests for the DEASolver.
 * 
 */
public class TestSpdzDEASolver2Parties extends AbstractSpdzTest {

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_minimize_1() throws Exception {
    runTest(new TestDeaFixed2<>(DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_minimize_2() throws Exception {
    runTest(new TestDeaFixed1<>(DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize_fixed_data_1() throws Exception {
    runTest(new TestDeaFixed1<>(AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize_fixed_data_2() throws Exception {
    runTest(new TestDeaFixed2<>(AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }


  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize() throws Exception {
    runTest(new RandomDataDeaTest<>(5, 1, 30, 3, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_dummy() throws Exception {
    runTest(new RandomDataDeaTest<>(2, 1, 5, 1, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

}

