package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;

/**
 * A factory for creating instances of {@link OInt} from native java data types such as {@link
 * java.math.BigInteger} and vice versa. <p>Arithmetic backend suites must implement this. A basic
 * implementation for open values wrapping {@link BigInteger} instances is provided by {@link
 * BigIntegerOIntFactory}.</p>
 */
public interface OIntFactory {

  /**
   * Convert open value to {@link BigInteger}.
   */
  BigInteger toBigInteger(OInt value);

  /**
   * Convert open value to long.
   */
  default long toLong(OInt value) {
    return toBigInteger(value).longValue();
  }

  /**
   * Convert {@link BigInteger} to {@link OInt}.
   */
  OInt fromBigInteger(BigInteger value);

  /**
   * Convert long to {@link OInt}.
   */
  default OInt fromLong(long value) {
    return fromBigInteger(BigInteger.valueOf(value));
  }

}
