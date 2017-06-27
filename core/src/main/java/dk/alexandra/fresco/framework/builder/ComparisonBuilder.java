package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;

public interface ComparisonBuilder {

  /**
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 <= x2)
   */
  Computation<SInt> compare(Computation<SInt> x1, Computation<SInt> x2);

  /**
   * Compares if x1 < x2, but with twice the possible bit-length.
   * Requires that the maximum bit length is set to something that can handle
   * this scenario.

   * Comparing long numbers and should use twice the bit length
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 <= x2)
   */
  Computation<SInt> compareLong(Computation<SInt> x1, Computation<SInt> x2);

  /**
   * Compares if x == y.
   *
   * @param x input
   * @param y input
   * @return output - [1] (true) or [0] (false) (result of x == y)
   */
  Computation<SInt> equals(Computation<SInt> x, Computation<SInt> y);

  Computation<SInt> sign(Computation<SInt> x);

  /**
   * Test for equality with zero for a bitLength-bit number (positive or negative)
   *
   * @param x the value to test against zero
   * @param bitLength bitlength
   * @return output - [1] (true) or [0] (false) (result of x == 0)
   */
  Computation<SInt> compareZero(Computation<SInt> x, int bitLength);
}
