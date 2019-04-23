package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import java.math.BigInteger;
import org.junit.Test;

public class TestMultiplicationTriple {

  private final BigInteger modulus = new BigInteger("251");
  private final BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(modulus);

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
    String expected = "MultiplicationTriple [left=AuthenticatedElement [share=BigIntegerFieldElement{value=1, modulus=BigIntegerModulus{value=251}}, mac=BigIntegerFieldElement{value=2, modulus=BigIntegerModulus{value=251}}], right=AuthenticatedElement [share=BigIntegerFieldElement{value=3, modulus=BigIntegerModulus{value=251}}, mac=BigIntegerFieldElement{value=4, modulus=BigIntegerModulus{value=251}}], product=AuthenticatedElement [share=BigIntegerFieldElement{value=5, modulus=BigIntegerModulus{value=251}}, mac=BigIntegerFieldElement{value=6, modulus=BigIntegerModulus{value=251}}]]";
    assertEquals(expected, triple.toString());
  }
}
