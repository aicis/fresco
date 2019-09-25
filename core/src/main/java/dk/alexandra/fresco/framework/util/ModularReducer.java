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
    if (modulus.compareTo(BigInteger.valueOf(3)) <= 0) {
      throw new IllegalArgumentException("Modulus must be greater than 3");
    }
    
    if (modulus.bitCount() == 1) {
      throw new IllegalArgumentException("Modulus cannot be a power of two");
    }
    
    this.m = modulus;
    this.k = modulus.bitLength();
    this.r = BigInteger.ONE.shiftLeft(2 * k).divide(modulus);
  }

  /**
   * Compute <i>x</i> modulus the value provided in the constructor.
   * 
   * @param x A non-negative integer smaller than the modulus squared.
   * @return The remainder of x divided by the modulus
   */
  private BigInteger modPositive(BigInteger x) {
    BigInteger t = x.subtract(x.multiply(r).shiftRight(2 * k).multiply(m));

    if (t.compareTo(m) < 0) {
      return t;
    } else {
      return t.subtract(m);
    }
  }
  
  /**
   * Compute <i>x</i> modulus the value provided in the constructor.
   * 
   * @param x An integer with absolute value smaller than the modulus squared.
   * @return The remainder of x divided by the modulus
   */
  public BigInteger mod(BigInteger x) {
    if (x.signum() >= 0) {
      // Non-negative input
      return modPositive(x);
    } else {
      // Negative input
      BigInteger mod = modPositive(x.abs());
      if (mod.signum() > 0) {
        return m.subtract(mod);
      } else {
        return BigInteger.ZERO;
      }     
    }
  }
  

}
