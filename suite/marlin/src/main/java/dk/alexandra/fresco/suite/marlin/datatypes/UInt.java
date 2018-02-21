package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for representing unsigned integers larger than 64 bits.
 */
public interface UInt<T extends UInt> {

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
   * Returns this as long.
   */
  long toLong();

  /**
   * Returns this as int.
   */
  int toInt();

  // TODO these belong on the factory

  /**
   * Compute sum of elements.
   */
  static <S extends UInt<S>> S sum(List<S> elements) {
    return elements.stream().reduce(UInt::add).get();
  }

  /**
   * Compute pairwise product of elements.
   */
  static <S extends UInt<S>> List<S> product(List<S> left, List<S> right) {
    List<S> product = new ArrayList<>(left.size());
    for (int i = 0; i < left.size(); i++) {
      product.add(left.get(i).multiply(right.get(i)));
    }
    return product;
  }

  /**
   * Compute inner product of elements.
   */
  static <S extends UInt<S>> S innerProduct(List<S> left, List<S> right) {
    return sum(product(left, right));
  }

  /**
   * Short-hand for Integer.toUnsignedLong().
   */
  static long toUnLong(int value) {
    return value & 0xffffffffL;
  }

}
