package dk.alexandra.fresco.lib.compare.eq;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Implements an equality protocol -- given inputs x, y set output to x==y
 *
 * @author ttoft
 */
public class Equality implements Computation<SInt, ProtocolBuilderNumeric> {

  // params
  private final int bitLength;
  private final DRes<SInt> x;
  private final DRes<SInt> y;


  public Equality(
      int bitLength, DRes<SInt> x, DRes<SInt> y) {
    super();
    this.bitLength = bitLength;
    this.x = x;
    this.y = y;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> diff = builder.numeric().sub(x, y);
    return builder.comparison().compareZero(diff, bitLength);
  }
}