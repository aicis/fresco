package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.BigIntMutable;
import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import java.math.BigInteger;
import org.junit.Test;

public class TestBinaryOperations {

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    new RightShift(2, new DummyArithmeticSInt(new BigInt(2, new Modulus(10))), -1, true);
  }

}
