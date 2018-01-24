package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * Helper class for computing suitable prime moduli for a given bit length.
 */
public class ModulusFinder {

  /**
   * Values pre-computed using {@link #compute(int)}.
   */
  private static final HashMap<Integer, BigInteger> precomputed = new HashMap<>();
  private static final BigInteger TWO = BigInteger.valueOf(2);
  private static final BigInteger THREE = BigInteger.valueOf(3);
  private static final int CERTAINTY = 1024;

  static {
    precomputed.put(8, new BigInteger("251"));
    precomputed.put(16, new BigInteger("65519"));
    precomputed.put(32, new BigInteger("4294967291"));
    precomputed.put(64, new BigInteger("18446744073709551557"));
    precomputed.put(128, new BigInteger("340282366920938463463374607431768211283"));
    precomputed.put(256, new BigInteger(
        "115792089237316195423570985008687907853269984665640564039457584007913129639349"));
  }

  private static boolean areCoprime(BigInteger a, BigInteger b) {
    return a.gcd(b).equals(BigInteger.ONE);
  }

  private static BigInteger compute(int modBitLength) {
    // modBitLength - 1 because of the extra sign bit
    BigInteger candidate = TWO.shiftLeft(modBitLength - 1);
    while (!candidate.isProbablePrime(CERTAINTY) || !areCoprime(candidate.subtract(BigInteger.ONE),
        THREE)) {
      candidate = candidate.subtract(BigInteger.ONE);
    }
    return candidate;
  }

  /**
   * Find closest prime p to 2^modBitLength such that p - 1 is comprime with 3 (required to MiMC
   * functionality).
   */
  public static BigInteger findSuitableModulus(int modBitLength) {
    if (modBitLength % 8 != 0) {
      throw new IllegalArgumentException("Bit length must be divisible by 8");
    }
    if (precomputed.containsKey(modBitLength)) {
      return precomputed.get(modBitLength);
    } else {
      return compute(modBitLength);
    }
  }

}
