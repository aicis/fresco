package dk.alexandra.fresco.framework.builder.numeric.field;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * A naïve implementation that does not restrict the value of the modulus used. Alternative
 * implementations (e.g. {@link MersennePrimeModulus}) may impose special restrictions on the
 * modulus value.
 */
final class BigIntegerModulus implements Serializable {

  private static final long serialVersionUID = 1L;
  private final BigInteger value;

  /**
   * Creates a new modulus object. The bid integer must be larger than 0.
   *
   * @param value the underlying value to use as modulus.
   */
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
