package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.lib.statistics.DeaSolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.Random;
import org.junit.Test;

public class TestDeaSolverAndCreditRater extends AbstractDummyArithmeticTest {

  // CreditRater
  @Test
  public void test_CreditRater_Single_Value_2_parties() {
    int[] values = {2};
    int[][] intervals = {{1, 3}};
    int[][] scores = {{10, 100, 1000}};
    runTest(new CreditRaterTest.TestCreditRater<>(values, intervals, scores),
        new TestParameters());
  }

  @Test
  public void test_CreditRater_Single_Value_3_parties() {
    int[] values = {2};
    int[][] intervals = {{1, 3}};
    int[][] scores = {{10, 100, 1000}};
    runTest(new CreditRaterTest.TestCreditRater<>(values, intervals, scores),
        new TestParameters());
  }

  @Test
  public void test_CreditRater_multi_Value_2_parties() {
    int[] values = {2, 2, 2};
    int[][] intervals = {{1, 3}, {0, 5}, {0, 1}};
    int[][] scores = {{10, 100, 1000}, {10, 100, 1000}, {10, 100, 1000}};
    runTest(new CreditRaterTest.TestCreditRater<>(values, intervals, scores),
        new TestParameters());
  }

  // DEASolver
  @Test
  public void test_DEASolver_2_Sequential_dummy_NoIterations() {
    runTest(
        new RandomDataDeaTest<>(2, 1, 5, 1, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY, new Random(),
            1, true),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_DeaSolver_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 1, DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_3_parties() {
    runTest(new RandomDataDeaTest<>(2, 2, 10, 1, DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        new TestParameters().numParties(3));
  }

  @Test
  public void test_DeaSolver_multiple_queries_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 2, DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_single_input_2_parties() {
    runTest(new RandomDataDeaTest<>(1, 2, 10, 1, DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_single_input_and_output_2_parties() {
    runTest(new RandomDataDeaTest<>(1, 1, 10, 1, DeaSolver.AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_output_efficiency_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 1, 10, 1, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_multiple_queries__output_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 2, DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_fixedData1() {
    runTest(new DeaSolverTests.TestDeaFixed1<>(DeaSolver.AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

}
