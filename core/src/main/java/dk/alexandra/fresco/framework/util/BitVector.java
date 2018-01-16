package dk.alexandra.fresco.framework.util;

import java.util.BitSet;

/**
 * Class for representing a vector of bits. Uses {@link BitSet} to hold the vector.
 * This class allows the construction of bit vector containing an arbitrary amount
 * of elements. Specifically the amount of bits does not need to be divisible by 8.
 */
public class BitVector {

  private final BitSet bits;
  private final int size;

  /**
   * Creates a BitVector from a <code>boolean</code> array.
   *
   * @param array
   *          a <code>boolean</code> array
   */
  public BitVector(boolean[] array) {
    this.bits = BitSetUtils.fromArray(array);
    this.size = array.length;
  }

  /**
   * Creates a BitVector from an array of bytes.
   *
   * @param array an array of bytes
   * @param size the number of bits of <code>array</code> to use for this vector
   */
  public BitVector(byte[] array, int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Size of vector must not be negative but was " + size);
    }
    this.bits = BitSet.valueOf(array);
    this.size = size;
  }

  /**
   * Creates a BitVector with all entries set to zero.
   *
   * @param size size of the vector
   */
  public BitVector(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Size of vector must not be negative but was " + size);
    }
    this.size = size;
    this.bits = new BitSet(size);
  }

  /**
   * Returns the amount of bit contained in this bit vector.
   *
   * @return the amount of bit contained in this bit vector
   */
  public int getSize() {
    return this.size;
  }

  /**
   * Returns the bit in position {@code index}. Counting from 0.
   *
   * @param index
   *          The index of the bit to retrieve
   * @return The bit at position {@code index}
   */
  public boolean get(int index) {
    rangeCheck(index);
    return this.bits.get(index);
  }

  /**
   * Creates new BitVector containing specified range of bits of this BitVector.
   * The bit at position {@code from} (counting from 0) will be included.
   * The bit at position {@code to} (counting from 0) will be excluded.
   *
   * @param from
   *          The position of the first bit to include in the range
   * @param to
   *          The position of the bit AFTER the last bit to include in the range
   * @return A new {@BitVector} containing the subset of [{@code from}, {@code to}[
   *          from this bit vector
   */
  public BitVector get(int from, int to) {
    rangeCheck(from);
    // What is returned excludes the to index
    rangeCheck(to - 1);
    int length = to - from;
    if (length < 0) {
      throw new IndexOutOfBoundsException(
          "From index is smaller than to index. From: " + from + " to: " + to);
    }
    BitSet subrange = bits.get(from, to);
    return new BitVector(subrange.toByteArray(), length);
  }

  /**
   * Set the bit in position {@code index} (counting from 0) to {@code value}.
   *
   * @param index
   *          The index of the bit to set
   * @param value
   *          The value which the bit at position {@code index} should take.
   */
  public void set(int index, boolean value) {
    rangeCheck(index);
    this.bits.set(index, value);
  }

  /**
   * Return the bit vector as a byte array, rounding up to the nearest byte if
   * necessary. The representation is little-endian.
   *
   * @return A byte array with the content of this bit vector
   */
  public byte[] asByteArr() {
    return bits.toByteArray();
  }

  /**
   * Updates this BitVector to be the XOR with another BitVector.
   *
   * @param other
   *          the other BitVector
   * @throws IllegalArgumentException
   *           if the two BitVectors are not of equal size
   */
  public void xor(BitVector other) {
    if (other.getSize() != this.getSize()) {
      throw new IllegalArgumentException("Vectors does not have same size");
    }
    bits.xor(other.bits);
  }

  private void rangeCheck(int i) {
    if (i < 0 || i >= this.size) {
      throw new IndexOutOfBoundsException(
          "Cannot access index " + i + " on vector of size " + this.size);
    }
  }
}
