package dk.alexandra.fresco.lib.compare.eq;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Implements an equality protocol -- given inputs x, y set output to x==y.
 *
 */
public class Equality implements Computation<SInt, ProtocolBuilderNumeric> {

  // params
  private final int bitLength;
  private final DRes<SInt> left;
  private final DRes<SInt> right;

  /**
   * Constructs an instance of the Equality computation.
   * 
   * @param bitLength The maximum bit length of the inputs.
   * @param left The first element to compare.
   * @param right The second element to compare.
   */
  public Equality(
      int bitLength, DRes<SInt> left, DRes<SInt> right) {
    super();
    this.bitLength = bitLength;
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> diff = builder.numeric().sub(left, right);
    return builder.comparison().compareZero(diff, bitLength);
  }
}