package dk.alexandra.fresco.lib.math.integer.binary;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;

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
   * @param bitLength
   *          An upper bound for the bitLength of the input.
   * @param input
   *          The input.
   * @param calculateRemainder
   *          true to also calculate remainder, aka input mod 2^shifts. If false
   *          remainder in result will be null.
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
  public DRes<RightShiftResult> buildComputation(ProtocolBuilderNumeric sequential) {
    return sequential.seq((builder) -> {
      /*
       * Generate random additive mask of the same length as the input + some
       * extra to avoid leakage.
       */
      AdvancedNumeric additiveMaskBuilder = builder.advancedNumeric();
      return additiveMaskBuilder.additiveMask(bitLength);
    }).seq((parSubSequential, randomAdditiveMask) -> {
      DRes<SInt> result = parSubSequential.numeric().add(input, () -> randomAdditiveMask.r);
      DRes<BigInteger> open = parSubSequential.numeric().open(result);
      return () -> new Pair<>(open, randomAdditiveMask);
    }).seq((round1, maskedInput) -> {
      BigInteger masked = maskedInput.getFirst().out();
      RandomAdditiveMask mask = maskedInput.getSecond();

      /*
       * m = r + input, so there is a carry from the addition of the first
       * (least significant) bit if and only if m_0 = 0 and r_0 = 1.
       */
      boolean mi = masked.testBit(0);
      DRes<SInt> ri = mask.bits.get(0);
      DRes<SInt> currentCarry = mi ? round1.numeric().known(BigInteger.ZERO) : ri;
      
      for (int i = 1; i < shifts; i++) {
        mi = masked.testBit(i);
        ri = mask.bits.get(i);
        DRes<SInt> x = round1.numeric().mult(currentCarry, ri);

        /*
         * One of the following must be true for there to be a carry from the
         * addition of the i'th bits of the input and the mask, r:
         * 
         * 1) If the i'th bit of the masked input is set, both the i'th bit of r
         * AND the carry from the previous bit should be set.
         * 
         * 2) If the i'th bit of the masked input it not set, either the i'th
         * bit of r is set OR there was a carry from the previous adding the
         * (i-1)'th bits.
         */
        currentCarry = mi ? x : round1.numeric().sub(round1.numeric().add(currentCarry, ri), x);
      }
      final DRes<SInt> carry = currentCarry;

      /*
       * rBottom = r (mod 2^shifts).
       */
      final DRes<SInt> rBottom = round1.advancedNumeric().innerProductWithPublicPart(
          round1.getBigIntegerHelper().getTwoPowersList(shifts), mask.bits);

      BigInteger inv = BigInteger.ONE.shiftLeft(shifts)
          .modInverse(round1.getBasicNumericContext().getModulus());
      DRes<SInt> rTop = round1.numeric().sub(mask.r, rBottom);

      /*
       * rTop is now divisible by 2^shifts, so multiplying with inv corresponds
       * to doing shifts right shifts.
       */
      DRes<SInt> rShifted = round1.numeric().mult(inv, rTop);

      return () -> new Pair<>(masked, new Pair<>(carry, rShifted));
    }).seq((round2, inputs) -> {

      BigInteger mShifted = inputs.getFirst().shiftRight(shifts);

      // Calculate the naive result, x >> shifts = mShifted - rShifted
      DRes<SInt> shifted = round2.numeric().sub(mShifted, inputs.getSecond().getSecond());

      // ...but if there was a carry from the bits just below the shift, we need
      // to subtract that
      DRes<SInt> result = round2.numeric().sub(shifted, inputs.getSecond().getFirst());

      return result;
    }).seq((round3, result) -> {

      if (!calculateRemainder) {
        return () -> new RightShiftResult(result, null);
      } else {
        BigInteger twoPow = BigInteger.ONE.shiftLeft(shifts);
        DRes<SInt> leftShifted = round3.numeric().mult(twoPow, result);
        DRes<SInt> remainder = round3.numeric().sub(input, leftShifted);
        return () -> new RightShiftResult(result, remainder.out());
      }
    });
  }
}
