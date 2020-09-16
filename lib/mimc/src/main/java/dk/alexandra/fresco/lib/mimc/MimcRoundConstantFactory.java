package dk.alexandra.fresco.lib.mimc;

import java.math.BigInteger;

/**
 * A factory providing the random round constants needed in the MiMC cipher.
 */
public interface MimcRoundConstantFactory {

  /**
   * Returns the round constant for a given round number and modulus.
   *
   * <p>
   * Note: while these must random, all parties must agree on the round constants of each round. The
   * round constants only needs to be picked once and can then be hard coded into the cipher and
   * used forever.
   * </p>
   *
   * @param roundIndex the round index
   * @param mod the modulus
   * @return the specified round constant
   */
  public BigInteger getConstant(int roundIndex, BigInteger mod);

}
