
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Shifts a number to the right.
 */
public class RightShift implements Computation<RightShiftResult, ProtocolBuilderNumeric> {

  private final boolean calculateRemainder;
  // Input
  private final DRes<SInt> input;
  private final int bitLength;
  private final int shifts;

  /**
   * @param bitLength An upper bound for the bitLength of the input.
   * @param input The input.
   * @param calculateRemainder true to also calculate remainder, aka input mod 2^shifts. If
   *     false remainder in result will be null.
   */
  public RightShift(int bitLength, DRes<SInt> input, boolean calculateRemainder) {
    this(bitLength, input, 1, calculateRemainder);
  }

  public RightShift(int bitLength, DRes<SInt> input, int shifts, boolean calculateRemainder) {

    if (shifts < 0) {
      throw new IllegalArgumentException();
    }

    this.bitLength = bitLength;
    this.input = input;
    this.shifts = shifts;
    this.calculateRemainder = calculateRemainder;
  }

  @Override
  public DRes<RightShiftResult> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) -> {
      /*
       * Generate random additive mask of the same length as the input + some extra to avoid
       * leakage.
       */
      AdvancedNumeric additiveMaskBuilder = seq.advancedNumeric();
      DRes<RandomAdditiveMask> randomAdditiveMask = additiveMaskBuilder.additiveMask(bitLength);
      DRes<SInt> result = seq.numeric().add(input, () -> randomAdditiveMask.out().random);
      DRes<BigInteger> open = seq.numeric().open(result);
      return () -> new Pair<>(open, randomAdditiveMask.out());
    }).seq((seq, maskedInput) -> {
      BigInteger masked = maskedInput.getFirst().out();
      RandomAdditiveMask mask = maskedInput.getSecond();

      /*
       * m = r + input, so there is a carry from the addition of the first (least significant) bit
       * if and only if m_0 = 0 and r_0 = 1.
       */
      boolean mi = masked.testBit(0);
      DRes<SInt> ri = mask.bits.get(0);
      DRes<SInt> currentCarry = mi ? seq.numeric().known(BigInteger.ZERO) : ri;

      for (int i = 1; i < shifts; i++) {
        mi = masked.testBit(i);
        ri = mask.bits.get(i);
        DRes<SInt> x = seq.numeric().mult(currentCarry, ri);

        /*
         * One of the following must be true for there to be a carry from the addition of the i'th
         * bits of the input and the mask, r:
         *
         * 1) If the i'th bit of the masked input is set, both the i'th bit of r AND the carry from
         * the previous bit should be set.
         *
         * 2) If the i'th bit of the masked input it not set, either the i'th bit of r is set OR
         * there was a carry from the previous adding the (i-1)'th bits.
         */
        currentCarry = mi ? x : seq.numeric().sub(seq.numeric().add(currentCarry, ri), x);
      }
      final DRes<SInt> carry = currentCarry;

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

      /*
       * The naive result to return would be mShifted - rShifted, but this is not equal to the
       * shifted input if there was a carry form the addition of the input and r on the most
       * significant bit that was removed by the shift.
       */
      DRes<SInt> naiveResult = seq.numeric().sub(mShifted, rShifted);
      DRes<SInt> result = seq.numeric().sub(naiveResult, carry);

      if (!calculateRemainder) {
        return () -> new RightShiftResult(result.out(), null);
      } else {
        BigInteger twoPow = BigInteger.ONE.shiftLeft(shifts);
        DRes<SInt> leftShifted = seq.numeric().mult(twoPow, result);
        DRes<SInt> remainder = seq.numeric().sub(input, leftShifted);
        return () -> new RightShiftResult(result.out(), remainder.out());
      }
    });
  }
}
