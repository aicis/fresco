package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;

public interface ModularReductionAlgorithm {

  /**
   * Return an instance of {@link ModularReductionAlgorithm} capable of performing modular reduction
   * modulo the provided value.
   * 
   * @param modulus The fixed modulus used by the reduction algorithm.
   * @return A modular reduction algorithm that can reduce an input modulus the given fixed modulus.
   */
  public static ModularReductionAlgorithm getReductionAlgorithm(BigInteger modulus) {
    if (modulus.bitCount() == 1) {
      return new ModularReduction2k(modulus.bitLength() - 1);
    } else {
      return new BarrettReduction(modulus);
    }
  }

  /**
   * Reduce <i>x</i> modulus a fixed modulus.
   * 
   * @param x An integer with absolute value smaller than the modulus squared.
   * @return The remainder of x divided by the modulus.
   */
  public BigInteger apply(BigInteger x);

  /**
   * This modular reduction algorithm works with moduli that are powers of two by performing a
   * bitwise AND on the input and the modulus - 1.
   */
  static class ModularReduction2k implements ModularReductionAlgorithm {
    
    private final BigInteger mask;

    private ModularReduction2k(int power) {
      this.mask = BigInteger.ONE.shiftLeft(power).subtract(BigInteger.ONE);
    }

    @Override
    public BigInteger apply(BigInteger x) {
      return x.and(mask);
    }

  }

  /**
   * This modular reduction algorithm is based on the
   * <a href="https://en.wikipedia.org/wiki/Barrett_reduction">BarrettReduction</a>.
   */
  static class BarrettReduction implements ModularReductionAlgorithm {

    private final int k;
    private final BigInteger m;
    private final BigInteger r;

    private BarrettReduction(BigInteger modulus) {
      if (modulus.compareTo(BigInteger.valueOf(3)) <= 0) {
        throw new IllegalArgumentException("Modulus must be greater than 3");
      }

      this.k = 2 * modulus.bitLength();
      this.m = modulus;
      this.r = BigInteger.ONE.shiftLeft(k).divide(modulus);
    }

    /**
     * Compute <i>x</i> modulus the value provided in the constructor.
     * 
     * @param x A non-negative integer smaller than the modulus squared.
     * @return The remainder of x divided by the modulus
     */
    private BigInteger applyPositive(BigInteger x) {
      BigInteger t = x.subtract(x.multiply(r).shiftRight(k).multiply(m));
      if (t.compareTo(m) < 0) {
        return t;
      } else {
        return t.subtract(m);
      }
    }

    public BigInteger apply(BigInteger x) {
      if (x.signum() >= 0) {
        // Non-negative input
        return applyPositive(x);
      } else {
        // Negative input
        BigInteger mod = applyPositive(x.negate());
        if (mod.signum() > 0) {
          return m.subtract(mod);
        } else {
          return BigInteger.ZERO;
        }
      }
    }
  }
}
