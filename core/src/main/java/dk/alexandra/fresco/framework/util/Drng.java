package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;

/**
 * Interface for Deterministic Random Number Generators.
 */
public interface Drng {

  /**
   * Gets the next integer of this DRNG in a given range
   * @param limit a limit on the value returned
   * @return an integer in the range <i>0,...,limit</i>
   */
  int nextInt(int limit);

  /**
   * Gets the next long of this DRNG in a given range
   * @param limit a limit on the value returned
   * @return an long in the range <i>0,...,limit</i>
   */
  long nextLong(long limit);

  /**
   * Gets the next integer of this DRNG in a given range
   * 
   * @param limit
   *          a limit on the value returned
   * @return an integer in the range <i>0,...,limit-1</i>
   */
  BigInteger nextBigInteger(BigInteger limit);

}
