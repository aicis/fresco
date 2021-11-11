package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests.TestRightShift;
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests.TestKnownDivisorDivision;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestInput;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestSimpleMultAndAdd;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestSumAndMult;
import org.junit.Test;

public class TestCRT {

  @Test
  public void testInput() {
    new AbstractDummyCRTTest().runTest(new TestInput<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testMultAndAdd() {
    new AbstractDummyCRTTest().runTest(new TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testRightShift() {
    new AbstractDummyCRTTest().runTest(new TestRightShift<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }
}
