package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.builder.numeric.FieldElementBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import org.junit.Test;

public class TestBinaryOperations {

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    FieldElementBigInteger value = new FieldElementBigInteger(2,
        new ModulusBigInteger(10));
    new RightShift(2, new DummyArithmeticSInt(value), -1, true);
  }
}
