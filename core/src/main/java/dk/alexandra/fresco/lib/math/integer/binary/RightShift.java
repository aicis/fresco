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
    }).pairInPar((parSubSequential, randomAdditiveMask) -> {
      return () -> randomAdditiveMask;
    } , (parSubSequential, randomAdditiveMask) -> {
      DRes<SInt> result = parSubSequential.numeric().add(input, () -> randomAdditiveMask.r);
      return parSubSequential.numeric().open(result);
    }).seq((round1, preprocessOutput) -> {
      BigInteger m = preprocessOutput.getSecond();
      RandomAdditiveMask randomAdditiveMask = preprocessOutput.getFirst();

      /*
       * m = r + input, so there is a carry from the addition of the first
       * (least significant) bit if and only if m_i = 0 and r_i = 1.
       */
      BigInteger mi = m.testBit(0) ? BigInteger.ONE : BigInteger.ZERO;
      DRes<SInt> ri = randomAdditiveMask.bits.get(0);
      DRes<SInt> currentCarry = round1.numeric().mult(mi.xor(BigInteger.ONE), ri);

      for (int i = 1; i < shifts; i++) {
        mi = m.testBit(i) ? BigInteger.ONE : BigInteger.ZERO;
        ri = randomAdditiveMask.bits.get(i);
        DRes<SInt> x = round1.numeric().mult(currentCarry, ri);

        /*
         * If the i'th bit of the mask is zero, either the i'th bit of r OR there's
         * carry from the previous bit.
         */
        DRes<SInt> miZeroAndCarry = round1.numeric().mult(mi.xor(BigInteger.ONE),
            round1.numeric().sub(round1.numeric().add(currentCarry, ri), x));

        /*
         * If the i'th bit of the mask is one, both the i'th bit of r AND
         * there's carry from the previous bit.
         */
        DRes<SInt> miOneAndCarry = round1.numeric().mult(mi, x);
        currentCarry = round1.numeric().add(miZeroAndCarry, miOneAndCarry);
      }
      final DRes<SInt> carry = currentCarry;

      /*
       * rBottom = r (mod 2^shifts).
       */
      final DRes<SInt> rBottom = round1.advancedNumeric().innerProductWithPublicPart(
          round1.getBigIntegerHelper().getTwoPowersList(shifts), randomAdditiveMask.bits);

      BigInteger inv = BigInteger.ONE.shiftLeft(shifts)
          .modInverse(round1.getBasicNumericContext().getModulus());
      DRes<SInt> rTop = round1.numeric().sub(randomAdditiveMask.r, rBottom);

      /*
       * rTop is now divisible by 2^shifts, so multiplying with inv corresponds
       * to doing shifts right shifts.
       */
      DRes<SInt> rShifted = round1.numeric().mult(inv, rTop);

      return () -> new Pair<>(m, new Pair<>(carry, rShifted));
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
