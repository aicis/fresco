package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;
import org.junit.Test;

public class TestAuthenticatedElement {

  private final BigInteger modulus = new BigInteger("251");

  @Test
  public void testToString() {
    AuthenticatedElement element =
        new AuthenticatedElement(new FieldElement(1, modulus),
            new FieldElement(2, modulus), modulus);
    String expected =
        "AuthenticatedElement [share=FieldElement [value=1, modulus=251, bitLength=8],"
            + " mac=FieldElement [value=2, modulus=251, bitLength=8]]";
    assertEquals(expected, element.toString());
  }

  @Test
  public void testAddPublicFieldElement() {
    FieldElement macKeyShare = new FieldElement(111, modulus);
    AuthenticatedElement element = new AuthenticatedElement(
        new FieldElement(2, modulus),
        new FieldElement(222, modulus),
        modulus);
    FieldElement publicElement = new FieldElement(44, modulus);
    AuthenticatedElement actualPartyOne = element.add(publicElement, 1, macKeyShare);
    AuthenticatedElement expectedPartyOne = new AuthenticatedElement(
        new FieldElement(46, modulus),
        new FieldElement(86, modulus),
        modulus);
    AuthenticatedElement actualPartyTwo = element.add(publicElement, 2, macKeyShare);
    AuthenticatedElement expectedPartyTwo = new AuthenticatedElement(
        new FieldElement(2, modulus),
        new FieldElement(86, modulus),
        modulus);
    CustomAsserts.assertEquals(actualPartyOne, expectedPartyOne);
    CustomAsserts.assertEquals(actualPartyTwo, expectedPartyTwo);
  }

}
