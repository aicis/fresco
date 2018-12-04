package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Deterministically seeded generator for MiMC round constants.
 */
public class MimcConstants {

  /**
   * Map used to cache mimc round constants.
   */
  private static Map<Pair<Integer, BigInteger>, BigInteger> roundConstants = new HashMap<>();

  private MimcConstants() {
    // Should not be instantiated
  }

  /**
   * Returns a random but deterministic constant depending on the round and the field size. It might
   * be a better idea to pre-generate the constants using SecureRandom, and distribute them among
   * the participants if run in production.
   *
   * @param roundIndex the round index
   * @param mod the modulus used in the computation
   * @return a constant for the given round and modulus
   */
  public static BigInteger getConstant(int roundIndex, BigInteger mod) {
    Pair<Integer, BigInteger> index = new Pair<>(roundIndex, mod);
    if (!roundConstants.containsKey(index)) {
      BigInteger constant = generateConstant(roundIndex, mod);
      roundConstants.put(index, constant);
      return constant;
    }
    return roundConstants.get(index);
  }

  private static BigInteger generateConstant(int roundIndex, BigInteger mod) {
    Random rnd = new Random(roundIndex);
    BigInteger r;
    do {
      r = new BigInteger(mod.bitLength(), rnd);
    } while (r.compareTo(mod) >= 0);
    return r;
  }
}
