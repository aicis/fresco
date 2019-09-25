package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;

/**
 * This class can perform modular reduction for a fixed modulus using the
 * <a href="https://en.wikipedia.org/wiki/Barrett_reduction">Barrett reduction algorithm</a>.
 */
public class ModularReducer {

  private BigInteger m;
  private BigInteger r;
  private int k;

  public ModularReducer(BigInteger m) {
    this.m = m;
    this.k = m.bitLength() + 1;
    this.r = BigInteger.ONE.shiftLeft(2 * k).divide(m);
  }

  /**
   * Compute <i>x mod m</i> for the <i>m</i> provided in the constructor and <i>0 &le; x <
   * m<sup>2</sup></i>.
   * 
   * @param x An non-negative integer smaller than <i>m<sup>2</sup></i>
   * @return
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
