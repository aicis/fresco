package dk.alexandra.fresco.framework.builder.numeric.field;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * An implementation of modulus assuming the prime is a psuedo Mersenne prime.
 */
final class MersennePrimeModulus implements Serializable {

  private static final long serialVersionUID = 7869304549721103721L;

  private final int bitLength;
  private final BigInteger constant;
  private final BigInteger precomputedBitMask;
  private final BigInteger prime;

  MersennePrimeModulus(int bitLength, int constant) {
    if (bitLength <= 0) {
      throw new IllegalArgumentException("Negative bit length");
    }
    if (constant <= 0) {
      throw new IllegalArgumentException("Negative constant");
    }
    this.bitLength = bitLength;
    this.constant = BigInteger.valueOf(constant);
    BigInteger shifted = BigInteger.ONE.shiftLeft(bitLength);
    this.precomputedBitMask = shifted.subtract(BigInteger.ONE);
    this.prime = shifted.subtract(BigInteger.valueOf(constant));
    if (prime.compareTo(BigInteger.ZERO) <= 0) {
      throw new IllegalArgumentException(
          "Constant is too large, the prime is now less than or equal to zero");
    }
  }

  BigInteger getPrime() {
    return prime;
  }

  @Override
  public String toString() {
    return "MersennePrimeModulus{"
        + "value=" + prime
        + '}';
  }

  /**
   * Takes the value and computes the equivalent value in
   * <i>0, ..., p - 1</i> for modulus <i>p</i>. This is used to sanitize the values after each
   * operation that could bring the value outside these bounds.
   *
   * @param value the value to make sure is in the correct interval.
   * @return <code>value mod this</code>
   */
  BigInteger ensureInField(BigInteger value) {
    int comparison = value.compareTo(prime);
    if (comparison < 0) {
      return value;
    } else if (comparison == 0) {
      return BigInteger.ZERO;
    }
    BigInteger quotient = value.shiftRight(bitLength);
    // q = z / b^n
    // r = z mod b^n
    BigInteger result = value.and(precomputedBitMask);
    while (quotient.signum() > 0) {
      BigInteger product = quotient.multiply(constant);
      //r = r + (c * q mod b^n)
      result = result.add(product.and(precomputedBitMask));

      //q = c*q / b^n
      quotient = product.shiftRight(bitLength);
    }

    while (result.compareTo(prime) >= 0) {
      result = result.subtract(prime);
    }
    return result;
  }
}
