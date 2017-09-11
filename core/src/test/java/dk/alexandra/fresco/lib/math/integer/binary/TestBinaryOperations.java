package dk.alexandra.fresco.lib.math.integer.binary;

import org.junit.Test;

import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestBinaryOperations {
  
  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    new RepeatedRightShift(new DummyArithmeticSInt(2), -1, true);
  }

}
