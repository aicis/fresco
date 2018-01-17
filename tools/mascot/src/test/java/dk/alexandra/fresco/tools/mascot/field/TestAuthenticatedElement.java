package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;
import org.junit.Test;

public class TestAuthenticatedElement {

  private final BigInteger modulus = new BigInteger("251");
  private final int modBitLength = 8;

  @Test
  public void testToString() {
    AuthenticatedElement element =
        new AuthenticatedElement(new FieldElement(1, modulus, modBitLength),
            new FieldElement(2, modulus, modBitLength), modulus, modBitLength);
    String expected =
        "AuthenticatedElement [share=FieldElement [value=1, modulus=251, bitLength=8],"
            + " mac=FieldElement [value=2, modulus=251, bitLength=8], "
            + "modulus=251, modBitLength=8]";
    assertEquals(expected, element.toString());
  }

  @Test
  public void testAddPublicFieldElement() {
    FieldElement macKeyShare = new FieldElement(111, modulus, modBitLength);
    AuthenticatedElement element = new AuthenticatedElement(
        new FieldElement(2, modulus, modBitLength),
        new FieldElement(222, modulus, modBitLength),
        modulus, modBitLength);
    FieldElement publicElement = new FieldElement(44, modulus, modBitLength);
    AuthenticatedElement actualPartyOne = element.add(publicElement, 1, macKeyShare);
    AuthenticatedElement expectedPartyOne = new AuthenticatedElement(
        new FieldElement(46, modulus, modBitLength),
        new FieldElement(86, modulus, modBitLength),
        modulus, modBitLength);
    AuthenticatedElement actualPartyTwo = element.add(publicElement, 2, macKeyShare);
    AuthenticatedElement expectedPartyTwo = new AuthenticatedElement(
        new FieldElement(2, modulus, modBitLength),
        new FieldElement(86, modulus, modBitLength),
        modulus, modBitLength);
    CustomAsserts.assertEquals(actualPartyOne, expectedPartyOne);
    CustomAsserts.assertEquals(actualPartyTwo, expectedPartyTwo);
  }

}
