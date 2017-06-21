package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;

public interface ComparisonBuilder<SIntT extends SInt> {

  /**
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 >= x2)
   */
  Computation<SIntT> compare(Computation<SIntT> x1, Computation<SIntT> x2);

  /**
   * Compares if x1 < x2, but with twice the possible bit-length.
   * Requires that the maximum bit length is set to something that can handle
   * this scenario.

   * Comparing long numbers and should use twice the bit length
   * @param x1 input
   * @param x2 input
   * @return output - [1] (true) or [0] (false) (result of x1 >= x2)
   */
  Computation<SIntT> compareLong(Computation<SIntT> x1, Computation<SIntT> x2);

  Computation<SIntT> sign(Computation<SIntT> x);
}
