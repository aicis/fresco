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

  Computation<SIntT> sign(Computation<SIntT> x);
}
