package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public abstract class RealAbstractDummyArithmeticTest extends AbstractDummyArithmeticTest {

  abstract RealNumericProvider getProvider();

  @Test
  public void test_Input_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestInput<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_Open_to_party_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestOpenToParty<>(getProvider()),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_Known() throws Exception {
    runTest(new BasicFixedPointTests.TestKnown<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_useSInt() throws Exception {
    runTest(new BasicFixedPointTests.TestUseSInt<>(getProvider()), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_AddKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestAddKnown<>(getProvider()), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_AddSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestAdd<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_SubtractSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestSubtractSecret<>(getProvider()),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_SubKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestSubKnown<>(getProvider()), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_MultSecret() throws Exception {
    runTest(new BasicFixedPointTests.TestMult<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_MultKnown() throws Exception {
    runTest(new BasicFixedPointTests.TestMultKnown<>(getProvider()), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_Mults() throws Exception {
    runTest(new BasicFixedPointTests.TestMult<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_DivisionSecretDivisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDiv<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_DivisionKnownDivisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDivisionKnownDivisor<>(getProvider()),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_closeFixedMatrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseFixedMatrix<>(getProvider()),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_closeAndOpenFixedMatrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseAndOpenMatrix<>(getProvider()),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_matrixAddition() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixAddition<>(getProvider()),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_matrixMultiplication() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixMultiplication<>(getProvider()),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_matrixScale() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixScale<>(getProvider()), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_exp() throws Exception {
    runTest(new MathTests.TestExp<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_RandomElement() throws Exception {
    runTest(new MathTests.TestRandom<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_leq() throws Exception {
    runTest(new MathTests.TestLeq<>(getProvider()), EvaluationStrategy.SEQUENTIAL, 2);
  }

}
