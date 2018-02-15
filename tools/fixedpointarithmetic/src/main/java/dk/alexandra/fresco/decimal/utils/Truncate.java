package dk.alexandra.fresco.decimal.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Returns a number which is approximately the input shifted a number of positions to the right.
 */
public class Truncate implements Computation<SInt, ProtocolBuilderNumeric> {

  // Input
  private final DRes<SInt> input;
  private final int bitLength;
  private final int shifts;

  public Truncate(int bitLength, DRes<SInt> input, int shifts) {

    if (shifts < 0) {
      throw new IllegalArgumentException();
    }

    this.bitLength = bitLength;
    this.input = input;
    this.shifts = shifts;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric sequential) {
    return sequential.seq((builder) -> {
      /*
       * Generate random additive mask of the same length as the input + some extra to avoid
       * leakage.
       */
      AdvancedNumeric additiveMaskBuilder = builder.advancedNumeric();
      DRes<RandomAdditiveMask> mask = additiveMaskBuilder.additiveMask(bitLength);
      return mask;
    }).seq((parSubSequential, randomAdditiveMask) -> {
      DRes<SInt> result = parSubSequential.numeric().add(input, () -> randomAdditiveMask.random);
      DRes<BigInteger> open = parSubSequential.numeric().open(result);
      return () -> new Pair<>(open, randomAdditiveMask);
    }).seq((seq, maskedInput) -> {
      BigInteger masked = maskedInput.getFirst().out();
      RandomAdditiveMask mask = maskedInput.getSecond();

      /*
       * rBottom = r (mod 2^shifts).
       */
      final DRes<SInt> rBottom = seq.advancedNumeric().innerProductWithPublicPart(
          seq.getBigIntegerHelper().getTwoPowersList(shifts), mask.bits);

      BigInteger inverse =
          BigInteger.ONE.shiftLeft(shifts).modInverse(seq.getBasicNumericContext().getModulus());
      DRes<SInt> rTop = seq.numeric().sub(mask.random, rBottom);

      /*
       * rTop is r with the last shifts bits set to zero, and it is hence divisible by 2^shifts, so
       * multiplying with the inverse in the field corresponds to shifting.
       */
      DRes<SInt> rShifted = seq.numeric().mult(inverse, rTop);
      BigInteger mShifted = masked.shiftRight(shifts);
      DRes<SInt> result = seq.numeric().sub(mShifted, rShifted);
      return result;
    });
  }
}
