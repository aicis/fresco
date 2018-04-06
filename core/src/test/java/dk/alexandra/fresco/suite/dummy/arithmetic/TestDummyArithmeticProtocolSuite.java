package dk.alexandra.fresco.suite.dummy.arithmetic;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ExponentiationPipeTests;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.arithmetic.AdvancedNumericTests;
import dk.alexandra.fresco.lib.arithmetic.AdvancedNumericTests.TestMinInfFrac;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.arithmetic.MiMCTests;
import dk.alexandra.fresco.lib.arithmetic.ParallelAndSequenceTests.TestSumAndProduct;
import dk.alexandra.fresco.lib.arithmetic.SearchingTests;
import dk.alexandra.fresco.lib.arithmetic.SortingTests;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.io.CloseListTests;
import dk.alexandra.fresco.lib.collections.io.CloseMatrixTests;
import dk.alexandra.fresco.lib.collections.permute.PermuteRows;
import dk.alexandra.fresco.lib.collections.permute.PermuteRowsTests;
import dk.alexandra.fresco.lib.collections.relational.LeakyAggregationTests;
import dk.alexandra.fresco.lib.collections.shuffle.ShuffleRowsTests;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapNeighborsTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapRowsTests;
import dk.alexandra.fresco.lib.conditional.SwapIfTests;
import dk.alexandra.fresco.lib.debug.ArithmeticDebugTests;
import dk.alexandra.fresco.lib.list.EliminateDuplicatesTests;
import dk.alexandra.fresco.lib.lp.LPSolver;
import dk.alexandra.fresco.lib.lp.LpBuildingBlockTests;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationTests;
import dk.alexandra.fresco.lib.math.integer.linalg.LinAlgTests;
import dk.alexandra.fresco.lib.math.integer.log.LogTests;
import dk.alexandra.fresco.lib.math.integer.min.MinTests;
import dk.alexandra.fresco.lib.math.integer.sqrt.SqrtTests;
import dk.alexandra.fresco.lib.math.integer.stat.StatisticsTests;
import dk.alexandra.fresco.lib.math.polynomial.PolynomialTests;
import dk.alexandra.fresco.lib.real.BasicFixedPointTests;
import dk.alexandra.fresco.lib.real.LinearAlgebraTests;
import dk.alexandra.fresco.lib.real.MathTests;
import dk.alexandra.fresco.lib.real.TruncationTests;
import dk.alexandra.fresco.lib.statistics.CreditRaterTest;
import dk.alexandra.fresco.lib.statistics.DeaSolver.AnalysisType;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.lib.statistics.DeaSolverTests.TestDeaFixed1;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.logging.arithmetic.ComparisonLoggerDecorator;
import dk.alexandra.fresco.logging.arithmetic.NumericLoggingDecorator;
import java.util.ArrayList;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  @Test
  public void test_Input_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestInput<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_OutputToTarget_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestOutputToSingleParty<>(), new TestParameters()
        .numParties(2)
        .performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), is((long) 0));
  }

  @Test
  public void test_AddPublicValue_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), new TestParameters());
  }

  @Test
  public void test_KnownSInt_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), new TestParameters());
  }

  @Test
  public void test_MultAndAdd_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(), new TestParameters());
  }

  @Test
  public void testSumAndOutputSequential() throws Exception {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), new TestParameters());
  }

  @Test
  public void testSumAndProduct() throws Exception {
    runTest(new TestSumAndProduct<>(), new TestParameters());
  }

  @Test
  public void test_MinInfFrac_Sequential() throws Exception {
    runTest(new TestMinInfFrac<>(), new TestParameters());
  }

  @Test
  public void test_MinInfFrac_SequentialBatched() throws Exception {
    runTest(new TestMinInfFrac<>(), new TestParameters());
  }

  @Test
  public void test_compareLt_Sequential() throws Exception {
    runTest(new CompareTests.TestCompareLT<>(), new TestParameters());
  }

  @Test
  public void testCompareLtEdgeCasesSequential() throws Exception {
    runTest(new CompareTests.TestCompareLTEdgeCases<>(), new TestParameters());
  }

  @Test
  public void test_compareEQ_Sequential() throws Exception {
    runTest(new CompareTests.TestCompareEQ<>(), new TestParameters());
  }

  @Test
  public void testCompareEqEdgeCasesSequential() throws Exception {
    runTest(new CompareTests.TestCompareEQEdgeCases<>(), new TestParameters());
  }

  @Test
  public void test_isSorted() throws Exception {
    runTest(new SortingTests.TestIsSorted<>(), new TestParameters());
  }

  @Test
  public void test_compareAndSwap() throws Exception {
    runTest(new SortingTests.TestCompareAndSwap<>(), new TestParameters());
  }

  @Test
  public void test_Sort() throws Exception {
    runTest(new SortingTests.TestSort<>(), new TestParameters());
  }

  @Test
  public void test_Big_Sort() throws Exception {
    runTest(new SortingTests.TestBigSort<>(), new TestParameters());
  }

  // Statistics


  // Creditrater
  @Test
  public void test_CreditRater_Single_Value_2_parties() throws Exception {
    int[] values = {2};
    int[][] intervals = {{1, 3}};
    int[][] scores = {{10, 100, 1000}};
    runTest(new CreditRaterTest.TestCreditRater<>(values, intervals, scores),
        new TestParameters());
  }

  @Test
  public void test_CreditRater_Single_Value_3_parties() throws Exception {
    int[] values = {2};
    int[][] intervals = {{1, 3}};
    int[][] scores = {{10, 100, 1000}};
    runTest(new CreditRaterTest.TestCreditRater<>(values, intervals, scores),
        new TestParameters());
  }

  @Test
  public void test_CreditRater_multi_Value_2_parties() throws Exception {
    int[] values = {2, 2, 2};
    int[][] intervals = {{1, 3}, {0, 5}, {0, 1}};
    int[][] scores = {{10, 100, 1000}, {10, 100, 1000}, {10, 100, 1000}};
    runTest(new CreditRaterTest.TestCreditRater<>(values, intervals, scores),
        new TestParameters());
  }

  // DEASolver
  @Test
  public void test_DeaSolver_2_parties() throws Exception {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_3_parties() throws Exception {
    runTest(new RandomDataDeaTest<>(2, 2, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters().numParties(3));
  }

  @Test
  public void test_DeaSolver_multiple_queries_2_parties() throws Exception {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 2, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_single_input_2_parties() throws Exception {
    runTest(new RandomDataDeaTest<>(1, 2, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_single_input_and_output_2_parties() throws Exception {
    runTest(new RandomDataDeaTest<>(1, 1, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_output_efficiency_2_parties() throws Exception {
    runTest(new RandomDataDeaTest<>(5, 1, 10, 1, AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_multiple_queries__output_2_parties() throws Exception {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 2, AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_fixedData1() throws Exception {
    runTest(new TestDeaFixed1<>(AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

  // lib.conditional

  @Test
  public void test_conditional_select_left() throws Exception {
    runTest(ConditionalSelectTests.testSelectLeft(), new TestParameters());
  }

  @Test
  public void test_conditional_select_right() throws Exception {
    runTest(ConditionalSelectTests.testSelectRight(), new TestParameters());
  }

  @Test
  public void test_swap_yes() throws Exception {
    runTest(SwapIfTests.testSwapYes(), new TestParameters());
  }

  @Test
  public void test_swap_no() throws Exception {
    runTest(SwapIfTests.testSwapNo(), new TestParameters());
  }

  @Test
  public void test_swap_rows_yes() throws Exception {
    runTest(ConditionalSwapRowsTests.testSwapYes(), new TestParameters());
  }

  @Test
  public void test_swap_rows_no() throws Exception {
    runTest(ConditionalSwapRowsTests.testSwapNo(), new TestParameters());
  }

  @Test
  public void test_swap_neighbors_yes() throws Exception {
    runTest(ConditionalSwapNeighborsTests.testSwapYes(), new TestParameters());
  }

  @Test
  public void test_swap_neighbors_no() throws Exception {
    runTest(ConditionalSwapNeighborsTests.testSwapNo(), new TestParameters());
  }

  // lib.collections

  @Test
  public void test_close_empty_list() throws Exception {
    runTest(new CloseListTests.TestCloseEmptyList<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_list() throws Exception {
    runTest(new CloseListTests.TestCloseEmptyList<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_empty_matrix() throws Exception {
    runTest(new CloseMatrixTests.TestCloseEmptyMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_close_matrix() throws Exception {
    runTest(new CloseMatrixTests.TestCloseAndOpenMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Test_Is_Sorted() throws Exception {
    runTest(new SearchingTests.TestIsSorted<>(), new TestParameters());
  }

  @Test
  public void test_permute_empty_rows() throws Exception {
    runTest(PermuteRowsTests.permuteEmptyRows(), new TestParameters().numParties(2));
  }

  @Test
  public void test_permute_rows() throws Exception {
    runTest(PermuteRowsTests.permuteRows(), new TestParameters().numParties(2));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test_permute_rows_non_power_of_two() throws Throwable {
    ArrayList<ArrayList<DRes<SInt>>> fakeRows = new ArrayList<>();
    Matrix<DRes<SInt>> fakeMatrix = new Matrix<>(3, 2, fakeRows);
    new PermuteRows(() -> fakeMatrix, new int[] {}, 1, true).buildComputation(null);
  }

  @Test
  public void test_shuffle_rows_two_parties() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsTwoParties(), new TestParameters().numParties(2));
  }

  @Test
  public void test_shuffle_rows_three_parties() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsThreeParties(), new TestParameters().numParties(3));
  }

  @Test
  public void test_shuffle_rows_empty() throws Exception {
    runTest(ShuffleRowsTests.shuffleRowsEmpty(), new TestParameters().numParties(2));
  }

  @Test
  public void test_leaky_aggregate_two() throws Exception {
    runTest(LeakyAggregationTests.aggregate(), new TestParameters().numParties(2));
  }

  @Test
  public void test_leaky_aggregate_unique_keys_two() throws Exception {
    runTest(LeakyAggregationTests.aggregateUniqueKeys(), new TestParameters().numParties(2));
  }

  @Test
  public void test_leaky_aggregate_three() throws Exception {
    runTest(LeakyAggregationTests.aggregate(), new TestParameters().numParties(3));
  }

  @Test
  public void test_leaky_aggregate_empty() throws Exception {
    runTest(LeakyAggregationTests.aggregateEmpty(), new TestParameters().numParties(2));
  }

  //

  @Test
  public void test_MiMC_DifferentPlainTexts() throws Exception {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(), new TestParameters());
  }

  @Test
  public void test_MiMC_EncSameEnc() throws Exception {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(), new TestParameters());
  }

  @Test
  public void test_MiMC_EncDec() throws Exception {
    runTest(new MiMCTests.TestMiMCEncDec<>(), new TestParameters()
        .modulus(ModulusFinder.findSuitableModulus(512)));
  }

  @Test
  public void test_MiMC_EncDecFixedRounds() throws Exception {
    runTest(new MiMCTests.TestMiMCEncDecFixedRounds<>(), new TestParameters()
        .modulus(ModulusFinder.findSuitableModulus(512)));
  }

  @Test
  public void test_MiMC_Deterministically() throws Exception {
    runTest(new MiMCTests.TestMiMCEncryptsDeterministically<>(), new TestParameters()
        .modulus(ModulusFinder.findSuitableModulus(512)));
  }

  // lib.list
  @Test
  public void test_findDuplicatesOne() throws Exception {
    runTest(new EliminateDuplicatesTests.TestFindDuplicatesOne<>(),
        new TestParameters().numParties(2));
  }

  // lib.lp
  @Test
  public void test_LpSolverEntering() throws Exception {
    runTest(new LpBuildingBlockTests.TestEnteringVariable<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverBlandEntering() throws Exception {
    runTest(new LpBuildingBlockTests.TestBlandEnteringVariable<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_LpTableauDebug() throws Exception {
    runTest(new LpBuildingBlockTests.TestLpTableuDebug<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverDanzig() throws Exception {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.DANZIG),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverDanzigSmallerMod() throws Exception {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.DANZIG),
        new TestParameters()
        .numParties(2)
        .modulus(ModulusFinder.findSuitableModulus(128))
        .maxBitLength(30)
        .fixedPointPrecesion(8)
        .performanceLogging(false));
  }

  @Test
  public void test_LpSolverBland() throws Exception {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.BLAND),
        new TestParameters().numParties(2).performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_EQ), is((long) 33));
  }

  @Test
  public void test_LpSolverDebug() throws Exception {
    runTest(new LpBuildingBlockTests.TestLpSolverDebug<>(), new TestParameters().numParties(2));
  }


  // lib.math.integer.binary
  @Test
  public void test_Right_Shift() throws Exception {
    runTest(new BinaryOperationsTests.TestRightShift<>(), new TestParameters());
  }

  @Test
  public void test_Bit_Length() throws Exception {
    runTest(new BinaryOperationsTests.TestBitLength<>(), new TestParameters());
  }

  @Test
  public void test_Bits() throws Exception {
    runTest(new BinaryOperationsTests.TestBits<>(), new TestParameters());
  }

  // Math tests

  @Test
  public void test_euclidian_division() throws Exception {
    runTest(new DivisionTests.TestKnownDivisorDivision<>(), new TestParameters());
  }

  @Test
  public void test_euclidian_division_large_divisor() throws Exception {
    runTest(new DivisionTests.TestKnownDivisorLargeDivisor<>(),
        new TestParameters());
  }

  @Test(expected = RuntimeException.class)
  public void test_euclidian_division_too_large_divisor() throws Exception {
    runTest(new DivisionTests.TestKnowndivisorTooLargeDivisor<>(),
        new TestParameters());
  }

  @Test
  public void test_ss_division() throws Exception {
    runTest(new DivisionTests.TestDivision<>(), new TestParameters().performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_COMP0), is((long) 80));
  }

  @Test
  public void test_Modulus() throws Exception {
    runTest(new AdvancedNumericTests.TestModulus<>(), new TestParameters());
  }

  @Test
  public void test_Exponentiation() throws Exception {
    runTest(new ExponentiationTests.TestExponentiation<>(),
        new TestParameters().numParties(2).performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(NumericLoggingDecorator.ARITHMETIC_BASIC_SUB), is((long) 19));
  }


  @Test
  public void test_ExponentiationOpenExponent() throws Exception {
    runTest(new ExponentiationTests.TestExponentiationOpenExponent<>(),
        new TestParameters());
  }

  @Test
  public void test_ExponentiationOpenBase() throws Exception {
    runTest(new ExponentiationTests.TestExponentiationOpenBase<>(),
        new TestParameters());
  }

  @Test()
  public void test_ExponentiationZeroExponent() throws Exception {
    runTest(new ExponentiationTests.TestExponentiationZeroExponent<>(),
        new TestParameters());
  }

  @Test
  public void test_InnerProductClosed() throws Exception {
    runTest(new LinAlgTests.TestInnerProductClosed<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_InnerProductOpen() throws Exception {
    runTest(new LinAlgTests.TestInnerProductOpen<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Logarithm() throws Exception {
    runTest(new LogTests.TestLogarithm<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Minimum_Protocol_2_parties() throws Exception {
    runTest(new MinTests.TestMinimumProtocol<>(),
        new TestParameters()
        .numParties(2)
        .performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), is((long) 10));
  }

  @Test
  public void test_Min_Inf_Frac_2_parties() throws Exception {
    runTest(new MinTests.TestMinInfFraction<>(),
        new TestParameters()
        .numParties(2)
        .performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), is((long) 10));
  }

  @Test
  public void test_Min_Inf_Frac_Trivial_2_parties() throws Exception {
    runTest(new MinTests.TestMinInfFractionTrivial<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_sqrt() throws Exception {
    runTest(new SqrtTests.TestSquareRoot<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Exiting_Variable_2_parties() throws Exception {
    runTest(new StatisticsTests.TestStatistics<>(), new TestParameters().numParties(3));
  }

  @Test
  public void test_Exiting_Variable_3_parties() throws Exception {
    runTest(new StatisticsTests.TestStatistics<>(), new TestParameters().numParties(3));
  }

  @Test
  public void test_Exiting_Variable_No_Mean_2_parties() throws Exception {
    runTest(new StatisticsTests.TestStatisticsNoMean<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Polynomial_Evaluator_2_parties() throws Exception {
    runTest(new PolynomialTests.TestPolynomialEvaluator<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_debug_tools() throws Exception {
    runTest(new ArithmeticDebugTests.TestArithmeticOpenAndPrint<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_exponentiation_pipe_preprocessed() throws Exception {
    runTest(new ExponentiationPipeTests.TestPreprocessedValues<>(), new TestParameters());
  }

  @Test
  public void test_Real_Input_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestInput<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Open_to_party_Sequential() throws Exception {
    runTest(new BasicFixedPointTests.TestOpenToParty<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Known() throws Exception {
    runTest(new BasicFixedPointTests.TestKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Use_SInt() throws Exception {
    runTest(new BasicFixedPointTests.TestUseSInt<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Add_Known() throws Exception {
    runTest(new BasicFixedPointTests.TestAddKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Add_Secret() throws Exception {
    runTest(new BasicFixedPointTests.TestAdd<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Subtract_Secret() throws Exception {
    runTest(new BasicFixedPointTests.TestSubtractSecret<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Sub_Known() throws Exception {
    runTest(new BasicFixedPointTests.TestSubKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Mult_Known() throws Exception {
    runTest(new BasicFixedPointTests.TestMultKnown<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Mults() throws Exception {
    runTest(new BasicFixedPointTests.TestMult<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Repeated_Multiplication() throws Exception {
    runTest(new BasicFixedPointTests.TestRepeatedMultiplication<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Secret_Divisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDiv<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Known_Divisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDivisionKnownDivisor<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Known_Negative_Divisor() throws Exception {
    runTest(new BasicFixedPointTests.TestDivisionKnownNegativeDivisor<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Close_Real_Matrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseFixedMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Close_And_Open_Real_Matrix() throws Exception {
    runTest(new LinearAlgebraTests.TestCloseAndOpenMatrix<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Addition() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixAddition<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Multiplication() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixMultiplication<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Scale() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixScale<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Matrix_Operate() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixOperate<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Vector_Multiplication_Unmatched() throws Exception {
    runTest(new LinearAlgebraTests.TestVectorMultUnmatchedDimensions<>(),
        new TestParameters());
  }

  @Test
  public void test_Real_Matrix_Multiplication_Unmatched() throws Exception {
    runTest(new LinearAlgebraTests.TestMatrixMultUnmatchedDimensions<>(),
        new TestParameters());
  }

  @Test
  public void test_Real_Matrix_Addition_Unmatched() throws Exception {
    runTest(new LinearAlgebraTests.TestAdditionUnmatchedDimensions<>(),
        new TestParameters());
  }

  @Test
  public void test_Real_Exp() throws Exception {
    runTest(new MathTests.TestExp<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Random_Element() throws Exception {
    runTest(new MathTests.TestRandom<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Leq() throws Exception {
    runTest(new BasicFixedPointTests.TestLeq<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Log() throws Exception {
    runTest(new MathTests.TestLog<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Sqrt() throws Exception {
    runTest(new MathTests.TestSqrt<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Sum() throws Exception {
    runTest(new MathTests.TestSum<>(), new TestParameters());
  }

  @Test
  public void test_inner_product() throws Exception {
    runTest(new MathTests.TestInnerProduct<>(), new TestParameters());
  }

  @Test
  public void test_inner_product_known_part() throws Exception {
    runTest(new MathTests.TestInnerProductPublicPart<>(), new TestParameters());
  }

  @Test
  public void test_inner_product_unmatched_dimensions() throws Exception {
    runTest(new MathTests.TestInnerProductUnmatchedDimensions<>(), new TestParameters());
  }

  @Test
  public void test_inner_product_known_part_unmatched() throws Exception {
    runTest(new MathTests.TestInnerProductPublicPartUnmatched<>(), new TestParameters());
  }

  @Test
  public void test_Real_Sqrt_Uneven_Precision() throws Exception {
    runTest(new MathTests.TestSqrt<>(),
        new TestParameters()
        .fixedPointPrecesion(BasicFixedPointTests.DEFAULT_PRECISION + 1));
  }

  @Test
  public void test_trunctation() throws Exception {
    runTest(new TruncationTests.TestTruncation<>(), new TestParameters().numParties(2));
  }

}
