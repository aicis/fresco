package dk.alexandra.fresco.suite.spdz.basic;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import dk.alexandra.fresco.suite.spdz.AbstractSpdzTest;
import org.junit.Test;

public class TestSpdzBasicArithmetic extends AbstractSpdzTest {

  @Test
  public void testInput() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testAddInSequence() {
    runTest(new BasicArithmeticTests.TestAddInSequence<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testAdd() {
    runTest(new BasicArithmeticTests.TestAdd<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testAddWithOverflow() {
    runTest(new BasicArithmeticTests.TestAddWithOverflow<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testMultiply() {
    runTest(new BasicArithmeticTests.TestMultiply<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testMultiplyInSequence() {
    runTest(new BasicArithmeticTests.TestMultiplyInSequence<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testMultiplyByZero() {
    runTest(new BasicArithmeticTests.TestMultiplyByZero<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testMultiplyWithOverflow() {
    runTest(new BasicArithmeticTests.TestMultiplyWithOverflow<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testKnown() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testAddPublic() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testInputOutputMany() {
    runTest(new TestCloseAndOpenList<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testMultiplyMany() {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testSumAndMult() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testSimpleMultAndAdd() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testAlternatingMultAdd() {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testMultiplyByPublicValue() {
    runTest(new BasicArithmeticTests.TestMultiplyByPublicValue<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testSubtract() {
    runTest(new BasicArithmeticTests.TestSubtract<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testSubtractNegative() {
    runTest(new BasicArithmeticTests.TestSubtractNegative<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testSubtractPublic() {
    runTest(new BasicArithmeticTests.TestSubtractPublic<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testSubtractFromPublic() {
    runTest(new BasicArithmeticTests.TestSubtractFromPublic<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testOutputToSingleParty() {
    runTest(new BasicArithmeticTests.TestOutputToSingleParty<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testRandomBit() {
    runTest(new BasicArithmeticTests.TestRandomBit<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

  @Test
  public void testRandomElement() {
    runTest(new BasicArithmeticTests.TestRandomElement<>(),
        EvaluationStrategy.NATIVE_BATCHED);
  }

}
