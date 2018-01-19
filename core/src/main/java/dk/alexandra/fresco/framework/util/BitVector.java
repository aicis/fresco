package dk.alexandra.fresco.framework.util;

/**
 * Interface defining a vector of bits and certain operations that can be 
 * executed on such a vector.
 */
public interface BitVector {

  /**
   * Returns the bit at a given index of this vector.
   *
   * @param index
   *          The index of the bit to access. First bit is at position 0.
   * @return The bit at position {@code index}
   */
  public boolean getBit(int index);

  /**
   * Set the bit in position {@code index} (counting from 0) to {@code value}.
   *
   * @param index
   *          The index of the bit to set
   * @param value
   *          The value which the bit at position {@code index} should take.
   */
  public void setBit(int index, boolean value);

  /**
   * Returns the amount of bits in the bit vector.
   *
   * @return The amount of bits in the bit vector
   */
  public int getSize();

  /**
   * Return the bit vector as a byte array, rounding up to the nearest byte if 
   * necessary.
   *
   * @return A byte array with the content of this bit vector
   */
  public byte[] toByteArray();

  /**
   * Updates the bit vector to be the XOR with an other bit vector.
   *
   * @param other
   *          the other bit vector
   */
  public void xor(BitVector other);
}
