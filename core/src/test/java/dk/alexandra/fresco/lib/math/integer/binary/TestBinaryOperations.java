package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import org.junit.Test;

public class TestBinaryOperations {

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    FieldDefinitionBigInteger definition = new FieldDefinitionBigInteger(new ModulusBigInteger(10));
    FieldElement value = definition.createElement(2);
    new RightShift(2, new DummyArithmeticSInt(value), -1, true);
  }
}
