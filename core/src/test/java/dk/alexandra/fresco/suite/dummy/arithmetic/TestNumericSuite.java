package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.lib.arithmetic.AdvancedNumericTests;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationTests;
import org.junit.Test;

public class TestNumericSuite extends AbstractDummyArithmeticTest {

  @Test
  public void testDivisionWithKnownDenominator() {
    runTest(new AdvancedNumericTests.TestDivisionWithKnownDenominator<>(10, 2),
        new TestParameters());
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
  public void testAddPublicValue() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), new TestParameters());
  }

  @Test
  public void testSubtractFromPublic() {
    runTest(new BasicArithmeticTests.TestSubtractFromPublic<>(), new TestParameters());
  }

  @Test
  public void testSubtractPublic() {
    runTest(new BasicArithmeticTests.TestSubtractPublic<>(), new TestParameters());
  }

  @Test
  public void testMultiplyByPublicValue() {
    runTest(new BasicArithmeticTests.TestMultiplyByPublicValue<>(), new TestParameters());
  }
}
