package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class AbstractFixedDummyArithmeticTest extends AbstractDummyArithmeticTest {

  @Test
  public void test_Input_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_Open_to_party_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestOpenToParty<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_Known() throws Exception {
    runTest(new BasicFixedPointTests.TestKnown<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_useSInt() throws Exception {
    runTest(new BasicFixedPointTests.TestUseSInt<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_AddKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestAddKnown<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_AddSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_SubtractSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestSubtractSecret<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_SubKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestSubKnown<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_MultKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestMultKnown<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_Mults() throws Exception {
    runTest(new BasicFixedPointTests.TestMult<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_repeated_Multiplication() throws Exception {
    runTest(new BasicFixedPointTests.TestRepeatedMultiplication<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_DivisionSecretDivisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDiv<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_DivisionKnownDivisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDivisionKnownDivisor<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_closeFixedMatrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseFixedMatrix<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_closeAndOpenFixedMatrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseAndOpenMatrix<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_matrixAddition() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixAddition<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_matrixMultiplication() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixMultiplication<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_matrixScale() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixScale<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_matrixOperate() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixOperate<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_exp() throws Exception {
    runTest(new MathTests.TestExp<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_RandomElement() throws Exception {
    runTest(new MathTests.TestRandom<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_leq() throws Exception {
    runTest(new BasicFixedPointTests.TestLeq<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_log() throws Exception {
    runTest(new MathTests.TestLog<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_sqrt() throws Exception {
    runTest(new MathTests.TestSqrt<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_trunctation() throws Exception {
    runTest(new TruncationTests.TestTruncation<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }
}
