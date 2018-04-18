package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.DRes;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MathUtils {

  private static final BigInteger TWO = BigInteger.valueOf(2);

  private MathUtils() {
  }

  /**
   * Checks whether a value is a quadratic residue, i.e., if it has a square root mod modulus. </b>
   * Uses Euler's criterion (https://en.wikipedia.org/wiki/Euler%27s_criterion).
   *
   * @param value value to test (n)
   * @param modulus prime modulus (p)
   * @return true if value is quadratic residue
   */
  public static boolean isQuadraticResidue(BigInteger value, BigInteger modulus) {
    // return n^((p - 1) / 2) == 1
    BigInteger res = value
        .modPow(modulus.subtract(BigInteger.ONE).shiftRight(1), modulus);
    return res.equals(BigInteger.ONE);
  }

  /**
   * Find square root modulo a prime (if the square root exists). </b> Implements Tonelli–Shanks
   * algorithm (https://en.wikipedia.org/wiki/Tonelli%E2%80%93Shanks_algorithm). Variable names
   * correspond to variable names in article.
   *
   * @param value value to find square root of (n)
   * @param modulus prime modulus (p)
   * @return square root (if it exists)
   */
  public static BigInteger modularSqrt(BigInteger value, BigInteger modulus) {
    // check if square root exists
    if (!isQuadraticResidue(value, modulus)) {
      throw new IllegalArgumentException("Value has no square root in field");
    }

    // find q and s such that (p - 1) = q * 2^s
    Pair<BigInteger, Integer> factors = expressAsProductOfPowerOfTwo(
        modulus.subtract(BigInteger.ONE));
    BigInteger q = factors.getFirst();
    int s = factors.getSecond();

    // find non-quadratic residue for field
    BigInteger z = getNonQuadraticResidue(modulus);

    int m = s;
    BigInteger c = z.modPow(q, modulus);
    BigInteger t = value.modPow(q, modulus);
    BigInteger r = value.modPow(q.add(BigInteger.ONE).divide(TWO), modulus);

    while (!t.equals(BigInteger.ONE)) {
      int i = 0;
      BigInteger power = t;
      while (!power.equals(BigInteger.ONE)) {
        power = power.pow(2).mod(modulus);
        i++;
      }
      BigInteger exp = TWO.pow(m - i - 1).mod(modulus);
      BigInteger b = c.modPow(exp, modulus);

      m = i;
      c = b.pow(2).mod(modulus);
      t = t.multiply(c).mod(modulus);
      r = r.multiply(b).mod(modulus);
    }
    return r;
  }

  /**
   * Computes the sum of elements with modular wrap-around.
   */
  public static BigInteger sum(List<BigInteger> summands, BigInteger modulus) {
    return summands.stream()
        .reduce(BigInteger::add)
        .orElse(BigInteger.ZERO)
        .mod(modulus);
  }

  /**
   * Turns input value into bits in big-endian order. <p>If the actual bit length of the value is
   * smaller than numBits, the result is padded with 0s. If the bit length is larger only the first
   * numBits bits are used.</p>
   */
  public static List<BigInteger> toBits(BigInteger value, int numBits) {
    List<BigInteger> bits = new ArrayList<>(numBits);
    for (int b = 0; b < numBits; b++) {
      boolean boolBit = value.testBit(b);
      bits.add(boolBit ? BigInteger.ONE : BigInteger.ZERO);
    }
    Collections.reverse(bits);
    return bits;
  }

  /**
   * Same as {@link #toBits(BigInteger, int)}, but wraps result bits in DRes.
   */
  public static List<DRes<BigInteger>> toBitsAsDRes(BigInteger value, int numBits) {
    List<DRes<BigInteger>> bits = new ArrayList<>(numBits);
    for (int b = 0; b < numBits; b++) {
      boolean boolBit = value.testBit(b);
      bits.add(boolBit ? () -> BigInteger.ONE : () -> BigInteger.ZERO);
    }
    Collections.reverse(bits);
    return bits;
  }

  /**
   * Find non-quadratic residue for field.
   *
   * @param modulus modulus of field
   * @return value that is not a quadratic residue
   */
  private static BigInteger getNonQuadraticResidue(BigInteger modulus) {
    BigInteger candidate = TWO;
    while (isQuadraticResidue(candidate, modulus)) {
      candidate = candidate.add(BigInteger.ONE);
    }
    return candidate;
  }

  /**
   * Find values q and s such that input equals q * 2^s and q is odd.
   *
   * @param value input value
   * @return q and s
   */
  private static Pair<BigInteger, Integer> expressAsProductOfPowerOfTwo(BigInteger value) {
    BigInteger q = value;
    int s = 0;
    // keep factoring out 2 until q is not divisible by it anymore
    while (q.mod(TWO).equals(BigInteger.ZERO)) {
      q = q.shiftRight(1); // divide by 2
      s++;
    }
    return new Pair<>(q, s);
  }

}
