package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;


public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  @Test
  public void test_Input_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_Known() throws Exception {
    runTest(new BasicArithmeticTests.TestKnown<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
  
  @Test
  public void test_AddKnown() throws Exception {
    runTest(new BasicArithmeticTests.TestAddKnown<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
  
  @Test
  public void test_AddSecret() throws Exception {
    runTest(new BasicArithmeticTests.TestAddSecret<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
/*
  @Test
  public void test_OutputToTarget_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestOutputToSingleParty<>(), EvaluationStrategy.SEQUENTIAL,
        2, defaultMod, true);
    assertThat(performanceLoggers.get(1).getLoggedValues()
        .get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), is((long)0));
  }

  @Test
  public void test_AddPublicValue_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), EvaluationStrategy.SEQUENTIAL,
        1);
  }

  @Test
  public void test_KnownSInt_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.SEQUENTIAL,
        1);
  }

  @Test
  public void test_MultAndAdd_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(), EvaluationStrategy.SEQUENTIAL,
        1);
  }

  @Test
  public void testSumAndOutputSequential() throws Exception {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL,
        1);
  }

  @Test
  public void testSumAndProduct() throws Exception {
    runTest(new TestSumAndProduct<>(), EvaluationStrategy.SEQUENTIAL,
        1);
  }
*/
}
