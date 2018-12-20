package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import java.io.Serializable;

/**
 * A field element is one element of the finite Galois field it belongs to defined
 * by {@link FieldDefinition}.
 */
public interface FieldElement extends Serializable, Addable<FieldElement> {

  /**
   * Subtracts an element from this element
   *
   * @param other operand
   * @return this - other
   */
  FieldElement subtract(FieldElement other);

  /**
   * Negates this element.
   *
   * @return -this or rather modulus-this
   */
  FieldElement negate();

  /**
   * Multiplies an element from this element
   *
   * @param other operand
   * @return this * other
   */
  FieldElement multiply(FieldElement other);

  /**
   * Computes the square root of this element.
   *
   * @return x, where x*x = this
   */
  FieldElement sqrt();

  /**
   * Computes the modular inverse of this element.
   *
   * @return this<sup>-1</sup> mod modulus
   */
  FieldElement modInverse();
}
