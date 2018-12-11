package dk.alexandra.fresco.suite.spdz;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.preprocessing.MascotFormatConverter;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import java.math.BigInteger;
import org.junit.Test;

public class TestMascotFormatConverter {

  private AuthenticatedElement getAuthElement(int shareVal, int macVal, BigInteger modulus) {
    MascotFieldElement share = new MascotFieldElement(shareVal, modulus);
    MascotFieldElement mac = new MascotFieldElement(macVal, modulus);
    return new AuthenticatedElement(share, mac, modulus);
  }

  private SpdzSInt getSpdzElement(int shareVal, int macVal, FieldDefinition definition) {
    return new SpdzSInt(definition.createElement(shareVal), definition.createElement(macVal));
  }

  @Test
  public void convertSingleElement() {
    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    FieldDefinitionBigInteger definition = new FieldDefinitionBigInteger(
        new ModulusBigInteger(modulus));
    AuthenticatedElement element = getAuthElement(100, 123, modulus);
    SpdzSInt expected = getSpdzElement(100, 123, definition);
    SpdzSInt actual = new MascotFormatConverter(definition).toSpdzSInt(element);
    assertEquals(expected, actual);
  }

  @Test
  public void convertTriple() {
    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    FieldDefinitionBigInteger definition = new FieldDefinitionBigInteger(
        new ModulusBigInteger(modulus));
    AuthenticatedElement left = getAuthElement(1, 2, modulus);
    AuthenticatedElement right = getAuthElement(3, 4, modulus);
    AuthenticatedElement product = getAuthElement(5, 6, modulus);
    MultiplicationTriple triple = new MultiplicationTriple(left, right, product);
    SpdzTriple expected = new SpdzTriple(getSpdzElement(1, 2, definition),
        getSpdzElement(3, 4, definition), getSpdzElement(5, 6, definition));
    SpdzTriple actual = new MascotFormatConverter(definition).toSpdzTriple(triple);
    assertEquals(expected, actual);
  }
}
