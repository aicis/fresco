package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * An unsigned integer conceptually composed of two other unsigned integers. <p>Composite integers
 * have a bit length of t = s + k, where the s most significant bits can be viewed as a s-bit
 * integer of type HighT and the k least significant bits as a k-bit integer of type LowT. The
 * underlying representation is big endian.</p>
 */
public interface CompUInt<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends UInt<CompT> & FieldElement> extends UInt<CompT>, FieldElement {

  /**
   * Get the s most significant bits as an unsigned integer of type {@link HighT}.
   */
  HighT getMostSignificant();

  /**
   * Get the k least significant bits as an unsigned integer of type {@link LowT}.
   */
  LowT getLeastSignificant();

  /**
   * Get the s least significant bits as an unsigned integer of type {@link HighT}.
   */
  HighT getLeastSignificantAsHigh();

  /**
   * Left-shift the k least significant bits by k.
   */
  CompT shiftLowIntoHigh();

  /**
   * Get length of least significant bit segment, i.e., k.
   */
  int getLowBitLength();

  /**
   * Get length of most significant bit segment, i.e., s.
   */
  int getHighBitLength();

  /**
   * Returns total bit length, i.e., k + s.
   */
  default int getCompositeBitLength() {
    return getHighBitLength() + getLowBitLength();
  }

  @Override
  default int getBitLength() {
    return getCompositeBitLength();
  }

  @Override
  default FieldElement sqrt() {
    throw new NotImplementedException();
  }

  @Override
  default FieldElement modInverse() {
    throw new IllegalStateException("Can't invert ring element");
  }

  @Override
  default FieldElement add(FieldElement other) {
    return add((CompT) other);
  }

  @Override
  default FieldElement subtract(FieldElement other) {
    return subtract((CompT) other);
  }

  @Override
  default FieldElement multiply(FieldElement other) {
    return multiply((CompT) other);
  }

  @Override
  default FieldElement negate() {
    return negateUInt();
  }

  /**
   * Util for padding a byte array up to the required bit length. <p>Since we are working with
   * unsigned ints, the first byte of the passed in array is discarded if it's a zero byte.</p>
   */
  static byte[] pad(byte[] bytes, int requiredBitLength) {
    byte[] padded = new byte[requiredBitLength / Byte.SIZE];
    // potentially drop byte containing sign bit
    boolean dropSignBitByte = (bytes[0] == 0x00);
    int bytesLen = dropSignBitByte ? bytes.length - 1 : bytes.length;
    if (bytesLen > padded.length) {
      throw new IllegalArgumentException("Exceeds capacity");
    }
    int srcPos = dropSignBitByte ? 1 : 0;
    System.arraycopy(bytes, srcPos, padded, padded.length - bytesLen, bytesLen);
    return padded;
  }

}
