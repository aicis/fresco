package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import java.math.BigInteger;
import java.util.Random;

/**
 * Deterministically seeded generator for MiMC round constants.
 */
public class MiMCConstants {

  /**
   * Returns a random but deterministic constant depending on the round and the field size.
   * It might be a better idea to pre-generate the constants using SecureRandom, and distribute
   * them among the participants if run in production.
   */
  public static BigInteger getConstant(int roundIndex, Modulus mod) {
    Random rnd = new Random(roundIndex);
    BigInteger r;
    do {
      r = new BigInteger(mod.getBigInteger().bitLength(), rnd);
    } while (r.compareTo(mod.getBigInteger()) >= 0);
    return r;
  }
}
