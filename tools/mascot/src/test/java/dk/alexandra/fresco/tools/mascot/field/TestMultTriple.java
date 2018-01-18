package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

public class TestMultTriple {

  private final BigInteger modulus = new BigInteger("251");

  @Test
  public void testToString() {
    AuthenticatedElement left = new AuthenticatedElement(new FieldElement(1, modulus),
        new FieldElement(2, modulus), modulus);
    AuthenticatedElement right =
        new AuthenticatedElement(new FieldElement(3, modulus),
            new FieldElement(4, modulus), modulus);
    AuthenticatedElement product =
        new AuthenticatedElement(new FieldElement(5, modulus),
            new FieldElement(6, modulus), modulus);
    MultTriple triple = new MultTriple(left, right, product);
    String expected = "MultTriple [left=AuthenticatedElement [share=FieldElement [value=1, modulus=251, bitLength=8], mac=FieldElement [value=2, modulus=251, bitLength=8]], right=AuthenticatedElement [share=FieldElement [value=3, modulus=251, bitLength=8], mac=FieldElement [value=4, modulus=251, bitLength=8]], product=AuthenticatedElement [share=FieldElement [value=5, modulus=251, bitLength=8], mac=FieldElement [value=6, modulus=251, bitLength=8]]]";
    assertEquals(expected, triple.toString());
  }

}
