package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.builder.numeric.ExponentiationPipeTests;
import dk.alexandra.fresco.lib.list.EliminateDuplicatesTests;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestReal extends AbstractDummyArithmeticTest {

  @Test
  public void test_Real_Input_Sequential() {
    runTest(new BasicFixedPointTests.TestInput<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Open_to_party_Sequential() {
    runTest(new BasicFixedPointTests.TestOpenToParty<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Known() {
    runTest(new BasicFixedPointTests.TestKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Use_SInt() {
    runTest(new BasicFixedPointTests.TestUseSInt<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Add_Known() {
    runTest(new BasicFixedPointTests.TestAddKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Add_Secret() {
    runTest(new BasicFixedPointTests.TestAdd<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Subtract_Secret() {
    runTest(new BasicFixedPointTests.TestSubtractSecret<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Sub_Known() {
    runTest(new BasicFixedPointTests.TestSubKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Mult_Known() {
    runTest(new BasicFixedPointTests.TestMultKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Mults() {
    runTest(new BasicFixedPointTests.TestMult<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Repeated_Multiplication() {
    runTest(
        new BasicFixedPointTests.TestRepeatedMultiplication<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Secret_Divisor() {
    runTest(new BasicFixedPointTests.TestDiv<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Known_Divisor() {
    runTest(
        new BasicFixedPointTests.TestDivisionKnownDivisor<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Known_Negative_Divisor() {
    runTest(
        new BasicFixedPointTests.TestDivisionKnownNegativeDivisor<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Close_Real_Matrix() {
    runTest(new LinearAlgebraTests.TestCloseFixedMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Close_And_Open_Real_Matrix() {
    runTest(new LinearAlgebraTests.TestCloseAndOpenMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Addition() {
    runTest(new LinearAlgebraTests.TestMatrixAddition<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Subtraction() {
    runTest(new LinearAlgebraTests.TestMatrixSubtraction<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Multiplication() {
    runTest(
        new LinearAlgebraTests.TestMatrixMultiplication<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Scale() {
    runTest(new LinearAlgebraTests.TestMatrixScale<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Operate() {
    runTest(new LinearAlgebraTests.TestMatrixOperate<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Vector_Multiplication_Unmatched() {
    runTest(new LinearAlgebraTests.TestVectorMultUnmatchedDimensions<>(), new TestParameters());
  }

  @Test
  public void test_Real_Matrix_Multiplication_Unmatched() {
    runTest(new LinearAlgebraTests.TestMatrixMultUnmatchedDimensions<>(), new TestParameters());
  }

  @Test
  public void test_Real_Matrix_Addition_Unmatched() {
    runTest(new LinearAlgebraTests.TestAdditionUnmatchedDimensions<>(), new TestParameters());
  }

  @Test
  public void test_Real_Matrix_Transpose() {
    runTest(new LinearAlgebraTests.TestTransposeMatrix<>(), new TestParameters());
  }

  @Test
  public void test_Real_Exp() {
    runTest(new MathTests.TestExp<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Random_Element() {
    runTest(new MathTests.TestRandom<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Leq() {
    runTest(new BasicFixedPointTests.TestLeq<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Log() {
    runTest(new MathTests.TestLog<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Sqrt() {
    runTest(new MathTests.TestSqrt<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Sum() {
    runTest(new MathTests.TestSum<>(), new TestParameters());
  }

  @Test
  public void test_inner_product() {
    runTest(new MathTests.TestInnerProduct<>(), new TestParameters());
  }

  @Test
  public void test_inner_product_known_part() {
    runTest(new MathTests.TestInnerProductPublicPart<>(), new TestParameters());
  }

  @Test
  public void test_inner_product_unmatched_dimensions() {
    runTest(new MathTests.TestInnerProductUnmatchedDimensions<>(), new TestParameters());
  }

  @Test
  public void test_inner_product_known_part_unmatched() {
    runTest(new MathTests.TestInnerProductPublicPartUnmatched<>(), new TestParameters());
  }

  @Test
  public void test_normalize_sreal() {
    runTest(new NormalizeTests.TestNormalizeSReal<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_normalize_power_sreal() {
    runTest(new NormalizeTests.TestNormalizePowerSReal<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_reciprocal() {
    runTest(new MathTests.TestReciprocal<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_two_power() {
    runTest(new MathTests.TestTwoPower<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_real_sign() {
    runTest(new MathTests.TestRealSign<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_constant_real_polynomial() {
    runTest(new MathTests.TestConstantPolynomial<>(), new TestParameters().numParties(2));
  }
}
