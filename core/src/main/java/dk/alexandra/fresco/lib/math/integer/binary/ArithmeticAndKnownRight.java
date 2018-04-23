package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes logical AND for each bit in the two lists. <p>Bits are represented as arithmetic
 * elements. The AND operation is expressed via an arithmetic gate.</p>
 */
public class ArithmeticAndKnownRight implements
    ComputationParallel<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SInt>>> leftBits;
  private final DRes<List<DRes<OInt>>> rightBits;

  /**
   * Constructs new {@link ArithmeticAndKnownRight}.
   *
   * @param leftBits secret bits represented as arithmetic elements
   * @param rightBits open bits represented as arithmetic elements
   */
  public ArithmeticAndKnownRight(
      DRes<List<DRes<SInt>>> leftBits,
      DRes<List<DRes<OInt>>> rightBits) {
    this.leftBits = leftBits;
    this.rightBits = rightBits;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> leftOut = leftBits.out();
    List<DRes<OInt>> rightOut = rightBits.out();
    List<DRes<SInt>> andedBits = new ArrayList<>(leftOut.size());
    for (int i = 0; i < leftOut.size(); i++) {
      DRes<SInt> leftBit = leftOut.get(i);
      DRes<OInt> rightBit = rightOut.get(i);
      // logical and of two bits can be computed as product
      DRes<SInt> andedBit = builder.seq(seq -> seq.numeric().multByOpen(rightBit, leftBit));
      andedBits.add(andedBit);
    }
    return () -> andedBits;
  }

}
