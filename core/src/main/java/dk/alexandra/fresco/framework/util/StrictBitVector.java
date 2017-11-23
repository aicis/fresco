package dk.alexandra.fresco.framework.util;

import java.util.Arrays;
import java.util.Random;

public class StrictBitVector {

  private byte[] bits;
  private int size;

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
    this.bits = bits;
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
   * @return Returns the "bit" number bit, reading from left-to-right, from "input"
   */
  public boolean getBit(int bit) {
    rangeCheck(bit);
    return ByteArrayHelper.getBit(bits, bit);
  }

  public int getSize() {
    return size;
  }

  public byte[] toByteArray() {
    return bits;
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

}
