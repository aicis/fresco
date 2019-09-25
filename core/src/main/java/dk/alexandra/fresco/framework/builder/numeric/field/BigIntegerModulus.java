package dk.alexandra.fresco.framework.builder.numeric.field;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

import dk.alexandra.fresco.framework.util.ModularReducer;

/**
 * A naïve implementation that does not restrict the value of the modulus used. Alternative
 * implementations (e.g. {@link MersennePrimeModulus}) may impose special restrictions on the
 * modulus value.
 */
final class BigIntegerModulus implements Serializable {

  private static final long serialVersionUID = 1L;
  private final BigInteger value;
  private final ModularReducer reducer;

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
    this.reducer = new ModularReducer(value);
  }

  BigInteger getBigInteger() {
    return value;
  }
  
  /**
   * Compute <i>x</i> modulus this modulus <i>M</i> assuming that <i>0 &le; x < M<sup>2</sup></i>
   * 
   * @param x A non-negative integer smaller than the modulus squared.
   * @return A BigInteger equal to x mod M.
   */
  BigInteger reduceModThis(BigInteger x) {
    return reducer.mod(x);
  }
  
  @Override
  public String toString() {
    return "BigIntegerModulus{"
        + "value=" + value
        + '}';
  }
}
