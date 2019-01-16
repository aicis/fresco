package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;

/**
 * Describes a finite field.
 * The field is defined by a <i>modulus</i> (i.e., the order of the field) and each element is
 * represented as a non-negative integer smaller than the modulus.
 */
public interface FieldDefinition extends ByteSerializer<FieldElement> {

  /**
   * Creates an element in the field from the supplied value resulting in
   * <code>value mod modulus</code>, where modulus taken from the field definition
   *
   * @param value the value to convert into the field
   * @return the created field element
   */
  FieldElement createElement(long value);

  /**
   * Creates an element in the field from the supplied value resulting in
   * <code>value mod modulus</code>, where modulus taken from the field definition
   *
   * @param value the value to convert into the field
   * @return the created field element
   */
  FieldElement createElement(String value);

  /**
   * Creates an element in the field from the supplied value resulting in
   * <code>value mod modulus</code>, where modulus taken from the field definition
   *
   * @param value the value to convert into the field
   * @return the created field element
   */
  FieldElement createElement(BigInteger value);

  /**
   * Gets the modulus for this finite field.
   *
   * @return the created field element
   */
  BigInteger getModulus();

  /**
   * Gets the bit length for numbers in this field.
   *
   * @return the bit length
   */
  int getBitLength();

  /**
   * Creates a StrictBitVector from an element.
   *
   * @param fieldElement The element to convert
   * @return the element represented as a bit vector
   */
  StrictBitVector convertToBitVector(FieldElement fieldElement);

  /**
   * Returns the element represented as an non-negative integer in the range
   * <i>0, ..., p - 1</i> for modulus <i>p</i>.
   *
   * @param value The element to convert
   * @return the unsigned representation as a BigInteger
   */
  BigInteger convertToUnsigned(FieldElement value);

  /**
   * Takes an unsigned big integer and interprets it as a normal integer.
   * <p>
   * This method returns the asUnsigned if in the range <i>0, ..., p/2 - 1</i> for modulus <i>p</i>.
   * This method returns the asUnsigned - modulus if in the range <i>p/2, ..., p - 1</i>
   * for modulus <i>p</i>.
   * </p><p>
   * Users of the framework would know themselves if a previously converted field element should
   * be cleansed through this method.
   * </p>
   *
   * @param asUnsigned The element to convert
   * @return asUnsigned if the value hereof is positive and close to 0, asUnsigned - modulus if
   *     close to modulus
   */
  BigInteger convertToSigned(BigInteger asUnsigned);
}

