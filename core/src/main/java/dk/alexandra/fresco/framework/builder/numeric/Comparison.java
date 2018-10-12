package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Interface for comparing numeric values.
 */
public interface Comparison extends ComputationDirectory {

  /**
   * The different algorithms supported by Fresco. The enum is used to decide of whether an
   * algorithm running in constant rounds or logarithmic rounds should be used. In general the
   * logarithmic round choice is the fastest and is therefore used by default.
   */
  enum ComparisonAlgorithm {
    LOG_ROUNDS, CONST_ROUNDS
  }

  ComparisonAlgorithm DEFAULT_EQUALITY = ComparisonAlgorithm.LOG_ROUNDS;

  /**
   * Compares two values and return x == y
   *
   * @param bitLength The maximum bit-length of the numbers to compare.
   * @param x The first number
   * @param y The second number
   * @return A deferred result computing x == y
   */
  default DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y) {
    return equals(bitLength, x, y, DEFAULT_EQUALITY);
  }

  /**
   * Call to {@link #equals(int, DRes, DRes, ComparisonAlgorithm)} with default algorithm {@link
   * #DEFAULT_EQUALITY}.
   */
  DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y);

  DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y, ComparisonAlgorithm algorithm);

  /**
   * Computes x == y.
   *
   * @param bitLength the amount of bits to do the equality test on. Must be less than or equal to
   * the max bit length allowed
   * @param x the first input
   * @param y the second input
   * @param algorithm the algorithm to use
   * @return A deferred result computing x' == y'. Where x' and y' represent the {@code bitlength}
   * least significant bits of x, respectively y. Result will be either [1] (true) or [0] (false).
   */
  DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y, ComparisonAlgorithm algorithm);

  /**
   * Computes if x1 <= x2.
   *
   * @param x1 input
   * @param x2 input
   * @return A deferred result computing x1 <= x2. Result will be either [1] (true) or [0] (false).
   */
  DRes<SInt> compareLEQ(DRes<SInt> x1, DRes<SInt> x2);

  /**
   * Compares if x1 <= x2, but with twice the possible bit-length. Requires that the maximum bit
   * length is set to something that can handle this scenario. It has to be at least less than half
   * the modulus bit size.
   *
   * @param x1 input
   * @param x2 input
   * @return A deferred result computing x1 <= x2. Result will be either [1] (true) or [0] (false).
   */
  DRes<SInt> compareLEQLong(DRes<SInt> x1, DRes<SInt> x2);

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
   * @param bitLength bitlength
   * @return A deferred result computing x == 0. Result will be either [1] (true) or [0] (false)
   */
  DRes<SInt> compareZero(DRes<SInt> x, int bitLength);
}
