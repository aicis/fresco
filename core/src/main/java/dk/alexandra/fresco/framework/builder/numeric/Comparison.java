package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;

public interface Comparison extends ComputationDirectory {

  DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y);

  /**
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 <= x2)
   */
  DRes<SInt> compareLEQ(DRes<SInt> x1, DRes<SInt> x2);

  /**
   * Compares if x1 <= x2, but with twice the possible bit-length.
   * Requires that the maximum bit length is set to something that can handle
   * this scenario. It has to be at least less than half the modulus bit size. 
   *
   * Comparing long numbers and uses twice the maximum bit length.
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 <= x2)
   */
  DRes<SInt> compareLEQLong(DRes<SInt> x1, DRes<SInt> x2);

  /**
   * Compares if x == y.
   *
   * @param x input
   * @param y input
   * @return output - [1] (true) or [0] (false) (result of x == y)
   */
  DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y);

  DRes<SInt> sign(DRes<SInt> x);

  /**
   * Test for equality with zero for a bitLength-bit number (positive or negative)
   *
   * @param x the value to test against zero
   * @param bitLength bitlength
   * @return output - [1] (true) or [0] (false) (result of x == 0)
   */
  DRes<SInt> compareZero(DRes<SInt> x, int bitLength);
}
