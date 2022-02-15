package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.integer.mod.Mod2m;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts a number to its bit representation.
 */
public class IntegerToBitsLogRounds implements
    Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int bits;
  private final boolean inputBitLengthBounded;

  /**
   * Compute the first <i>m</i> bits of the input.
   *
   * @param input The input
   * @param m     The number of bits
   */
  public IntegerToBitsLogRounds(DRes<SInt> input, int m) {
    this(input, m, true);
  }

  /**
   * Compute the first <i>m</i> bits of the input. If <i>inputBitLengthBounded</i> is set to
   * <i>false</i>, the input need have bitlength at most <i>m</i> for the computation to return the
   * correct result.
   *
   * @param input                 The input
   * @param m                     Number of bits to compute
   * @param inputBitLengthBounded Should
   */
  private IntegerToBitsLogRounds(DRes<SInt> input, int m, boolean inputBitLengthBounded) {
    this.input = input;
    this.bits = m;
    this.inputBitLengthBounded = inputBitLengthBounded;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {

    // If the input bit length is not bounded, we need to replace it with input mod 2^m in order
    // to get the right result
    DRes<SInt> value = inputBitLengthBounded ? input
        : builder.seq(seq -> AdvancedNumeric.using(seq).mod2m(input, bits));

    if (bits == 1) {
      return DRes.of(Collections.singletonList(value));
    }

    int bitsBottom = bits / 2;
    int bitsTop = bits - bitsBottom;
    return builder.seq(seq -> {

      // Split the input in bottom = input mod 2^m and top = (input - bottom) 2^{-m}
      DRes<SInt> bottom = seq.seq(new Mod2m(value, bitsBottom, bits,
          seq.getBasicNumericContext().getStatisticalSecurityParam()));
      DRes<SInt> top = seq.numeric().mult(
          BigInteger.ONE.shiftLeft(bitsBottom)
              .modInverse(seq.getBasicNumericContext().getModulus()),
          seq.numeric().sub(value, bottom));
      return Pair.lazy(bottom, top);

    }).pairInPar(

        // Compute the bits of the first bitsBottom bits and the top in parallel
        (par, split) -> par.seq(new IntegerToBitsLogRounds(split.getFirst(), bitsBottom, false)),
        (par, split) -> par.seq(new IntegerToBitsLogRounds(split.getSecond(), bitsTop, false)))

        .seq((seq, splitResult) -> {
          List<DRes<SInt>> result = new ArrayList<>();
          result.addAll(splitResult.getFirst());
          result.addAll(splitResult.getSecond());
          return DRes.of(result);
        });

  }
}
