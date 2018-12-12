package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import java.math.BigInteger;
import org.junit.Test;

public class TestMultiplicationTriple {

  private final BigInteger modulus = new BigInteger("251");
  private final FieldDefinitionBigInteger definition = new FieldDefinitionBigInteger(
      new ModulusBigInteger(modulus));

  @Test
  public void testToString() {
    AuthenticatedElement left = new AuthenticatedElement(
        definition.createElement(1),
        definition.createElement(2));
    AuthenticatedElement right = new AuthenticatedElement(
        definition.createElement(3),
        definition.createElement(4));
    AuthenticatedElement product = new AuthenticatedElement(
        definition.createElement(5),
        definition.createElement(6));
    MultiplicationTriple triple = new MultiplicationTriple(left, right, product);
    String expected = "MultiplicationTriple [left=AuthenticatedElement [share=FieldElement [value=1, modulus=251, bitLength=8], mac=FieldElement [value=2, modulus=251, bitLength=8]], right=AuthenticatedElement [share=FieldElement [value=3, modulus=251, bitLength=8], mac=FieldElement [value=4, modulus=251, bitLength=8]], product=AuthenticatedElement [share=FieldElement [value=5, modulus=251, bitLength=8], mac=FieldElement [value=6, modulus=251, bitLength=8]]]";
    assertEquals(expected, triple.toString());
  }
}
