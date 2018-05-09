package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

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
   * Convert {@link BigInteger} to {@link OInt}.
   */
  OInt fromBigInteger(BigInteger value);

  /**
   * Default method for converting multiple instances of {@link BigInteger}.
   */
  default List<DRes<OInt>> fromBigInteger(List<BigInteger> values) {
    // TODO shouldn't be a DRes
    return values.stream().map(this::fromBigInteger).collect(Collectors.toList());
  }

  /**
   * Returns representation of the value 0.
   */
  OInt zero();

  /**
   * Returns representation of the value 1.
   */
  OInt one();

  /**
   * Returns representation of the value 2.
   */
  OInt two();

}
