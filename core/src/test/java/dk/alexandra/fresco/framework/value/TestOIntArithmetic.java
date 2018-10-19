package dk.alexandra.fresco.framework.value;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;

public class TestOIntArithmetic extends AbstractDummyArithmeticTest {

  @Test
  public void testConstantChecks() {
    runTest(new OIntTests.TestConstantCheck<>(), new TestParameters().numParties(2));
  }

  @Test
  public void testTwoPowers() {
    runTest(new OIntTests.TestTwoPowers<>(), new TestParameters().numParties(2));
  }

  @Test
  public void testBigPower() {
    runTest(new OIntTests.TestBigPower<>(), new TestParameters().numParties(2));
  }

  @Test
  public void testFromBigInteger() {
    runTest(new OIntTests.TestFromBigInteger<>(), new TestParameters().numParties(2));
  }

  @Test
  public void testOIntToString() {
    OInt val = new BigIntegerOInt(BigInteger.valueOf(2));
    assertEquals("ToString is not implemented as expected", "BigIntegerOInt{value=2}", val
        .toString());
  }
}
