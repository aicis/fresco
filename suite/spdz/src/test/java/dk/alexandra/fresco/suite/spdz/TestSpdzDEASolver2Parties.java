package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.statistics.DeaSolver;
import dk.alexandra.fresco.lib.statistics.DeaSolver.AnalysisType;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.TestDeaFixed1;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.TestDeaFixed2;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.util.Random;
import org.junit.Test;


/**
 * Tests for the DEASolver.
 */
public class TestSpdzDEASolver2Parties extends AbstractSpdzTest {

  @Test
  public void test_DEASolver_2_dummy_minimize_1() {
    runTest(new TestDeaFixed2<>(DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }

  @Test
  public void test_DEASolver_2_minimize_2() {
    runTest(new TestDeaFixed1<>(DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }

  @Test
  public void test_DEASolver_2_maximize_fixed_data_1() {
    runTest(new TestDeaFixed1<>(AnalysisType.OUTPUT_EFFICIENCY),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }

  @Test
  public void test_DEASolver_2_maximize_fixed_data_2() {
    runTest(new TestDeaFixed2<>(AnalysisType.OUTPUT_EFFICIENCY),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }


  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize() {
    runTest(new RandomDataDeaTest<>(5, 1, 30, 3, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }

  @Test
  public void test_DEASolver_2() {
    runTest(new RandomDataDeaTest<>(2, 1, 5, 1, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }

  @Test
  public void test_DEASolver_2_NoIterations() {
    runTest(
        new RandomDataDeaTest<>(2, 1, 5, 1, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY, new Random(),
            1, true),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16,
        BasicNumericContext.DEFAULT_STATISTICAL_SECURITY);
  }

}

