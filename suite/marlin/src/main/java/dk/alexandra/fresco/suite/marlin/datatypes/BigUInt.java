package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.util.ArrayList;
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
   * Check if values is zero.
   */
  boolean isZero();

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

  /**
   * Compute pairwise product of elements.
   */
  static <S extends BigUInt<S>> List<S> product(List<S> left, List<S> right) {
    List<S> product = new ArrayList<>(left.size());
    for (int i = 0; i < left.size(); i++) {
      product.add(left.get(i).multiply(right.get(i)));
    }
    return product;
  }

  /**
   * Compute inner product of elements.
   */
  static <S extends BigUInt<S>> S innerProduct(List<S> left, List<S> right) {
    return sum(product(left, right));
  }

  // TODO hack hack hack

  long getLow();

  long getHigh();

  T shiftLowIntoHigh();

}
