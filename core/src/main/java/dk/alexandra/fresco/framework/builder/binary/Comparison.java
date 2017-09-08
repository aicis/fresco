package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

public interface Comparison extends ComputationDirectory {

  /**
   * Performs a greater than operation on the two inputted bitstrings which each represents a number
   * in base 2.
   * 
   * @param inLeft The left secret shared bitstring
   * @param inRight The right secret shared bitstring
   * @return inLeft > inRight
   */
  DRes<SBool> greaterThan(List<DRes<SBool>> inLeft, List<DRes<SBool>> inRight);

  /**
   * Performs an equality operation on the two inputted bitstrings.
   * 
   * @param inLeft The left secret shared bitstring
   * @param inRight The right secret shared bitstring
   * @return inLeft == inRight
   */
  DRes<SBool> equal(List<DRes<SBool>> inLeft, List<DRes<SBool>> inRight);
}
