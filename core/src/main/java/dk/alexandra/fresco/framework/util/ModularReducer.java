package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;

/**
 * This class can perform modular reduction for a fixed modulus using the
 * <a href="https://en.wikipedia.org/wiki/Barrett_reduction">Barrett reduction algorithm</a>.
 */
public class ModularReducer {

  private final BigInteger m;
  private final BigInteger r;
  private final int k;

  public ModularReducer(BigInteger modulus) {
    this.m = modulus;
    this.k = modulus.bitLength() + 1;
    this.r = BigInteger.ONE.shiftLeft(2 * k).divide(modulus);
  }

  /**
   * Compute <i>x mod m</i> for the modulus <i>m</i> provided in the constructor and <i>0 &le; x <
   * m<sup>2</sup></i>.
   * 
   * @param x A non-negative integer smaller than the modulus squared.
   * @return x mod m
   */
  public BigInteger mod(BigInteger x) {
    BigInteger t = x.subtract(x.multiply(r).shiftRight(2 * k).multiply(m));

    if (t.compareTo(m) < 0) {
      return t;
    } else {
      return t.subtract(m);
    }
  }

}
