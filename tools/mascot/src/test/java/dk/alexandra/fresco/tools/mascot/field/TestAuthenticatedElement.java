package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

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
  
}
