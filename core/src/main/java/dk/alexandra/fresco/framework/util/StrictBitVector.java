package dk.alexandra.fresco.framework.util;

import java.util.Arrays;
import java.util.Random;

public class StrictBitVector {

  private byte[] bits;
  private int size;

  /**
   * Creates a StrictBitVector with all entries set to zero.
   *
   * @param size size of the vector
   */
  public StrictBitVector(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Size of vector must not be negative but was " + size);
    }
    if (size % 8 != 0) {
      throw new IllegalArgumentException("Size must be multiple of 8");
    }
    this.size = size;
    this.bits = new byte[size / 8];
  }

  /**
   * Constructs new strict bit vector.
   * 
   * @param bits raw bytes
   * @param size length in bits. must be a multiple of 8 (byte size)
   */
  public StrictBitVector(byte[] bits, int size) {
    if (size % 8 != 0) {
      throw new IllegalArgumentException("Size must be multiple of 8");
    }
    if ((bits.length * 8) != size) {
      throw new IllegalArgumentException("Size does not match byte array bit length");
    }
    this.bits = bits.clone();
    this.size = size;
  }

  /**
   * Constructs new strict bit vector from source of randomness.
   * 
   * @param bits raw bytes
   * @param rand source of randomness
   */
  public StrictBitVector(int size, Random rand) {
    this(ByteArrayHelper.randomByteArray(size / 8, rand), size);
  }
  
  /**
   * Returns the "bit" number bit, reading from left-to-right, from a byte array.
   * 
   * @param input The arrays of which to retrieve a bit
   * @param bit The index of the bit, counting from 0
   * @param isBigEndian Indicates whether the underlying byte array should be interpreted as
   *        big-endian or little-endian
   * @return Returns the "bit" number bit, reading from left-to-right, from "input"
   */
  public boolean getBit(int bit, boolean isBigEndian) {
    rangeCheck(bit);
    int actualIndex = isBigEndian ? size - 1 - bit : bit;
    return ByteArrayHelper.getBit(bits, actualIndex);
  }

  /**
   * Sets the bit at index to value, reading from left-to-right.
   * 
   * @param index index of the bit to be set
   * @param value value to set bit to
   * @param isBigEndian Indicates whether the underlying byte array should be interpreted as
   *        big-endian or little-endian 
   */
  public void setBit(int index, boolean value, boolean isBigEndian) {
    rangeCheck(index);
    int actualIndex = isBigEndian ? size - 1 - index : index;
    ByteArrayHelper.setBit(bits, actualIndex, value);
  }
  
  /**
   * Returns the "bit" number bit, reading from left-to-right, from a byte array.
   * <br>
   * Assumes underlying byte array is big-endian.
   * 
   * @param input The arrays of which to retrieve a bit
   * @param bit The index of the bit, counting from 0
   * @return Returns the "bit" number bit, reading from left-to-right, from "input"
   */
  public boolean getBit(int bit) {
    return getBit(bit, true);
  }
  
  /**
   * Sets the bit at index to value, reading from left-to-right.
   * <br>
   * Assumes underlying byte array is big-endian.
   * 
   * @param index index of the bit to be set
   * @param value value to set bit to
   */
  public void setBit(int index, boolean value) {
    setBit(index, value, true);
  }

  public int getSize() {
    return size;
  }

  public byte[] toByteArray() {
    return bits.clone();
  }

  /**
   * Concatenates bit vectors into one bit vector.
   * 
   * @param bitVectors
   * @return
   */
  public static StrictBitVector concat(StrictBitVector... bitVectors) {
    // compute length of result byte array and number of bits
    int combinedByteLength = 0;
    int combinedBitLength = 0;
    for (StrictBitVector bitVector : bitVectors) {
      combinedByteLength += bitVector.getSize() / 8;
      combinedBitLength += bitVector.getSize();
    }
    byte[] combined = new byte[combinedByteLength];
    int offset = 0;
    for (StrictBitVector bitVector : bitVectors) {
      byte[] rawBytes = bitVector.toByteArray();
      System.arraycopy(rawBytes, 0, combined, offset, rawBytes.length);
      offset += rawBytes.length;
    }
    return new StrictBitVector(combined, combinedBitLength);
  }

  private void rangeCheck(int bit) {
    if (bit < 0 || bit >= this.size) {
      throw new IndexOutOfBoundsException("Index out of bounds");
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(bits);
    result = prime * result + size;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StrictBitVector other = (StrictBitVector) obj;
    if (!Arrays.equals(bits, other.bits))
      return false;
    if (size != other.size)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "StrictBitVector [bits=" + Arrays.toString(bits) + "]";
  }

  /**
   * Updates this StrictBitVector to be the XOR with an other StrictBitVector.
   * 
   * @param other the other StrictBitVector
   * @throws IllegalArgumentException if the two BitVectors are not of equal size
   */
  public void xor(StrictBitVector other) {
    if (other.getSize() != this.getSize()) {
      throw new IllegalArgumentException("Vectors does not have same size");
    }
    ByteArrayHelper.xor(bits, other.bits);
  }

}
