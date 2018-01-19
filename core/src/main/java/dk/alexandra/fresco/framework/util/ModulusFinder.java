package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * Helper class for computing suitable prime moduli for a given bit length.
 */
public class ModulusFinder {

  private static final HashMap<Integer, BigInteger> precomputed = new HashMap<>();

  static {
    precomputed.put(8, new BigInteger("251"));
    precomputed.put(16, new BigInteger("65519"));
    precomputed.put(64, new BigInteger("18446744073709551557"));
    precomputed.put(128, new BigInteger("340282366920938463463374607431768211283"));
    precomputed.put(256, new BigInteger(
        "115792089237316195423570985008687907853269984665640564039457584007913129639349"));
  }

  private static BigInteger compute(int modBitLength) {
    // TODO implement
    throw new UnsupportedOperationException();
  }

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
