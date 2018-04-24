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
import dk.alexandra.fresco.lib.compare.CompareTests.TestLessThanLogRounds;
import dk.alexandra.fresco.lib.compare.lt.BitLessThanOpenTests.TestBitLessThanOpen;
import dk.alexandra.fresco.lib.compare.lt.CarryOutTests.TestCarryOut;
import dk.alexandra.fresco.lib.compare.lt.LessThanZeroTests.TestLessThanZero;
import dk.alexandra.fresco.lib.compare.lt.PreCarryTests.TestCarryHelper;
import dk.alexandra.fresco.lib.compare.lt.PreCarryTests.TestPreCarryBits;
import dk.alexandra.fresco.lib.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapNeighborsTests;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapRowsTests;
import dk.alexandra.fresco.lib.conditional.SwapIfTests;
import dk.alexandra.fresco.lib.debug.ArithmeticDebugTests;
import dk.alexandra.fresco.lib.list.EliminateDuplicatesTests;
import dk.alexandra.fresco.lib.lp.LPSolver;
import dk.alexandra.fresco.lib.lp.LpBuildingBlockTests;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests.TestArithmeticAndKnownRight;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests.TestArithmeticXorKnownRight;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests.TestGenerateRandomBitMask;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationTests;
import dk.alexandra.fresco.lib.math.integer.linalg.LinAlgTests;
import dk.alexandra.fresco.lib.math.integer.log.LogTests;
import dk.alexandra.fresco.lib.math.integer.min.MinTests;
import dk.alexandra.fresco.lib.math.integer.mod.Mod2mTests.TestMod2mBaseCase;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

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
    runTest(new BasicArithmeticTests.TestOutputToSingleParty<>(), new TestParameters()
        .numParties(2)
        .performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), is((long) 0));
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

  // Statistics


  // Creditrater
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
  public void test_DeaSolver_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_3_parties() {
    runTest(new RandomDataDeaTest<>(2, 2, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters().numParties(3));
  }

  @Test
  public void test_DeaSolver_multiple_queries_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 2, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_single_input_2_parties() {
    runTest(new RandomDataDeaTest<>(1, 2, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DeaSolver_single_input_and_output_2_parties() {
    runTest(new RandomDataDeaTest<>(1, 1, 10, 1, AnalysisType.INPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_output_efficiency_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 1, 10, 1, AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_multiple_queries__output_2_parties() {
    runTest(new RandomDataDeaTest<>(5, 2, 10, 2, AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
  }

  @Test
  public void test_DEASolver_fixedData1() {
    runTest(new TestDeaFixed1<>(AnalysisType.OUTPUT_EFFICIENCY),
        new TestParameters());
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
    new PermuteRows(() -> fakeMatrix, new int[]{}, 1, true).buildComputation(null);
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

  @Test
  public void test_leaky_aggregate_two() {
    runTest(LeakyAggregationTests.aggregate(), new TestParameters().numParties(2));
  }

  @Test
  public void test_leaky_aggregate_unique_keys_two() {
    runTest(LeakyAggregationTests.aggregateUniqueKeys(), new TestParameters().numParties(2));
  }

  @Test
  public void test_leaky_aggregate_three() {
    runTest(LeakyAggregationTests.aggregate(), new TestParameters().numParties(3));
  }

  @Test
  public void test_leaky_aggregate_empty() {
    runTest(LeakyAggregationTests.aggregateEmpty(), new TestParameters().numParties(2));
  }

  //

  @Test
  public void test_MiMC_DifferentPlainTexts() {
    runTest(new MiMCTests.TestMiMCDifferentPlainTexts<>(), new TestParameters());
  }

  @Test
  public void test_MiMC_EncSameEnc() {
    runTest(new MiMCTests.TestMiMCEncSameEnc<>(), new TestParameters());
  }

  @Test
  public void test_MiMC_EncDec() {
    runTest(new MiMCTests.TestMiMCEncDec<>(), new TestParameters()
        .modulus(ModulusFinder.findSuitableModulus(512)));
  }

  @Test
  public void test_MiMC_EncDecFixedRounds() {
    runTest(new MiMCTests.TestMiMCEncDecFixedRounds<>(), new TestParameters()
        .modulus(ModulusFinder.findSuitableModulus(512)));
  }

  @Test
  public void test_MiMC_Deterministically() {
    runTest(new MiMCTests.TestMiMCEncryptsDeterministically<>(), new TestParameters()
        .modulus(ModulusFinder.findSuitableModulus(512)));
  }

  // lib.list
  @Test
  public void test_findDuplicatesOne() {
    runTest(new EliminateDuplicatesTests.TestFindDuplicatesOne<>(),
        new TestParameters().numParties(2));
  }

  // lib.lp
  @Test
  public void test_LpSolverEntering() {
    runTest(new LpBuildingBlockTests.TestEnteringVariable<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverBlandEntering() {
    runTest(new LpBuildingBlockTests.TestBlandEnteringVariable<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_LpTableauDebug() {
    runTest(new LpBuildingBlockTests.TestLpTableuDebug<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverDanzig() {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.DANZIG),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_LpSolverDanzigSmallerMod() {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.DANZIG),
        new TestParameters()
            .numParties(2)
            .modulus(ModulusFinder.findSuitableModulus(128))
            .maxBitLength(30)
            .fixedPointPrecesion(8)
            .performanceLogging(false));
  }

  @Test
  public void test_LpSolverBland() {
    runTest(new LpBuildingBlockTests.TestLpSolver<>(LPSolver.PivotRule.BLAND),
        new TestParameters().numParties(2).performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_EQ), is((long) 33));
  }

  @Test
  public void test_LpSolverDebug() {
    runTest(new LpBuildingBlockTests.TestLpSolverDebug<>(), new TestParameters().numParties(2));
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

  // Math tests

  @Test
  public void test_euclidian_division() {
    runTest(new DivisionTests.TestKnownDivisorDivision<>(), new TestParameters());
  }

  @Test
  public void test_euclidian_division_large_divisor() {
    runTest(new DivisionTests.TestKnownDivisorLargeDivisor<>(),
        new TestParameters());
  }

  @Test(expected = RuntimeException.class)
  public void test_euclidian_division_too_large_divisor() {
    runTest(new DivisionTests.TestKnowndivisorTooLargeDivisor<>(),
        new TestParameters());
  }

  @Test
  public void test_ss_division() {
    runTest(new DivisionTests.TestDivision<>(), new TestParameters().performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_COMP0), is((long) 80));
  }

  @Test
  public void test_Modulus() {
    runTest(new AdvancedNumericTests.TestModulus<>(), new TestParameters());
  }

  @Test
  public void test_Exponentiation() {
    runTest(new ExponentiationTests.TestExponentiation<>(),
        new TestParameters().numParties(2).performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(NumericLoggingDecorator.ARITHMETIC_BASIC_SUB), is((long) 19));
  }


  @Test
  public void test_ExponentiationOpenExponent() {
    runTest(new ExponentiationTests.TestExponentiationOpenExponent<>(),
        new TestParameters());
  }

  @Test
  public void test_ExponentiationOpenBase() {
    runTest(new ExponentiationTests.TestExponentiationOpenBase<>(),
        new TestParameters());
  }

  @Test()
  public void test_ExponentiationZeroExponent() {
    runTest(new ExponentiationTests.TestExponentiationZeroExponent<>(),
        new TestParameters());
  }

  @Test
  public void test_InnerProductClosed() {
    runTest(new LinAlgTests.TestInnerProductClosed<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_InnerProductOpen() {
    runTest(new LinAlgTests.TestInnerProductOpen<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Logarithm() {
    runTest(new LogTests.TestLogarithm<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Minimum_Protocol_2_parties() {
    runTest(new MinTests.TestMinimumProtocol<>(),
        new TestParameters()
            .numParties(2)
            .performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), is((long) 10));
  }

  @Test
  public void test_Min_Inf_Frac_2_parties() {
    runTest(new MinTests.TestMinInfFraction<>(),
        new TestParameters()
            .numParties(2)
            .performanceLogging(true));
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(ComparisonLoggerDecorator.ARITHMETIC_COMPARISON_LEQ), is((long) 10));
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
  public void test_debug_tools() {
    runTest(new ArithmeticDebugTests.TestArithmeticOpenAndPrint<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_exponentiation_pipe_preprocessed() {
    runTest(new ExponentiationPipeTests.TestPreprocessedValues<>(), new TestParameters());
  }

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
    runTest(new BasicFixedPointTests.TestRepeatedMultiplication<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Secret_Divisor() {
    runTest(new BasicFixedPointTests.TestDiv<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Known_Divisor() {
    runTest(new BasicFixedPointTests.TestDivisionKnownDivisor<>(),
        new TestParameters().numParties(2));
  }

  @Test
  public void test_Real_Division_Known_Negative_Divisor() {
    runTest(new BasicFixedPointTests.TestDivisionKnownNegativeDivisor<>(),
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
  public void test_Real_Matrix_Multiplication() {
    runTest(new LinearAlgebraTests.TestMatrixMultiplication<>(),
        new TestParameters().numParties(2));
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
    runTest(new LinearAlgebraTests.TestVectorMultUnmatchedDimensions<>(),
        new TestParameters());
  }

  @Test
  public void test_Real_Matrix_Multiplication_Unmatched() {
    runTest(new LinearAlgebraTests.TestMatrixMultUnmatchedDimensions<>(),
        new TestParameters());
  }

  @Test
  public void test_Real_Matrix_Addition_Unmatched() {
    runTest(new LinearAlgebraTests.TestAdditionUnmatchedDimensions<>(),
        new TestParameters());
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
  public void test_Real_Sqrt_Uneven_Precision() {
    runTest(new MathTests.TestSqrt<>(),
        new TestParameters()
            .fixedPointPrecesion(BasicFixedPointTests.DEFAULT_PRECISION + 1));
  }

  @Test
  public void test_trunctation() {
    runTest(new TruncationTests.TestTruncation<>(), new TestParameters().numParties(2));
  }

  @Test
  public void testArithmeticAndKnownRight() {
    runTest(new TestArithmeticAndKnownRight<>(), new TestParameters());
  }

  @Test
  public void testArithmeticXorKnownRight() {
    runTest(new TestArithmeticXorKnownRight<>(), new TestParameters());
  }

  @Test
  public void testMod2mBaseCase() {
    TestParameters params = new TestParameters()
        .modulus(ModulusFinder.findSuitableModulus(256))
        .numParties(2);
    runTest(new TestMod2mBaseCase<>(), params);
  }

  @Test
  public void testCarryHelper() {
    runTest(new TestCarryHelper<>(), new TestParameters());
  }

  @Test
  public void testPreCarryBits() {
    runTest(new TestPreCarryBits<>(), new TestParameters());
  }

  @Test
  public void testCarryOutZero() {
    runTest(new TestCarryOut<>(0x00000000, 0x00000000), new TestParameters().numParties(2));
  }

  @Test
  public void testCarryOutOne() {
    runTest(new TestCarryOut<>(0x80000000, 0x80000000), new TestParameters().numParties(2));
  }

  @Test
  public void testCarryOutAllOnes() {
    runTest(new TestCarryOut<>(0xffffffff, 0xffffffff), new TestParameters().numParties(2));
  }

  @Test
  public void testCarryOutOneFromCarry() {
    runTest(new TestCarryOut<>(0x40000000, 0xc0000000), new TestParameters().numParties(2));
  }

  @Test
  public void testCarryOutRandom() {
    runTest(new TestCarryOut<>(new Random(42).nextInt(), new Random(1).nextInt()),
        new TestParameters().numParties(2));
  }

  @Test
  public void testBitLessThanOpen() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    TestParameters parameters = new TestParameters().numParties(2).modulus(modulus);
    runTest(new TestBitLessThanOpen<>(), parameters);
  }

  @Test
  public void testLessThanZero() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    TestParameters parameters = new TestParameters().numParties(2).modulus(modulus);
    runTest(new TestLessThanZero<>(modulus), parameters);
  }

  @Test
  public void testLessThanLogRounds() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    int maxBitLength = 64;
    TestParameters parameters = new TestParameters()
        .numParties(2)
        .modulus(modulus)
        .maxBitLength(maxBitLength);
    runTest(new TestLessThanLogRounds<>(maxBitLength), parameters);
  }

  @Test
  public void testGenerateRandomBitMask() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    int maxBitLength = 64;
    TestParameters parameters = new TestParameters()
        .numParties(2)
        .modulus(modulus)
        .maxBitLength(maxBitLength);
    runTest(new TestGenerateRandomBitMask<>(), parameters);
  }

}
