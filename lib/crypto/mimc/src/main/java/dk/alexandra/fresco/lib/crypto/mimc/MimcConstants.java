package dk.alexandra.fresco.lib.crypto.mimc;

import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministically seeded generator for MiMC round constants.
 *
 * <p>Constants are sampled from a DRNG with seed set to 0 (this should be sufficiently "random" for
 * MiMC). Each round constant is sampled in order of the round numbers. This should ensure that each
 * party samples the same round constants in the same order.
 */
public final class MimcConstants implements MimcRoundConstantFactory {

  private final List<BigInteger> roundConstants;
  private final Drng drng;

  public MimcConstants() {
    this.drng = new DrngImpl(AesCtrDrbgFactory.fromDerivedSeed((byte) 0x00));
    this.roundConstants = new ArrayList<>();
  }

  /**
   * Returns a random but deterministic constant depending on the round and the modulus.
   *
   * @param roundIndex the round index
   * @param mod the modulus used in the computation
   * @return a constant for the given round and modulus
   */
  @Override
  public BigInteger getConstant(int roundIndex, BigInteger mod) {
    if (roundIndex > roundConstants.size() - 1) {
      int diff = roundIndex - (roundConstants.size() - 1);
      for (int i = 0; i < diff; i++) {
        BigInteger constant = drng.nextBigInteger(mod);
        roundConstants.add(constant);
      }
    }
    return roundConstants.get(roundIndex);
  }
}
