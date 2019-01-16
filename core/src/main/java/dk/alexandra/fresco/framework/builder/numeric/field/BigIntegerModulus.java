package dk.alexandra.fresco.framework.builder.numeric.field;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * This class is a naïve implementation of a modulus, other field structures would
 * have more complicated data structures than this.
 */
final class BigIntegerModulus implements Serializable {

  private static final long serialVersionUID = 1L;
  private final BigInteger value;

  BigIntegerModulus(BigInteger value) {
    if (value.compareTo(BigInteger.ZERO) <= 0) {
      throw new IllegalArgumentException("Only positive modulus is acceptable");
    }
    this.value = Objects.requireNonNull(value);
  }

  BigInteger getBigInteger() {
    return value;
  }

  @Override
  public String toString() {
    return "BigIntegerModulus{"
        + "value=" + value
        + '}';
  }
}
