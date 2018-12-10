package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.builder.numeric.FieldElementMersennePrime;
import dk.alexandra.fresco.framework.builder.numeric.ModulusMersennePrime;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import org.junit.Test;

public class TestBinaryOperations {

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    FieldElementMersennePrime value = new FieldElementMersennePrime(2,
        new ModulusMersennePrime(10));
    new RightShift(2, new DummyArithmeticSInt(value), -1, true);
  }
}
