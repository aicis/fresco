package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;
import org.junit.Test;

public class TestAuthenticatedElement {

  private final BigInteger modulus = new BigInteger("251");
  private final FieldDefinitionBigInteger definition = new FieldDefinitionBigInteger(
      new ModulusBigInteger(modulus));

  @Test
  public void testToString() {
    AuthenticatedElement element =
        new AuthenticatedElement(
            definition.createElement(1),
            definition.createElement(2)
        );
    String expected =
        "AuthenticatedElement [share=FieldElement [value=1, modulus=251, bitLength=8],"
            + " mac=FieldElement [value=2, modulus=251, bitLength=8]]";
    assertEquals(expected, element.toString());
  }

  @Test
  public void testAddPublicFieldElement() {
    FieldElement macKeyShare = definition.createElement(111);
    AuthenticatedElement element = new AuthenticatedElement(
        definition.createElement(2),
        definition.createElement(222));
    FieldElement publicElement = definition.createElement(44);
    AuthenticatedElement actualPartyOne = element.add(definition,
        publicElement, 1, macKeyShare);
    AuthenticatedElement expectedPartyOne = new AuthenticatedElement(
        definition.createElement(46),
        definition.createElement(86));
    AuthenticatedElement actualPartyTwo = element.add(definition,
        publicElement, 2, macKeyShare);
    AuthenticatedElement expectedPartyTwo = new AuthenticatedElement(
        definition.createElement(2),
        definition.createElement(86));
    CustomAsserts.assertEquals(actualPartyOne, expectedPartyOne);
    CustomAsserts.assertEquals(actualPartyTwo, expectedPartyTwo);
  }
}
