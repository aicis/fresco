package dk.alexandra.fresco.suite.spdz.basic;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import dk.alexandra.fresco.suite.spdz.AbstractSpdzTest;
import org.junit.Test;

public class TestSpdzBasicArithmetic extends AbstractSpdzTest {

  @Test
  public void testInput() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAddInSequence() {
    runTest(new BasicArithmeticTests.TestAddInSequence<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
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
  public void testMultiplyInSequence() {
    runTest(new BasicArithmeticTests.TestMultiplyInSequence<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
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

}
