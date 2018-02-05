package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;

/**
 * Interface for representing unsigned integers larger than 64 bits.
 */
public interface BigUInt<T extends BigUInt> {

  /**
   * Update value of this to be sum of this and other {@code other}. <p>This is an in-place
   * operation.</p>
   */
  void addInPlace(T other);

  /**
   * Update value of this to be product of this and other {@code other}. <p>This is an in-place
   * operation.</p>
   */
  void multiplyInPlace(T other);

  /**
   * Compute sum of this and {@code other}.
   */
  BigUInt add(T other);

  /**
   * Compute product of this and {@code other}.
   */
  BigUInt multiply(T other);

  /**
   * Return bit length.
   */
  int getBitLength();

  /**
   * Return this as array of bytes. <p>Result is big-endian.</p>
   */
  byte[] toByteArray();

  /**
   * Return this as {@link BigInteger}.
   */
  BigInteger toBigInteger();

}
