package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;

public interface ComparisonBuilder {

  /**
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 >= x2)
   */
  Computation<SInt> compare(Computation<SInt> x1, Computation<SInt> x2);

  /**
   * Compares if x1 < x2, but with twice the possible bit-length.
   * Requires that the maximum bit length is set to something that can handle
   * this scenario.

   * Comparing long numbers and should use twice the bit length
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 >= x2)
   */
  Computation<SInt> compareLong(Computation<SInt> x1, Computation<SInt> x2);

  Computation<SInt> sign(Computation<SInt> x);

}
