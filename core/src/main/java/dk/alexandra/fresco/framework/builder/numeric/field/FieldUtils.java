package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Set of utility methods for the FieldDefinition.
 */
final class FieldUtils {

  private final int modulusLength;
  private final Function<BigInteger, FieldElement> creator;
  private final Function<FieldElement, BigInteger> toBigInteger;

  /**
   * Creates a new utility class.
   *
   * @param modulusBitLength the modulus bit length - translates to byte length in serialization
   * @param creator boot strap creator from open values and deserialization
   * @param toBigInteger extraction method for serialization and openings
   */
  FieldUtils(int modulusBitLength, Function<BigInteger, FieldElement> creator,
      Function<FieldElement, BigInteger> toBigInteger) {
    this.modulusLength = 1 + ((modulusBitLength - 1) / 8);
    this.creator = creator;
    this.toBigInteger = toBigInteger;
  }

  /**
   * Converts the supplied value to an strict bit vector by taking the bit representation
   * of the internal value.
   */
  StrictBitVector convertToBitVector(FieldElement value) {
    return new StrictBitVector(serialize(value));
  }

  /**
   * Serializes the field element to a byte array, the length as determined as the the bit length
   * of the modulus.
   *
   * @param value value to serialize
   * @return the value in a byte array
   */
  byte[] serialize(FieldElement value) {
    return serializeWithOffset(value, 0, new byte[modulusLength]);
  }

  private byte[] serializeWithOffset(FieldElement value, int offset, byte[] res) {
    byte[] bytes = toBigInteger.apply(value).toByteArray();
    int arrayStart = bytes.length > modulusLength ? bytes.length - modulusLength : 0;
    int resStart = bytes.length > modulusLength ? 0 : modulusLength - bytes.length;
    int len = Math.min(modulusLength, bytes.length);
    System.arraycopy(bytes, arrayStart, res, resStart + offset, len);
    return res;
  }

  /**
   * Reads the serialized field element from a byte array, the length is fixed and
   * determined as the the bit length of the modulus.
   *
   * @param bytes the value in a byte array
   * @return value deserialized
   */
  FieldElement deserialize(byte[] bytes) {
    return deserializeWithOffset(bytes, 0);
  }

  private FieldElement deserializeWithOffset(byte[] bytes, int offset) {
    byte[] actual;
    if (bytes.length == modulusLength) {
      actual = bytes;
    } else {
      actual = new byte[modulusLength];
      System.arraycopy(bytes, offset, actual, 0, modulusLength);
    }
    return creator.apply(new BigInteger(1, actual));
  }

  /**
   * Serializes a list of field elements to a byte array, similar to a single serialization.
   *
   * @param fieldElements values to serialize
   * @return the value in a byte array
   */
  byte[] serializeList(List<FieldElement> fieldElements) {
    byte[] bytes = new byte[modulusLength * fieldElements.size()];
    for (int i = 0; i < fieldElements.size(); i++) {
      serializeWithOffset(fieldElements.get(i), i * modulusLength, bytes);
    }
    return bytes;
  }

  /**
   * Reads a list of serialized field elements from a byte array, the length is fixed and
   * determined as the the bit length of the modulus for each value.
   *
   * @param bytes the values in a byte array
   * @return value deserialized
   */
  List<FieldElement> deserializeList(byte[] bytes) {
    ArrayList<FieldElement> elements = new ArrayList<>();
    for (int i = 0; i < bytes.length; i += modulusLength) {
      elements.add(deserializeWithOffset(bytes, i));
    }
    return elements;
  }

  /**
   * Implementation of {@link FieldDefinition#convertToSigned(BigInteger)}.
   *
   * @param value the value to convert
   * @param modulus the modulus to convert under
   * @param modulusHalf a precomputed value
   * @return the converted value according to the specification
   */
  static BigInteger convertRepresentation(BigInteger value, BigInteger modulus,
      BigInteger modulusHalf) {
    if (value.compareTo(modulusHalf) > 0) {
      return value.subtract(modulus);
    } else {
      return value;
    }
  }
}
