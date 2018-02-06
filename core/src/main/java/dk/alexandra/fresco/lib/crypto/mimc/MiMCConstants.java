package dk.alexandra.fresco.lib.crypto.mimc;

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
	 * @param roundIndex
	 * @param mod
	 * @return
	 */
	public static BigInteger getConstant(int roundIndex, BigInteger mod) {
		Random rnd = new Random(roundIndex);
		BigInteger r;
		do {
		    r = new BigInteger(mod.bitLength(), rnd);
		} while (r.compareTo(mod) >= 0);
		return r;
	}
}
