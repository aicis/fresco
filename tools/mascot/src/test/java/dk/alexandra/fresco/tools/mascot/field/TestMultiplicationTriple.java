package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;

public class TestMultiplicationTriple {

  private final BigInteger modulus = new BigInteger("251");

  @Test
  public void testToString() {
    AuthenticatedElement left = new AuthenticatedElement(new MascotFieldElement(1, modulus),
        new MascotFieldElement(2, modulus), modulus);
    AuthenticatedElement right =
        new AuthenticatedElement(new MascotFieldElement(3, modulus),
            new MascotFieldElement(4, modulus), modulus);
    AuthenticatedElement product =
        new AuthenticatedElement(new MascotFieldElement(5, modulus),
            new MascotFieldElement(6, modulus), modulus);
    MultiplicationTriple triple = new MultiplicationTriple(left, right, product);
    String expected = "MultiplicationTriple [left=AuthenticatedElement [share=MascotFieldElement [value=1, modulus=251, bitLength=8], mac=MascotFieldElement [value=2, modulus=251, bitLength=8]], right=AuthenticatedElement [share=MascotFieldElement [value=3, modulus=251, bitLength=8], mac=MascotFieldElement [value=4, modulus=251, bitLength=8]], product=AuthenticatedElement [share=MascotFieldElement [value=5, modulus=251, bitLength=8], mac=MascotFieldElement [value=6, modulus=251, bitLength=8]]]";
    assertEquals(expected, triple.toString());
  }
}
