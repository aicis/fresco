package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric.RandomAdditiveMask;
import java.math.BigInteger;

/**
 * Returns a number which is approximately the input shifted a number of positions to the right. The
 * result will be one larger than the exact result with some non-negligible probability. If you need
 * the exact result you need to use {@link RightShift} instead, but this will be at a significant
 * performance cost.
 * <p>
 * The protocol is similar to protocol 3.1 in Catrina O., Saxena A. (2010) Secure Computation with
 * Fixed-Point Numbers. In: Sion R. (eds) Financial Cryptography and Data Security. FC 2010. Lecture
 * Notes in Computer Science, vol 6052. Springer, Berlin, Heidelberg.
 */
public class Truncate implements Computation<SInt, ProtocolBuilderNumeric> {

  // Input
  private final DRes<SInt> input;
  private final int shifts;
  private final int maxBitLength;

  public Truncate(DRes<SInt> input, int shifts, int maxBitLength) {
    this.input = input;
    this.maxBitLength = maxBitLength;
    this.shifts = shifts;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    if (shifts >= maxBitLength) {
      return builder.numeric().known(0);
    }

    return builder.seq(seq -> {

      /*
       * Generate random additive mask of the same length as the input + some extra to avoid
       * leakage.
       */
      return AdvancedNumeric.using(seq)
          .additiveMask(maxBitLength + builder.getBasicNumericContext().getStatisticalSecurityParam());

    }).seq((seq, randomAdditiveMask) -> {

      DRes<SInt> b = seq.numeric().add(BigInteger.ONE.shiftLeft(maxBitLength - 1), input);
      DRes<SInt> result = seq.numeric().add(b, randomAdditiveMask.value);
      DRes<BigInteger> open = seq.numeric().open(result);
      return Pair.lazy(open, randomAdditiveMask);

    }).seq((seq, maskedInput) -> {

      BigInteger masked = maskedInput.getFirst().out();
      BigInteger maskedPrime = masked.mod(BigInteger.ONE.shiftLeft(shifts));
      RandomAdditiveMask mask = maskedInput.getSecond();

      /*
       * rBottom = r (mod 2^shifts).
       */
      final DRes<SInt> rBottom = AdvancedNumeric.using(seq)
          .bitsToInteger(mask.bits.subList(0, shifts));

      BigInteger inverse =
          BigInteger.ONE.shiftLeft(shifts).modInverse(seq.getBasicNumericContext().getModulus());

      DRes<SInt> aPrime = seq.numeric().sub(maskedPrime, rBottom);
      DRes<SInt> d = seq.numeric().sub(input, aPrime);
      return seq.numeric().mult(inverse, d);
    });
  }
}
