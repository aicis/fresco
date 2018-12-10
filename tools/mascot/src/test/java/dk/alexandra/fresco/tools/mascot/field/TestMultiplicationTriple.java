package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import org.junit.Test;

public class TestMultiplicationTriple {

  private final ModulusBigInteger modulus = new ModulusBigInteger("251");

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
    String expected = "MultiplicationTriple [left=AuthenticatedElement [share=MascotFieldElement [value=1, modulus=ModulusBigInteger{value=251}, bitLength=8], mac=MascotFieldElement [value=2, modulus=ModulusBigInteger{value=251}, bitLength=8]], right=AuthenticatedElement [share=MascotFieldElement [value=3, modulus=ModulusBigInteger{value=251}, bitLength=8], mac=MascotFieldElement [value=4, modulus=ModulusBigInteger{value=251}, bitLength=8]], product=AuthenticatedElement [share=MascotFieldElement [value=5, modulus=ModulusBigInteger{value=251}, bitLength=8], mac=MascotFieldElement [value=6, modulus=ModulusBigInteger{value=251}, bitLength=8]]]";
    assertEquals(expected, triple.toString());
  }

}
