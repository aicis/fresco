package dk.alexandra.fresco.fixedpoint.basic;

import org.junit.Test;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.spdz.AbstractSpdzTest;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;

/**
 * Basic arithmetic tests using the SPDZ protocol suite with 2 parties. Have to
 * hardcode the number of parties for now, since the storage is currently build
 * to handle a fixed number of parties.
 */
public class TestSpdzProtocolSuite extends AbstractSpdzTest {

  @Test
  public void test_Input_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Open_to_party_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestOpenToParty<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Known() throws Exception {
    runTest(new BasicFixedPointTests.TestKnown<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_useSInt() throws Exception {
    runTest(new BasicFixedPointTests.TestUseSInt<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_AddKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestAddKnown<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_AddSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_SubtractSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestSubtractSecret<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_SubKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestSubKnown<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_SubKnown2() throws Exception {
    runTest(new BasicFixedPointTests.TestSubKnown2<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MultSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestMult<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MultKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestMultKnown<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Mults() throws Exception {
    runTest(new BasicFixedPointTests.TestMult<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DivisionSecretDivisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDiv<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DivisionKnownDivisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDivisionKnownDivisor<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_RandomElement() throws Exception {
    runTest(new BasicFixedPointTests.TestRandom<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_closeFixedMatrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseFixedMatrix<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_closeAndOpenFixedMatrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseAndOpenMatrix<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_matrixAddition() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixAddition<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_matrixMultiplication() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixMultiplication<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2, 128, 64);
  }

  @Test
  public void test_matrixScale() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixScale<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_exp() throws Exception {
    runTest(new MathTests.TestExp<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY,
        2);
  }
}
