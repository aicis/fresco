package dk.alexandra.fresco.lib.common.arithmetic;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ExponentiationPipeTests;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.arithmetic.AdvancedNumericTests.TestMinInfFrac;
import dk.alexandra.fresco.lib.common.arithmetic.ParallelAndSequenceTests.TestSumAndProduct;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.collections.io.CloseListTests;
import dk.alexandra.fresco.lib.common.collections.io.CloseMatrixTests;
import dk.alexandra.fresco.lib.common.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.common.collections.permute.PermuteRowsTests;
import dk.alexandra.fresco.lib.common.collections.shuffle.ShuffleRowsTests;
import dk.alexandra.fresco.lib.common.compare.CompareTests;
import dk.alexandra.fresco.lib.common.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.lib.common.conditional.ConditionalSwapNeighborsTests;
import dk.alexandra.fresco.lib.common.conditional.ConditionalSwapRowsTests;
import dk.alexandra.fresco.lib.common.conditional.SwapIfTests;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests;
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.common.math.integer.exp.ExponentiationTests;
import dk.alexandra.fresco.lib.common.math.integer.inv.InversionTests;
import dk.alexandra.fresco.lib.common.math.integer.inv.InversionTests.TestInversion;
import dk.alexandra.fresco.lib.common.math.integer.inv.InversionTests.TestInvertZero;
import dk.alexandra.fresco.lib.common.math.integer.linalg.LinAlgTests;
import dk.alexandra.fresco.lib.common.math.integer.log.LogTests;
import dk.alexandra.fresco.lib.common.math.integer.min.MinTests;
import dk.alexandra.fresco.lib.common.math.integer.sqrt.SqrtTests;
import dk.alexandra.fresco.lib.common.math.integer.stat.StatisticsTests;
import dk.alexandra.fresco.lib.common.math.polynomial.PolynomialTests;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.arithmetic.NumericLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.ArrayList;
import org.junit.Test;

public class TestArithmetic extends AbstractDummyArithmeticTest {

  @Test
  public void test_Input_Sequential() {
    runTest(new BasicArithmeticTests.TestInput<>(), new TestParameters().numParties(2));
  }

  @Test
  public void testInputFromAll() {
    runTest(new BasicArithmeticTests.TestInputFromAll<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_OutputToTarget_Sequential() {
    runTest(
        new BasicArithmeticTests.TestOutputToSingleParty<>(),
        new TestParameters().numParties(2).performanceLogging(true));
    assertThat(
        performanceLoggers
            .get(1)
            .getLoggedValues()
            .get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES),
        is((long) 0));
  }

  @Test
  public void test_AddPublicValue_Sequential() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), new TestParameters());
  }

  @Test
  public void test_KnownSInt_Sequential() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), new TestParameters());
  }

  @Test
  public void test_MultAndAdd_Sequential() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(), new TestParameters());
  }

  @Test
  public void testSumAndOutputSequential() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), new TestParameters());
  }

  @Test
  public void testSumAndProduct() {
    runTest(new TestSumAndProduct<>(), new TestParameters());
  }

  @Test
  public void test_MinInfFrac_Sequential() {
    runTest(new TestMinInfFrac<>(), new TestParameters());
  }

  @Test
  public void test_MinInfFrac_SequentialBatched() {
    runTest(new TestMinInfFrac<>(), new TestParameters());
  }

  @Test
  public void test_compareLt_Sequential() {
    runTest(new CompareTests.TestCompareLT<>(), new TestParameters());
  }

  @Test
  public void testCompareLtEdgeCasesSequential() {
    runTest(new CompareTests.TestCompareLTEdgeCases<>(), new TestParameters());
  }

  @Test
  public void test_compareEQ_Sequential() {
    runTest(new CompareTests.TestCompareEQ<>(), new TestParameters());
  }

  @Test
  public void test_compareFracEQ_Sequential() {
    runTest(new CompareTests.TestCompareFracEQ<>(), new TestParameters());
  }

  @Test
  public void testCompareEqEdgeCasesSequential() {
    runTest(new CompareTests.TestCompareEQEdgeCases<>(), new TestParameters());
  }

  @Test
  public void test_isSorted() {
    runTest(new SortingTests.TestIsSorted<>(), new TestParameters());
  }

  @Test
  public void test_compareAndSwap() {
    runTest(new SortingTests.TestCompareAndSwap<>(), new TestParameters());
  }

  @Test
  public void test_Sort() {
    runTest(new SortingTests.TestSort<>(), new TestParameters());
  }

  @Test
  public void test_Big_Sort() {
    runTest(new SortingTests.TestBigSort<>(), new TestParameters());
  }

  // lib.conditional

  @Test
  public void test_conditional_select_left() {
    runTest(ConditionalSelectTests.testSelectLeft(), new TestParameters());
  }

  @Test
  public void test_conditional_select_right() {
    runTest(ConditionalSelectTests.testSelectRight(), new TestParameters());
  }

  @Test
  public void test_swap_yes() {
    runTest(SwapIfTests.testSwapYes(), new TestParameters());
  }

  @Test
  public void test_swap_no() {
    runTest(SwapIfTests.testSwapNo(), new TestParameters());
  }

  @Test
  public void test_swap_rows_yes() {
    runTest(ConditionalSwapRowsTests.testSwapYes(), new TestParameters());
  }

  @Test
  public void test_swap_rows_no() {
    runTest(ConditionalSwapRowsTests.testSwapNo(), new TestParameters());
  }

  @Test
  public void test_swap_neighbors_yes() {
    runTest(ConditionalSwapNeighborsTests.testSwapYes(), new TestParameters());
  }

  @Test
  public void test_swap_neighbors_no() {
    runTest(ConditionalSwapNeighborsTests.testSwapNo(), new TestParameters());
  }

  // lib.collections

  @Test
  public void test_close_empty_list() {
    runTest(new CloseListTests.TestCloseEmptyList<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_list() {
    runTest(new CloseListTests.TestCloseEmptyList<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_empty_matrix() {
    runTest(new CloseMatrixTests.TestCloseEmptyMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_matrix() {
    runTest(new CloseMatrixTests.TestCloseAndOpenMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Test_Is_Sorted() {
    runTest(new SearchingTests.TestIsSorted<>(), new TestParameters());
  }

  @Test
  public void test_permute_empty_rows() {
    runTest(PermuteRowsTests.permuteEmptyRows(), new TestParameters().numParties(2));
  }

  @Test
  public void test_permute_rows() {
    runTest(PermuteRowsTests.permuteRows(), new TestParameters().numParties(2));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test_permute_rows_non_power_of_two() {
    ArrayList<ArrayList<DRes<SInt>>> fakeRows = new ArrayList<>();
    Matrix<DRes<SInt>> fakeMatrix = new Matrix<>(3, 2, fakeRows);
    new PermuteRows(() -> fakeMatrix, new int[] {}, 1, true).buildComputation(null);
  }

  @Test
  public void test_shuffle_rows_two_parties() {
    runTest(ShuffleRowsTests.shuffleRowsTwoParties(), new TestParameters().numParties(2));
  }

  @Test
  public void test_shuffle_rows_three_parties() {
    runTest(ShuffleRowsTests.shuffleRowsThreeParties(), new TestParameters().numParties(3));
  }

  @Test
  public void test_shuffle_rows_empty() {
    runTest(ShuffleRowsTests.shuffleRowsEmpty(), new TestParameters().numParties(2));
  }

  // lib.math.integer.binary
  @Test
  public void test_Right_Shift() {
    runTest(new BinaryOperationsTests.TestRightShift<>(), new TestParameters());
  }

  @Test
  public void test_Bit_Length() {
    runTest(new BinaryOperationsTests.TestBitLength<>(), new TestParameters());
  }

  @Test
  public void test_Bits() {
    runTest(new BinaryOperationsTests.TestBits<>(), new TestParameters());
  }

  @Test
  public void test_normalize_sint() {
    runTest(new BinaryOperationsTests.TestNormalizeSInt<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_truncation() {
    runTest(new BinaryOperationsTests.TestTruncation<>(), new TestParameters().numParties(2));
  }

  // Math tests

  @Test
  public void test_euclidian_division() {
    runTest(new DivisionTests.TestKnownDivisorDivision<>(), new TestParameters());
  }

  @Test
  public void test_euclidian_division_large_divisor() {
    runTest(new DivisionTests.TestKnownDivisorLargeDivisor<>(), new TestParameters());
  }

  @Test
  public void test_ss_division() {
    runTest(new DivisionTests.TestDivision<>(), new TestParameters().performanceLogging(true));
    //    assertThat(performanceLoggers.get(1).getLoggedValues()
    //        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_COMP0), is((long) 80));
  }

  @Test
  public void test_Exponentiation() {
    runTest(
        new ExponentiationTests.TestExponentiation<>(),
        new TestParameters().numParties(2).performanceLogging(true));
    assertThat(
        performanceLoggers
            .get(1)
            .getLoggedValues()
            .get(NumericLoggingDecorator.ARITHMETIC_BASIC_SUB),
        is((long) 19));
  }

  @Test
  public void test_ExponentiationOpenExponent() {
    runTest(new ExponentiationTests.TestExponentiationOpenExponent<>(), new TestParameters());
  }

  @Test
  public void test_ExponentiationOpenBase() {
    runTest(new ExponentiationTests.TestExponentiationOpenBase<>(), new TestParameters());
  }

  @Test()
  public void test_ExponentiationZeroExponent() {
    runTest(new ExponentiationTests.TestExponentiationZeroExponent<>(), new TestParameters());
  }

  @Test
  public void test_InnerProductClosed() {
    runTest(new LinAlgTests.TestInnerProductClosed<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_InnerProductClosedLinkedList() {
    runTest(new LinAlgTests.TestInnerProductLinkedList<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_InnerProductOpen() {
    runTest(new LinAlgTests.TestInnerProductOpen<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_inversion() {
    runTest(new TestInversion<>(), new TestParameters().numParties(2));
  }

  @Test(expected = RuntimeException.class)
  public void test_invert_zero() {
    runTest(new TestInvertZero<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Logarithm() {
    runTest(new LogTests.TestLogarithm<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Minimum_Protocol_2_parties() {
    runTest(
        new MinTests.TestMinimumProtocol<>(),
        new TestParameters().numParties(2).performanceLogging(true));
    //    assertThat(performanceLoggers.get(1).getLoggedValues()
    //        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), is((long) 10));
  }

  @Test
  public void test_Min_Inf_Frac_2_parties() {
    runTest(
        new MinTests.TestMinInfFraction<>(),
        new TestParameters().numParties(2).performanceLogging(true));
    //    assertThat(performanceLoggers.get(1).getLoggedValues()
    //        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), is((long) 10));
  }

  @Test
  public void test_Min_Inf_Frac_Trivial_2_parties() {
    runTest(new MinTests.TestMinInfFractionTrivial<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_sqrt() {
    runTest(new SqrtTests.TestSquareRoot<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Exiting_Variable_2_parties() {
    runTest(new StatisticsTests.TestStatistics<>(), new TestParameters().numParties(3));
  }

  @Test
  public void test_Exiting_Variable_3_parties() {
    runTest(new StatisticsTests.TestStatistics<>(), new TestParameters().numParties(3));
  }

  @Test
  public void test_Exiting_Variable_No_Mean_2_parties() {
    runTest(new StatisticsTests.TestStatisticsNoMean<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Polynomial_Evaluator_2_parties() {
    runTest(new PolynomialTests.TestPolynomialEvaluator<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_exponentiation_pipe_preprocessed() {
    runTest(new ExponentiationPipeTests.TestPreprocessedValues<>(), new TestParameters());
  }
}
