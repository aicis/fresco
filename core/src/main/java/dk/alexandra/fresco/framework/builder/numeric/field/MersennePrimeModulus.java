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

  // Precomputed values used for modular inverse
  private static final int[] a = {1, 2, 3, 6, 12, 15, 30, 60, 120, 240, 255};
  private int b0, j0, d0;

  /**
   * Creates a modulus assuming a psuedo Mersenne prime in the form:
   * <code>2<sup>bitLength</sup>-constant</code>. Users must choose constant adequately for
   * the modulus to actually be a prime.
   *
   * @param bitLength the bitlength of the psuedo Mersenne
   * @param constant  the (small) constant
   */
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

    initInverse();
  }

  /**
   * Gets the prime used for modulus as a BigInteger.
   *
   * @return the BigInteger equivalent
   */
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

  /** Precompute some values used in the modular inverse which are independent of the input */
  private void initInverse() {
    this.b0 = 0;
    int w0 = 1;
    int c = constant.intValue();
    while (w0 < c + 2) {
      w0 = 2 * w0;
      b0 = b0 + 1;
    }

    this.j0 = w0 - c - 2;
    this.d0 = 10;
    while (a[d0] > j0) {
      d0 = d0 - 1;
    }
    j0 = j0 - a[d0];
  }

  /**
   * Compute the inverse modulo this modulus
   */
  BigInteger inverse(BigInteger value) {

    if (value.equals(BigInteger.ONE)) {
      return value;
    }

    // For small moduli we use BigInteger's modInverse
    if (bitLength < 16) {
      return value.modInverse(prime);
    }

    // We use algorithm 1 from https://eprint.iacr.org/2018/1038.pdf
    int n = bitLength;

    // Phase 1
    BigInteger[] h = computePhase1(value);

    // Use precomputed values for b, j and d
    int b = b0;
    int j = j0;
    int d = d0;

    // Calculate key
    BigInteger k = calculateKey(h, b, j, d);

    // Phase 2
    h = computePhase2(h, j, b);

    // Complete addition chain
    BigInteger r = completeAdditionChain(h, n, b);

    // Phase 3
    r= computePhase3(b,k,r);

    return r;
  }
  BigInteger computePhase3(int b, BigInteger k,BigInteger r)
  {
    for (int i = 0; i < b; i++) {
      r = ensureInField(r.multiply(r));
    }
    r = ensureInField(r.multiply(k));
    return r;
  }

  private BigInteger[] computePhase1(BigInteger value) {
    int n = bitLength;
    BigInteger[] h = new BigInteger[11];
    h[0] = value;
    h[1] = ensureInField(h[0].multiply(h[0]));
    h[2] = ensureInField(h[0].multiply(h[1]));
    h[3] = ensureInField(h[2].multiply(h[2]));
    h[4] = ensureInField(h[3].multiply(h[3]));
    h[5] = ensureInField(h[4].multiply(h[2]));
    h[6] = ensureInField(h[5].multiply(h[5]));
    h[7] = ensureInField(h[6].multiply(h[6]));
    h[8] = ensureInField(h[7].multiply(h[7]));
    h[9] = ensureInField(h[8].multiply(h[8]));
    h[10] = ensureInField(h[9].multiply(h[5]));
    return h;
  }

  private BigInteger calculateKey(BigInteger[] h, int b, int j, int d) {
    BigInteger k = h[d];
    while (j != 0) {
      d = d - 1;
      if (j >= a[d]) {
        k = ensureInField(k.multiply(h[d]));
        j = j - a[d];
      }
    }
    return k;
  }
  private BigInteger[] computePhase2(BigInteger[] h, int j, int b) {
    int m = 8;
    int n = bitLength - b;
    while (2 * m < n) {
      BigInteger t = h[j];
      j = j + 1;
      for (int i = 0; i < m; i++) {
        t = ensureInField(t.multiply(t));
      }
      h[j] = ensureInField(t.multiply(h[j - 1]));
      m = 2 * m;
    }
    return new BigInteger[]{h[2], h[5], h[10]};
  }
  private BigInteger completeAdditionChain(BigInteger[] h, int n, int b) {
    int l = n - (2 * b);
    int j = 3;
    int m = 8;
    BigInteger r = h[j];
    while (l != 0) {
      m = m / 2;
      j = j - 1;
      if (l >= m) {
        l = l - m;
        BigInteger t = r;
        for (int i = 0; i < m; i++) {
          t = ensureInField(t.multiply(t));
        }
        r = ensureInField(t.multiply(h[j]));
      }
    }
    for (int i = 0; i < b; i++) {
      r = ensureInField(r.multiply(r));
    }
    return r;
  }
}
