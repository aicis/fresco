package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.util.SIntPair;
import java.math.BigInteger;
import java.util.List;

/**
 * Interface for comparing numeric values.
 */
public interface Comparison extends ComputationDirectory {

  /**
   * Create a new Comparison using the given builder.
   *
   * @param builder The root builder to use.
   * @return A new Comparison computation directory.
   */
  static Comparison using(ProtocolBuilderNumeric builder) {
    return new DefaultComparison(builder);
  }

  /**
   * The different algorithms supported by Fresco. The enum is used to decide of whether an
   * algorithm running in constant rounds or logarithmic rounds should be used. In general the
   * logarithmic round choice is the fastest.
   */
  enum Algorithm {
    LOG_ROUNDS, CONST_ROUNDS
  }

  /**
   * Computes x == y.
   *
   * @param x the first input
   * @param y the second input
   * @param bitlength the amount of bits to do the equality test on. Must be less than or equal to
   * the max bitlength allowed
   * @param algorithm the algorithm to use
   * @return A deferred result computing x' == y'. Where x' and y' represent the {@code bitlength}
   * least significant bits of x, respectively y. Result will be either [1] (true) or [0] (false).
   */
  DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y, int bitlength, Algorithm algorithm);

  /**
   * Call to {@link #equals(DRes, DRes, int, Algorithm)} with default comparison algorithm.
   */
  default DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y, int bitlength) {
    return equals(x, y, bitlength, Algorithm.LOG_ROUNDS);
  }

  /**
   * Call to {@link #equals(DRes, DRes, int, Algorithm)} with default comparison algorithm, checking
   * equality of all bits.
   */
  DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y);

  /**
   * Computes if x &le; y.
   *
   * @param x the first input
   * @param y the second input
   * @return A deferred result computing x &le; y. Result will be either [1] (true) or [0] (false).
   */
  @Deprecated
  DRes<SInt> compareLEQ(DRes<SInt> x, DRes<SInt> y);

  /**
   * Computes if x &lt; y.
   *
   * @param x the first input. Must be less than 2^{@code bitlength}
   * @param y the second input. Must be less than 2^{@code bitlength}
   * @param bitlength the amount of bits to do the comparison on. Must be less than or equal to
   * the max bitlength allowed
   * @param algorithm the algorithm to use
   * @return A deferred result computing x' &lt; y'. Where x' and y' represent the {@code bitlength}
   * least significant bits of x, respectively y. Result will be either [1] (true) or [0] (false).
   */
  DRes<SInt> compareLT(DRes<SInt> x, DRes<SInt> y, int bitlength, Algorithm algorithm);

  /**
   * Call to {@link #compareLT(DRes, DRes, int, Algorithm)} with default comparison algorithm.
   */
  default DRes<SInt> compareLT(DRes<SInt> x, DRes<SInt> y, int bitlength) {
    return compareLT(x, y, bitlength, Algorithm.LOG_ROUNDS);
  }

  /**
   * Call to {@link #compareLT(DRes, DRes, int, Algorithm)} with default comparison algorithm,
   * comparing all bits.
   */
  DRes<SInt> compareLT(DRes<SInt> x, DRes<SInt> y);

  /**
   * Computes if the bit decomposition of an open value is less than the bit decomposition of a
   * secret value.
   *
   * @param openValue open value which will be decomposed into bits and compared to secretBits
   * @param secretBits secret value decomposed into bits
   */
  DRes<SInt> compareLTBits(BigInteger openValue, DRes<List<DRes<SInt>>> secretBits);

  /**
   * Compares if x &le; y, but with twice the possible bit-length. Requires that the maximum bit
   * length is set to something that can handle this scenario. It has to be at least less than half
   * the modulus bit size.
   *
   * @param x the first input
   * @param y the second input
   * @return A deferred result computing x &le; y. Result will be either [1] (true) or [0] (false).
   */
  @Deprecated
  DRes<SInt> compareLEQLong(DRes<SInt> x, DRes<SInt> y);

  /**
   * Computes the sign of the value (positive or negative)
   *
   * @param x The value to compute the sign off
   * @return A deferred result computing the sign. Result will be 1 if the value is positive
   * (including 0) and -1 if negative.
   */
  DRes<SInt> sign(DRes<SInt> x);

  /**
   * Test for equality with zero for a bitLength-bit number (positive or negative)
   *
   * @param x the value to test against zero
   * @param bitlength the amount of bits to do the zero-test on. Must be less than or equal to the
   * modulus bitlength
   * @param algorithm the algorithm to use for zero-equality test
   * @return A deferred result computing x' == 0 where x' is the {@code bitlength} least significant
   * bits of x. Result will be either [1] (true) or [0] (false)
   */
  DRes<SInt> compareZero(DRes<SInt> x, int bitlength, Algorithm algorithm);

  /**
   * Computes the index of the minimum element in a list and the element itself. <p>The index is
   * expressed as a list of bits where all bits are 0 except for the bit at the index of the minimum
   * element, which is set to 1.</p>
   */
  DRes<Pair<List<DRes<SInt>>, SInt>> argMin(List<DRes<SInt>> xs);

  /**
   * Call to {@link #compareZero(DRes, int, Algorithm)} with default comparison algorithm.
   */
  default DRes<SInt> compareZero(DRes<SInt> x, int bitlength) {
    return compareZero(x, bitlength, Algorithm.LOG_ROUNDS);
  }

}
