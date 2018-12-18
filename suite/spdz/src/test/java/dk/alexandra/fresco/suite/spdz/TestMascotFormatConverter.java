package dk.alexandra.fresco.suite.spdz;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.preprocessing.MascotFormatConverter;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import java.math.BigInteger;
import org.junit.Test;

public class TestMascotFormatConverter {

  private BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(modulus);

  private AuthenticatedElement getAuthElement(int shareVal, int macVal) {
    FieldElement share = definition.createElement(shareVal);
    FieldElement mac = definition.createElement(macVal);
    return new AuthenticatedElement(share, mac);
  }

  private SpdzSInt getSpdzElement(int shareVal, int macVal, FieldDefinition definition) {
    return new SpdzSInt(definition.createElement(shareVal), definition.createElement(macVal));
  }

  @Test
  public void convertSingleElement() {
    AuthenticatedElement element = getAuthElement(100, 123);
    SpdzSInt expected = getSpdzElement(100, 123, definition);
    SpdzSInt actual = MascotFormatConverter.toSpdzSInt(element);
    assertEquals(definition.convertToUnsigned(expected.getShare()),
        definition.convertToUnsigned(actual.getShare()));
  }

  @Test
  public void convertTriple() {
    AuthenticatedElement left = getAuthElement(1, 2);
    AuthenticatedElement right = getAuthElement(3, 4);
    AuthenticatedElement product = getAuthElement(5, 6);
    MultiplicationTriple triple = new MultiplicationTriple(left, right, product);
    SpdzTriple expected = new SpdzTriple(getSpdzElement(1, 2, definition),
        getSpdzElement(3, 4, definition), getSpdzElement(5, 6, definition));
    SpdzTriple actual = MascotFormatConverter.toSpdzTriple(triple);
    assertEquals(definition.convertToUnsigned(expected.getA().getShare()),
        definition.convertToUnsigned(actual.getA().getShare()));
    assertEquals(definition.convertToUnsigned(expected.getB().getShare()),
        definition.convertToUnsigned(actual.getB().getShare()));
    assertEquals(definition.convertToUnsigned(expected.getC().getShare()),
        definition.convertToUnsigned(actual.getC().getShare()));
  }
}
