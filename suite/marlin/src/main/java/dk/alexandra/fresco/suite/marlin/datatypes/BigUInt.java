package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.util.List;

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
   * Update value of this to be difference of this and other {@code other}. <p>This is an in-place
   * operation.</p>
   */
  void subtractInPlace(T other);

  /**
   * Negates the value of this (with wrap-around). <p>This is an in-place operation.</p>
   */
  void negateInPlace();

  /**
   * Compute sum of this and {@code other}.
   */
  T add(T other);

  /**
   * Compute product of this and {@code other}.
   */
  T multiply(T other);

  /**
   * Compute difference of this and {@code other}.
   */
  T subtract(T other);

  /**
   * Compute negation of this and return.
   */
  T negate();

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

  /**
   * Compute sum of elements.
   */
  static <S extends BigUInt<S>> S sum(List<S> elements) {
    return elements.stream().reduce(BigUInt::add).get();
  }

}
