package dk.alexandra.fresco.framework.value;

import java.util.List;

/**
 * Helper class for implementing various arithmetic operations on open values.
 */
public interface OIntArithmetic {

  /**
   * Returns the number one as a deferred opened int.
   */
  OInt one();

  /**
   * Turns input value into bits in big-endian order.
   * <p>
   * If the actual bit length of the value is smaller than numBits, the result is padded with 0s. If
   * the bit length is larger only the first numBits bits are used.
   * </p>
   */
  List<OInt> toBits(OInt openValue, int numBits);

  /**
   * Returns a list of powers of two in ascending order, up to numPowers - 1 ([2^0, 2^1, ...,
   * 2^{numPowers - 1}]).
   */
  List<OInt> getPowersOfTwo(int numPowers);

  /**
   * Computes 2^{power}.
   */
  OInt twoTo(int power);

  /**
   * Reduces {@code input} modulo 2^{power}.
   *
   * @param input
   *          the input to reduce
   * @param power
   *          the two-power to reduce against
   * @return the reduced input modulo the two-power
   */
  OInt modTwoTo(OInt input, int power);

}
