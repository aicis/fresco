package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.lib.common.math.integer.mod.Mod2m;
import java.math.BigInteger;

/**
 * Shifts a number to the right.
 */
public class RightShift implements Computation<RightShiftResult, ProtocolBuilderNumeric> {

  // Input
  private final DRes<SInt> input;
  private final int bitLength;
  private final int shifts;

  /**
   * @param input     The input.
   * @param bitLength An upper bound for the bitLength of the input.
   */
  public RightShift(DRes<SInt> input, int bitLength) {
    this(input, 1, bitLength);
  }

  public RightShift(DRes<SInt> input, int shifts, int bitLength) {

    if (shifts < 0) {
      throw new IllegalArgumentException();
    }

    this.bitLength = bitLength;
    this.input = input;
    this.shifts = shifts;
  }

  @Override
  public DRes<RightShiftResult> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(new Mod2m(input, shifts, bitLength,
        builder.getBasicNumericContext().getStatisticalSecurityParam())).seq(
        (seq, mod) ->
            Pair.lazy(seq.numeric().mult(BigInteger.ONE.shiftLeft(shifts)
                    .modInverse(builder.getBasicNumericContext().getModulus()),
                seq.numeric().sub(input, mod)), mod)
    ).seq((seq, result) -> DRes
        .of(new RightShiftResult(result.getFirst().out(), result.getSecond())));
  }
}
