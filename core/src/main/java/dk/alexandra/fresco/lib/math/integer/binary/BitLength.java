package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Finds the bit length of an integer. This is done by finding the bit representation of the integer
 * and then returning the index of the highest set bit.
 */
public class BitLength implements Computation<SInt, ProtocolBuilderNumeric> {

  private DRes<SInt> input;
  private int maxBitLength;

  /**
   * Create a protocol for finding the bit length of an integer. This is done by finding the bit
   * representation of the integer and then returning the index of the highest set bit.
   *
   * @param input An integer.
   * @param maxBitLength An upper bound for the bit length.
   */
  public BitLength(DRes<SInt> input, int maxBitLength) {
    this.input = input;
    this.maxBitLength = maxBitLength;

  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) -> {
      //Find the bit representation of the input
      return seq.advancedNumeric().toBits(input, maxBitLength);
    }).seq((seq, bits) -> {
      DRes<SInt> mostSignificantBitIndex = null;
      Numeric numeric = seq.numeric();
      for (int n = 0; n < maxBitLength; n++) {
        // If bits[n] == 1 we let mostSignificantIndex be current index.
        // Otherwise we leave it be.
        SInt remainderResult = bits.get(n);
        if (mostSignificantBitIndex == null) {
          mostSignificantBitIndex = numeric.mult(BigInteger.valueOf(n), () -> remainderResult);
        } else {
          DRes<SInt> sub = numeric.sub(BigInteger.valueOf(n), mostSignificantBitIndex);
          DRes<SInt> mult = numeric.mult(() -> remainderResult, sub);
          mostSignificantBitIndex = numeric.add(mult, mostSignificantBitIndex);
        }
      }
      // We are interested in the bit length of the input, so we add one to
      // the index of the most significant bit since the indices are counte from 0
      return numeric.add(BigInteger.ONE, mostSignificantBitIndex);
    });
  }

}
