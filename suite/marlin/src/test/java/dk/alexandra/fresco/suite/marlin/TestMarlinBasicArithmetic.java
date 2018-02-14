package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import org.junit.Test;

public class TestMarlinBasicArithmetic extends AbstractMarlinTest {

  @Test
  public void testInput() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testInputThree() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testAdd() {
    runTest(new BasicArithmeticTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testAddThree() {
    runTest(new BasicArithmeticTests.TestAdd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testMultiply() {
    runTest(new BasicArithmeticTests.TestMultiply<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testKnown() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testKnownThree() {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testMultiplyThree() {
    runTest(new BasicArithmeticTests.TestMultiply<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testInputOutputMany() {
    runTest(new TestCloseAndOpenList<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

  @Test
  public void testInputOutputManyThree() {
    runTest(new TestCloseAndOpenList<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3,
        false);
  }

  @Test
  public void testMultiplyMany() {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2,
        false);
  }

}
