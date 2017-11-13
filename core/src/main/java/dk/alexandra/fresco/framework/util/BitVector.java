package dk.alexandra.fresco.framework.util;

import java.util.BitSet;

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

  public int getSize() {
    return this.size;
  }

  public boolean get(int index) {
    rangeCheck(index);
    return this.bits.get(index);
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

}
