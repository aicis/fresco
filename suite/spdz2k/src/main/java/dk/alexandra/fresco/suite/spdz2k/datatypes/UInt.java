package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;
import java.util.List;

/**
 * Interface for representing unsigned integers.
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
   * Compute arithmetic negation of this and return. <p>Note that the result will have modular wrap
   * around mod 2^bitLength since the value is unsigned. Equivalent to computing unsigned result of
   * (this * (2^bitLength - 1)).</p>
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

  /**
   * Compute sum of elements.
   */
  static <S extends UInt<S>> S sum(List<S> elements) {
    return elements.stream().reduce(UInt::add).orElse(elements.get(0));
  }

  /**
   * Compute inner product of elements.
   */
  static <S extends UInt<S>> S innerProduct(List<S> left, List<S> right) {
    S accumulator = left.get(0).multiply(right.get(0));
    for (int i = 1; i < left.size(); i++) {
      accumulator = accumulator.add(left.get(i).multiply(right.get(i)));
    }
    return accumulator;
  }

  /**
   * Short-hand for Integer.toUnsignedLong().
   */
  static long toUnLong(int value) {
    return value & 0xffffffffL;
  }

}
