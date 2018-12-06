package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;
import org.junit.Test;

public class TestAuthenticatedElement {

  private final Modulus modulus = new Modulus("251");

  @Test
  public void testToString() {
    AuthenticatedElement element =
        new AuthenticatedElement(new MascotFieldElement(1, modulus),
            new MascotFieldElement(2, modulus), modulus);
    String expected =
        "AuthenticatedElement [share=MascotFieldElement [value=1, modulus=251, bitLength=8],"
            + " mac=MascotFieldElement [value=2, modulus=251, bitLength=8]]";
    assertEquals(expected, element.toString());
  }

  @Test
  public void testAddPublicFieldElement() {
    MascotFieldElement macKeyShare = new MascotFieldElement(111, modulus);
    AuthenticatedElement element = new AuthenticatedElement(
        new MascotFieldElement(2, modulus),
        new MascotFieldElement(222, modulus),
        modulus);
    MascotFieldElement publicElement = new MascotFieldElement(44, modulus);
    AuthenticatedElement actualPartyOne = element.add(publicElement, 1, macKeyShare);
    AuthenticatedElement expectedPartyOne = new AuthenticatedElement(
        new MascotFieldElement(46, modulus),
        new MascotFieldElement(86, modulus),
        modulus);
    AuthenticatedElement actualPartyTwo = element.add(publicElement, 2, macKeyShare);
    AuthenticatedElement expectedPartyTwo = new AuthenticatedElement(
        new MascotFieldElement(2, modulus),
        new MascotFieldElement(86, modulus),
        modulus);
    CustomAsserts.assertEquals(actualPartyOne, expectedPartyOne);
    CustomAsserts.assertEquals(actualPartyTwo, expectedPartyTwo);
  }

}
