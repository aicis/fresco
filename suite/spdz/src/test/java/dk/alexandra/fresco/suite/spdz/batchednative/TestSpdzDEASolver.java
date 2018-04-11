package dk.alexandra.fresco.suite.spdz.batchednative;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.statistics.DeaSolver.AnalysisType;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.TestDeaFixed1;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.TestDeaFixed2;
import dk.alexandra.fresco.suite.spdz.AbstractSpdzTest;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzDEASolver extends AbstractSpdzTest {

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_minimize_1() {
    runTest(new TestDeaFixed2<>(AnalysisType.INPUT_EFFICIENCY),
        EvaluationStrategy.NATIVE_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_minimize_2() {
    runTest(new TestDeaFixed1<>(AnalysisType.INPUT_EFFICIENCY),
        EvaluationStrategy.NATIVE_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize_fixed_data_1() {
    runTest(new TestDeaFixed1<>(AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.NATIVE_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize_fixed_data_2() {
    runTest(new TestDeaFixed2<>(AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.NATIVE_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize() {
    runTest(new RandomDataDeaTest<>(5, 1, 30, 3, AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.NATIVE_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

}

