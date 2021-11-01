package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
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
    return builder.seq(
      new NormalizeSInt(input, maxBitLength)).seq((seq, result) ->
        seq.numeric().sub(maxBitLength, result.getSecond())
    );
  }

}
