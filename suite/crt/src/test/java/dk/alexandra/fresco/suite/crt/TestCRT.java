package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests.TestRightShift;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestCorrelatedNoise;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestInput;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestLiftPQ;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestLiftQP;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestMaskAndOpen;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestMixedAdd;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestProjectionLeft;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestProjectionRight;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestTruncp;
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

  @Test
  public void testCorrelatedNoise() {
    new AbstractDummyCRTTest()
        .runTest(new TestCorrelatedNoise<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testMixedAdd() {
    new AbstractDummyCRTTest().runTest(new TestMixedAdd<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }


  @Test
  public void testMaskAndOpen() {
    new AbstractDummyCRTTest().runTest(new TestMaskAndOpen<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testLiftPQ() {
    new AbstractDummyCRTTest().runTest(new TestLiftPQ<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testLiftQP() {
    new AbstractDummyCRTTest().runTest(new TestLiftQP<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testProjectionLeft() {
    new AbstractDummyCRTTest()
        .runTest(new TestProjectionLeft<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testProjectionRight() {
    new AbstractDummyCRTTest()
        .runTest(new TestProjectionRight<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testTruncp() {
    new AbstractDummyCRTTest()
        .runTest(new TestTruncp<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

}
