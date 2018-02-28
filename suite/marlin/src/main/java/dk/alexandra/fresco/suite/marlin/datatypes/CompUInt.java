package dk.alexandra.fresco.suite.marlin.datatypes;

/**
 * An unsigned integer conceptually composed of two other unsigned integers. <p>Composite integers
 * have a bit length of t = s + k, where the s most significant bits can be viewed as a s-bit
 * integer of type HighT and the k least significant bits as a k-bit integer of type LowT. The
 * underlying representation is big endian.</p>
 */
public interface CompUInt<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends UInt<CompT>> extends UInt<CompT> {

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
   * TODO
   */
  HighT computeOverflow();

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

}
