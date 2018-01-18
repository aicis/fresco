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
  public void test_Open_to_party_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestOpenToParty<>(), EvaluationStrategy.SEQUENTIAL,
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
  
  @Test
  public void test_SubtractSecret() throws Exception {
    runTest(new BasicArithmeticTests.TestSubtractSecret<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
  
  @Test
  public void test_SubKnown() throws Exception {
    runTest(new BasicArithmeticTests.TestSubKnown<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
  
  @Test
  public void test_SubKnown2() throws Exception {
    runTest(new BasicArithmeticTests.TestSubKnown2<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_MultSecret() throws Exception {
    runTest(new BasicArithmeticTests.TestMultSecret<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
  
  @Test
  public void test_MultKnown() throws Exception {
    runTest(new BasicArithmeticTests.TestMultKnown<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }

  @Test
  public void test_Mults() throws Exception {
    runTest(new BasicArithmeticTests.TestMult<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
  
  @Test
  public void test_DivisionSecretDivisor() throws Exception {
    runTest(new BasicArithmeticTests.TestDivisionSecret<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
  
  @Test
  public void test_DivisionKnownDivisor() throws Exception {
    runTest(new BasicArithmeticTests.TestDivisionKnownDivisor<>(), EvaluationStrategy.SEQUENTIAL,
        2);
  }
}
