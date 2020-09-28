package dk.alexandra.fresco.suite.dummy.arithmetic;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.suite.dummy.arithmetic.ParallelAndSequenceTests.TestSumAndProduct;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import org.junit.Test;

public class TestBasicArithmetic extends AbstractDummyArithmeticTest {

  @Test
  public void test_Input_Sequential() {
    runTest(new BasicArithmeticTests.TestInput<>(), new TestParameters().numParties(2));
  }

  @Test
  public void test_Input_From_All() {
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
  public void test_SumAndMult_Sequential() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), new TestParameters());
  }

  @Test
  public void test_MultAndAdd_Sequential() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(), new TestParameters());
  }

  @Test
  public void test_TestAdd() {
    runTest(new BasicArithmeticTests.TestAdd<>(), new TestParameters());
  }

  @Test
  public void test_TestAddWithOverflow() {
    runTest(new BasicArithmeticTests.TestAddWithOverflow<>(), new TestParameters());
  }

  @Test
  public void test_TestMultiply() {
    runTest(new BasicArithmeticTests.TestMultiply<>(), new TestParameters());
  }

  @Test
  public void test_TestMultiplyByZero() {
    runTest(new BasicArithmeticTests.TestMultiplyByZero<>(), new TestParameters());
  }

  @Test
  public void test_TestSubtract() {
    runTest(new BasicArithmeticTests.TestSubtract<>(), new TestParameters());
  }

  @Test
  public void test_TestSubtractNegative() {
    runTest(new BasicArithmeticTests.TestSubtractNegative<>(), new TestParameters());
  }

  @Test
  public void test_TestSubtractPublic() {
    runTest(new BasicArithmeticTests.TestSubtractPublic<>(), new TestParameters());
  }

  @Test
  public void test_TestSubtractFromPublic() {
    runTest(new BasicArithmeticTests.TestSubtractFromPublic<>(), new TestParameters());
  }

  @Test
  public void test_TestMultiplyByPublicValue() {
    runTest(new BasicArithmeticTests.TestMultiplyByPublicValue<>(), new TestParameters());
  }

  @Test
  public void test_TestOpenNoConversionByDefault() {
    runTest(new BasicArithmeticTests.TestOpenNoConversionByDefault<>(), new TestParameters());
  }

  @Test
  public void test_TestLotsMult() {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), new TestParameters());
  }

  @Test
  public void test_TestAlternatingMultAdd() {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>(), new TestParameters());
  }

  @Test
  public void test_TestMultiplyWithOverflow() {
    runTest(new BasicArithmeticTests.TestMultiplyWithOverflow<>(), new TestParameters());
  }

  @Test
  public void testSumAndProduct() {
    runTest(new TestSumAndProduct<>(), new TestParameters());
  }

  @Test
  public void test_TestRandomBit() {
    runTest(new BasicArithmeticTests.TestRandomBit<>(), new TestParameters());
  }

  @Test
  public void test_TestRandomElement() {
    runTest(new BasicArithmeticTests.TestRandomElement<>(), new TestParameters());
  }
}
