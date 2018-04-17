package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes logical XOR for each bit in the two lists. <p>Bits are represented as represented as
 * arithmetic elements. The XOR operation is expressed via an arithmetic gate.</p>
 */
public class ArithmeticXorKnownRight implements
    ComputationParallel<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private static final BigInteger TWO = BigInteger.valueOf(2);
  private final DRes<List<DRes<SInt>>> leftBits;
  private final DRes<List<DRes<BigInteger>>> rightBits;

  /**
   * Constructs new {@link ArithmeticXorKnownRight}.
   *
   * @param leftBits secret bits represented as arithmetic elements
   * @param rightBits open bits represented as arithmetic elements
   */
  public ArithmeticXorKnownRight(
      DRes<List<DRes<SInt>>> leftBits,
      DRes<List<DRes<BigInteger>>> rightBits) {
    this.leftBits = leftBits;
    this.rightBits = rightBits;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> leftOut = leftBits.out();
    List<DRes<BigInteger>> rightOut = rightBits.out();
    List<DRes<SInt>> xoredBits = new ArrayList<>(leftOut.size());
    for (int i = 0; i < leftOut.size(); i++) {
      DRes<SInt> leftBit = leftOut.get(i);
      BigInteger rightBit = rightOut.get(i).out();
      // logical xor of two bits can be computed as leftBit + rightBit - 2 * leftBit * rightBit
      DRes<SInt> xoredBit = builder.seq(seq -> {
        Numeric nb = seq.numeric();
        return nb.sub(
            nb.add(rightBit, leftBit),
            nb.mult(TWO, nb.mult(rightBit, leftBit))
        );
      });
      xoredBits.add(xoredBit);
    }
    return () -> xoredBits;
  }

}
