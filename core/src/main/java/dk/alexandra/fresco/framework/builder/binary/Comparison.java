package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

public interface ComparisonBuilderBinary {

  /**
   * Performs a greater than operation on the two inputted bitstrings which each represents a number
   * in base 2.
   * 
   * @param inLeft The left secret shared bitstring
   * @param inRight The right secret shared bitstring
   * @return inLeft > inRight
   */
  Computation<SBool> greaterThan(List<Computation<SBool>> inLeft, List<Computation<SBool>> inRight);

  /**
   * Performs an equality operation on the two inputted bitstrings.
   * 
   * @param inLeft The left secret shared bitstring
   * @param inRight The right secret shared bitstring
   * @return inLeft == inRight
   */
  Computation<SBool> equal(List<Computation<SBool>> inLeft, List<Computation<SBool>> inRight);
}
