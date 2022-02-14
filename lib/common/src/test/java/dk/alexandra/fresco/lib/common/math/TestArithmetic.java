package dk.alexandra.fresco.lib.common.math;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.TestFrameworkException;
import dk.alexandra.fresco.framework.builder.numeric.ExponentiationPipeTests;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.lib.common.compare.CompareTests;
import dk.alexandra.fresco.lib.common.compare.CompareTests.TestCompareEQModulusTooSmall;
import dk.alexandra.fresco.lib.common.compare.CompareTests.TestCompareLTModulusTooSmall;
import dk.alexandra.fresco.lib.common.compare.CompareTests.TestCompareLTUnsupportedAlgorithm;
import dk.alexandra.fresco.lib.common.compare.CompareTests.TestCompareZeroInputTooLarge;
import dk.alexandra.fresco.lib.common.compare.CompareTests.TestLessThanLogRounds;
import dk.alexandra.fresco.lib.common.compare.lt.BitLessThanOpenTests.TestBitLessThanOpen;
import dk.alexandra.fresco.lib.common.compare.lt.CarryOutTests;
import dk.alexandra.fresco.lib.common.compare.lt.CarryOutTests.TestCarryOut;
import dk.alexandra.fresco.lib.common.compare.lt.CarryOutTests.TestCarryOutSizeMismatch;
import dk.alexandra.fresco.lib.common.compare.lt.CarryOutTests.TestCarrySingleton;
import dk.alexandra.fresco.lib.common.compare.lt.LessThanZeroTests.TestLessThanZero;
import dk.alexandra.fresco.lib.common.compare.lt.PreCarryTests.TestPreCarryBits;
import dk.alexandra.fresco.lib.common.math.AdvancedNumericTests.TestMinInfFrac;
import dk.alexandra.fresco.lib.common.math.integer.TestProductAndSum.TestProduct;
import dk.alexandra.fresco.lib.common.math.integer.TestProductAndSum.TestSum;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests.TestGenerateRandomBitMask;
import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSelectTests;
import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSwapNeighborsTests;
import dk.alexandra.fresco.lib.common.math.integer.conditional.ConditionalSwapRowsTests;
import dk.alexandra.fresco.lib.common.math.integer.conditional.SwapIfTests;
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.common.math.integer.exp.ExponentiationTests;
import dk.alexandra.fresco.lib.common.math.integer.inv.InversionTests.TestInversion;
import dk.alexandra.fresco.lib.common.math.integer.inv.InversionTests.TestInvertZero;
import dk.alexandra.fresco.lib.common.math.integer.linalg.LinAlgTests;
import dk.alexandra.fresco.lib.common.math.integer.log.LogTests;
import dk.alexandra.fresco.lib.common.math.integer.min.MinTests;
import dk.alexandra.fresco.lib.common.math.integer.min.MinTests.TestArgMin;
import dk.alexandra.fresco.lib.common.math.integer.min.MinTests.TestArgMinTrivial;
import dk.alexandra.fresco.lib.common.math.integer.mod.Mod2mTests.TestMod2mBaseCase;
import dk.alexandra.fresco.lib.common.math.integer.sqrt.SqrtTests;
import dk.alexandra.fresco.lib.common.math.integer.stat.StatisticsTests;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestAnd;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestAndKnown;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestNot;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestOr;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestOrList;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestOrNeighbors;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestXor;
import dk.alexandra.fresco.lib.common.math.logical.LogicalOperationsTests.TestXorKnown;
import dk.alexandra.fresco.lib.common.math.polynomial.PolynomialTests;
import dk.alexandra.fresco.logging.arithmetic.NumericLoggingDecorator;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.math.BigInteger;
import java.util.Random;
import org.junit.Test;

public class TestArithmetic extends AbstractDummyArithmeticTest {

  @Test
  public void test_MinInfFrac_Sequential() {
    runTest(new TestMinInfFrac<>(), new TestParameters());
  }

  @Test
  public void test_MinInfFrac_SequentialBatched() {
    runTest(new TestMinInfFrac<>(), new TestParameters());
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
        is((long) 14));
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
  }

  @Test
  public void test_Min_Inf_Frac_2_parties() {
    runTest(
        new MinTests.TestMinInfFraction<>(),
        new TestParameters().numParties(2).performanceLogging(true));
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

  @Test
  public void testModulus() {
    runTest(new AdvancedNumericTests.TestModulus<>(), new TestParameters());
  }

  @Test
  public void testExponentiationOpenBase() {
    runTest(new ExponentiationTests.TestExponentiationOpenBase<>(), new TestParameters());
  }

  @Test
  public void testExponentiationOpenExponent() {
    runTest(new ExponentiationTests.TestExponentiationOpenExponent<>(), new TestParameters());
  }

  @Test
  public void testSum() {
    runTest(new TestSum<>(), new TestParameters());
  }

  @Test
  public void testProduct() {
    runTest(new TestProduct<>(), new TestParameters());
  }

  @Test
  public void testAndKnown() {
    runTest(new TestAndKnown<>(), new TestParameters());
  }

  @Test
  public void testAnd() {
    runTest(new TestAnd<>(), new TestParameters());
  }

  @Test
  public void testOr() {
    runTest(new TestOr<>(), new TestParameters());
  }

  @Test
  public void testXor() {
    runTest(new TestXor<>(), new TestParameters());
  }

  @Test
  public void testOrNeighbours() {
    runTest(new TestOrNeighbors<>(), new TestParameters());
  }

  @Test
  public void testOrList() {
    runTest(new TestOrList<>(), new TestParameters());
  }

  @Test
  public void testNot() {
    runTest(new TestNot<>(), new TestParameters());
  }

  @Test
  public void testXorKnown() {
    runTest(new TestXorKnown<>(), new TestParameters());
  }

  @Test
  public void testMod2mBaseCase() {
    runTest(new TestMod2mBaseCase<>(), new TestParameters());
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
  public void testCarrySingleton() {
    runTest(new TestCarrySingleton<>(), new TestParameters());
  }

  @Test(expected = TestFrameworkException.class)
  public void testCarryOutSizeMismatch() {
    runTest(new TestCarryOutSizeMismatch<>(), new TestParameters());
  }

  @Test
  public void testBitLessThanOpen() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    TestParameters parameters = new TestParameters().numParties(2).field(new BigIntegerFieldDefinition(modulus));
    runTest(new TestBitLessThanOpen<>(), parameters);
  }

  @Test
  public void testLessThanZero() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    int maxBitLength = 64;
    TestParameters parameters = new TestParameters().numParties(2).field(new BigIntegerFieldDefinition(modulus)).maxBitLength(
        maxBitLength);
    runTest(new TestLessThanZero<>(modulus), parameters);
  }

  @Test
  public void testLessThanLogRounds() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    int maxBitLength = 64;
    TestParameters parameters = new TestParameters().numParties(2).field(new BigIntegerFieldDefinition(modulus)).maxBitLength(
        maxBitLength);
    runTest(new TestLessThanLogRounds<>(maxBitLength), parameters);
  }

  @Test
  public void testGenerateRandomBitMask() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    int maxBitLength = 64;
    TestParameters parameters = new TestParameters().numParties(2).field(new BigIntegerFieldDefinition(modulus)).maxBitLength(
        maxBitLength);
    runTest(new TestGenerateRandomBitMask<>(), parameters);
  }

  @Test
  public void testArgMin() {
    runTest(new TestArgMin<>(), new TestParameters());
  }

  @Test(expected = TestFrameworkException.class)
  public void testArgMinTrivial() {
    runTest(new TestArgMinTrivial<>(), new TestParameters());
  }

  @Test(expected = TestFrameworkException.class)
  public void testEQModulusTooSmall() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    TestParameters parameters = new TestParameters().numParties(2).field(new BigIntegerFieldDefinition(modulus));
    runTest(new TestCompareEQModulusTooSmall<>(127), parameters);
  }

  @Test(expected = TestFrameworkException.class)
  public void testCompareZeroInputTooLarge() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    int maxBitLength = 64;
    TestParameters parameters = new TestParameters().numParties(2).field(new BigIntegerFieldDefinition(modulus)).maxBitLength(maxBitLength);
    runTest(new TestCompareZeroInputTooLarge<>(65), parameters);
  }

  @Test(expected = TestFrameworkException.class)
  public void testLTModulusTooSmall() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(128);
    TestParameters parameters = new TestParameters().numParties(2).field(new BigIntegerFieldDefinition(modulus)).maxBitLength(127);
    runTest(new TestCompareLTModulusTooSmall<>(), parameters);
  }

  @Test(expected = TestFrameworkException.class)
  public void testLTUnsupportedAlgorithm() {
    runTest(new TestCompareLTUnsupportedAlgorithm<>(), new TestParameters());
  }
}
