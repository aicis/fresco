package dk.alexandra.fresco.framework.util;

import java.util.Arrays;
import java.util.Collections;

/**
 * This class represents a bit vector. Internally the bit vector is represented by a byte array.
 * This is done to make it easy and fast to carry out internal operations and bit manipulations.
 * However, this also means that an instance MUST contain an amount of bits which is divisible by 8
 * since a byte always contains 8 bits.
 */
public class StrictBitVector implements BitVector {

  private byte[] bits;
  private final int size;

  /**
   * Constructs new strict bit vector, using the byte array given as input for the internal
   * representation. Thus modifying this byte array DIRECTLY modifies the bits in this StrictBitVector
   * object.
   *
   * @param bits
   *          raw bytes
   */
  public StrictBitVector(byte[] bits) {
    this.bits = bits.clone();
    // Note that the amount of bits in the bit vector is the amount of bytes in
    // {@code bits} multiplied
    this.size = bits.length * 8;
  }

  /**
   * Creates a StrictBitVector with all entries set to zero.
   *
   * @param size bit size of the vector
   */
  public StrictBitVector(int size) {
    if (size % 8 != 0) {
      throw new IllegalArgumentException("Size must be multiple of 8");
    }
    this.size = size;
    this.bits = new byte[size / 8];
  }

  /**
   * Constructs new strict bit vector from a secure source of randomness.
   *
   * @param size the bit size of the vector
   * @param rand secure source of randomness
   */
  public StrictBitVector(int size, Drbg rand) {
    this(size);
    rand.nextBytes(bits);
  }

  /**
   * Returns the but at a given index of this vector.
   *
   * @param index
   *          The index of the bit, counting from 0
   * @param isBigEndian
   *          Indicates whether the underlying byte array should be interpreted
   *          as big-endian or little-endian
   * @return Returns the bit at the given index
   */
  public boolean getBit(int index, boolean isBigEndian) {
    rangeCheck(index);
    int actualIndex = isBigEndian ? size - 1 - index : index;
    return ByteArrayHelper.getBit(bits, actualIndex);
  }

  @Override
  public boolean getBit(int bit) {
    return getBit(bit, true);
  }

  /**
   * Sets the bit at index to value, reading from left-to-right.
   *
   * @param index
   *          index of the bit to be set
   * @param value
   *          value to set bit to
   * @param isBigEndian
   *          Indicates whether the underlying byte array should be interpreted 
   *          as big-endian or little-endian
   */
  public void setBit(int index, boolean value, boolean isBigEndian) {
    rangeCheck(index);
    int actualIndex = isBigEndian ? size - 1 - index : index;
    ByteArrayHelper.setBit(bits, actualIndex, value);
  }

  @Override
  public void setBit(int index, boolean value) {
    setBit(index, value, true);
  }

  @Override
  public int getSize() {
    return size;
  }

  /**
   * Returns a reference to the internal byte array representing the bit vector. 
   * Thus modifying this  byte array DIRECTLY modifies the bits in this 
   * StrictBitVector object.
   * <p>
   * The representation is big-endian, that is the first 8 bits will be in the 
   * last byte.
   * </p>
   *
   * @return A byte array with the content of this bit vector
   */
  @Override
  public byte[] toByteArray() {
    return bits;
  }

  /**
   * Generates a string representation of this vector.
   *
   * @return A string with the binary representation of this vector
   */
  public String asBinaryString() {
    String binStr = "";
    for (int b = 0; b < size; b++) {
      binStr += getBit(b) ? "1" : "0";
    }
    return binStr;
  }

  @Override
  public void xor(BitVector other) {
    ByteArrayHelper.xor(bits, other.toByteArray());
  }

  @Override
  public String toString() {
    return "StrictBitVector [bits=" + Arrays.toString(bits) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(bits);
    // no need to include size parameter since it is derived directly from bits
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    StrictBitVector other = (StrictBitVector) obj;
    if (!Arrays.equals(bits, other.bits)) {
      return false;
    }
    // no need to check size parameter since it is derived directly from bits
    return true;
  }

  /**
   * Constructs a new StrictBitVector which is the concatenation of the vectors 
   * given as input.
   *
   * @param bitVectors
   *          the vectors to concatenate
   * @return the concatenated vector
   */
  public static StrictBitVector concat(StrictBitVector... bitVectors) {
    return concat(false, bitVectors);
  }

  /**
   * Constructs a new StrictBitVector which is the concatenation of the vectors 
   * given as input.
   *
   * @param reverse
   *          indicating whether or not to reverse the list before concatenation
   * @param bitVectors
   *          the vectors to concatenate
   * @return the concatenated vector
   */
  public static StrictBitVector concat(boolean reverse,
      StrictBitVector... bitVectors) {
    if (reverse) {
      Collections.reverse(Arrays.asList(bitVectors));
    }
    // compute length of result byte array and number of bits
    int combinedBitLength = 0;
    for (StrictBitVector bitVector : bitVectors) {
      combinedBitLength += bitVector.getSize();
    }
    byte[] combined = new byte[combinedBitLength / 8];
    int offset = 0;
    for (StrictBitVector bitVector : bitVectors) {
      byte[] rawBytes = bitVector.toByteArray();
      System.arraycopy(rawBytes, 0, combined, offset, rawBytes.length);
      offset += rawBytes.length;
    }
    return new StrictBitVector(combined);
  }

  private void rangeCheck(int bit) {
    if (bit < 0 || bit >= this.size) {
      throw new IndexOutOfBoundsException("Index out of bounds");
    }
  }
}