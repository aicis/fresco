package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import dk.alexandra.fresco.lib.compare.CompareTests.TestCompareEQ;
import dk.alexandra.fresco.lib.compare.CompareTests.TestCompareEQEdgeCases;
import dk.alexandra.fresco.lib.compare.CompareTests.TestCompareEQZero;
import dk.alexandra.fresco.lib.compare.CompareTests.TestLessThanLogRounds;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests.TestGenerateRandomBitMask;
import dk.alexandra.fresco.lib.real.BasicFixedPointTests;
import dk.alexandra.fresco.lib.real.BasicFixedPointTests.TestMult;
import dk.alexandra.fresco.lib.real.BasicFixedPointTests.TestMultIsolated;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.TestComparisonSpdz2k.TestBitLessThanOpenSpdz2k;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.TestLogicalOperationsSpdz2k.TestAndKnownSpdz2k;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.TestLogicalOperationsSpdz2k.TestAndSpdz2k;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.TestLogicalOperationsSpdz2k.TestNotSpdz2k;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.TestLogicalOperationsSpdz2k.TestOrListSpdz2k;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.TestLogicalOperationsSpdz2k.TestOrSpdz2k;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.TestLogicalOperationsSpdz2k.TestXorKnownSpdz2k;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import org.junit.Test;

public abstract class Spdz2kTestSuite<Spdz2kResourcePoolT extends Spdz2kResourcePool<?>>
    extends AbstractSpdz2kTest<Spdz2kResourcePoolT> {

  @Test
  public void testInput() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAdd() {
    runTest(new BasicArithmeticTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAddWithOverflow() {
    runTest(new BasicArithmeticTests.TestAddWithOverflow<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testMultiply() {
    runTest(new BasicArithmeticTests.TestMultiply<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testMultiplyByZero() {
    runTest(new BasicArithmeticTests.TestMultiplyByZero<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testMultiplyWithOverflow() {
    runTest(new BasicArithmeticTests.TestMultiplyWithOverflow<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testKnown() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAddPublic() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testInputOutputMany() {
    runTest(new TestCloseAndOpenList<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testMultiplyMany() {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSumAndMult() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSimpleMultAndAdd() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAlternatingMultAdd() {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testMultiplyByPublicValue() {
    runTest(new BasicArithmeticTests.TestMultiplyByPublicValue<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSubtract() {
    runTest(new BasicArithmeticTests.TestSubtract<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSubtractNegative() {
    runTest(new BasicArithmeticTests.TestSubtractNegative<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSubtractPublic() {
    runTest(new BasicArithmeticTests.TestSubtractPublic<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSubtractFromPublic() {
    runTest(new BasicArithmeticTests.TestSubtractFromPublic<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testOutputToSingleParty() {
    runTest(new BasicArithmeticTests.TestOutputToSingleParty<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testRandomBit() {
    runTest(new BasicArithmeticTests.TestRandomBit<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testRandomElement() {
    runTest(new BasicArithmeticTests.TestRandomElement<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testBitLessThanOpen() {
    runTest(new TestBitLessThanOpenSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testGenerateRandomBitMask() {
    runTest(new TestGenerateRandomBitMask<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testLessThanLogRounds() {
    runTest(new TestLessThanLogRounds<>(getMaxBitLength()),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAndKnown() {
    runTest(new TestAndKnownSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAnd() {
    runTest(new TestAndSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testOrOfList() {
    runTest(new TestOrListSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testOr() {
    runTest(new TestOrSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testNot() {
    runTest(new TestNotSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testXorKnown() {
    runTest(new TestXorKnownSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testCompareZeroLogRounds() {
    runTest(new TestCompareEQZero<>(getMaxBitLength()), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testEqualsLogRounds() {
    runTest(new TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testEqualsEdgeCasesLogRounds() {
    runTest(new TestCompareEQEdgeCases<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testRealInput() {
    runTest(new BasicFixedPointTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testRealOpenToParty() {
    runTest(new BasicFixedPointTests.TestOpenToParty<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testRealKnown() {
    runTest(new BasicFixedPointTests.TestKnown<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Real_Add_Secret() {
    runTest(new BasicFixedPointTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Real_Mult_Known() {
    runTest(new BasicFixedPointTests.TestMultKnown<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Real_Mults() {
    runTest(new TestMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Real_Mults_Isolated() {
    runTest(new TestMultIsolated<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  protected abstract int getMaxBitLength();

}
