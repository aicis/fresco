package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Random;

/**
 * Class for representing a vector of bits. Uses {@link BitSet} to hold the vector.
 */
public class BitVector {

  private BitSet bits;
  private int size;

  /**
   * Creates a BitVector from a <code>boolean</code> array.
   *
   * @param array a <code>boolean</code> array
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
   * Creates a random BitVector using source of randomness.
   *
   * @param size size of the vector
   * @param rand source of randomness
   */
  public BitVector(int size, Random rand) {
    // TODO: revisit this
    this(new BigInteger(size, rand).toByteArray(), size);
  }

  public int getSize() {
    return this.size;
  }

  public boolean get(int index) {
    rangeCheck(index);
    return this.bits.get(index);
  }

  /**
   * Creates new BitVector containing specified range of bits of this BitVector.
   * 
   * @param from
   * @param to
   * @return
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

  public void set(int index, boolean value) {
    rangeCheck(index);
    this.bits.set(index, value);
  }

  public byte[] asByteArr() {
    return bits.toByteArray();
  }

  public boolean[] asBooleans() {
    return BitSetUtils.toArray(bits, size);
  }

  /**
   * Updates this BitVector to be the XOR with an other BitVector.
   * 
   * @param other the other BitVector
   * @throws IllegalArgumentException if the two BitVectors are not of equal size
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

  public static BitVector concat(BitVector... bitVectors) {
    for (BitVector bitVector : bitVectors) {
      if (bitVector.getSize() % 8 != 0) {
        throw new IllegalArgumentException(
            "Only support concatenation of bit vectors of length divisible by 8");
      }
    }
    return null;
  }

}
