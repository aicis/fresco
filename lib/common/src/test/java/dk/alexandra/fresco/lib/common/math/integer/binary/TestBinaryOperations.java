package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import org.junit.Test;

public class TestBinaryOperations {

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition("10");
    FieldElement value = definition.createElement(2);
    new RightShift(2, new DummyArithmeticSInt(value), -1);
  }
}
