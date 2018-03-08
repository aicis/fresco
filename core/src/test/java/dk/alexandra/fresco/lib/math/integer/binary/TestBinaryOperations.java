package dk.alexandra.fresco.lib.math.integer.binary;

import org.junit.Test;

import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestBinaryOperations {
  
  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    new RightShift(2, new DummyArithmeticSInt(2), -1, true);
  }

}
