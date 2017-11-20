package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Collections;

/**
 * Shifts a number to the right.
 */
public class RightShift implements Computation<RightShiftResult, ProtocolBuilderNumeric> {

  private final boolean calculateRemainders;
  // Input
  private final DRes<SInt> input;
  private final int bitLength;


  /**
   * @param bitLength An upper bound for the bitLength of the input.
   * @param input The input.
   * @param calculateRemainders true to also calculate remainder. If false remainders in result will
   *        be null.
   */
  public RightShift(int bitLength, DRes<SInt> input, boolean calculateRemainders) {
    this.bitLength = bitLength;
    this.input = input;
    this.calculateRemainders = calculateRemainders;
  }

  @Override
  public DRes<RightShiftResult> buildComputation(ProtocolBuilderNumeric sequential) {
    return sequential.seq((builder) -> {
      AdvancedNumeric additiveMaskBuilder = builder.advancedNumeric();
      return additiveMaskBuilder.additiveMask(bitLength);
    }).pairInPar((parSubSequential, randomAdditiveMask) -> {
      BigInteger two = BigInteger.valueOf(2);
      Numeric numericBuilder = parSubSequential.numeric();
      BigInteger inverseOfTwo =
          two.modInverse(parSubSequential.getBasicNumericContext().getModulus());
      DRes<SInt> rBottom = randomAdditiveMask.bits.get(0);
      DRes<SInt> sub = numericBuilder.sub(randomAdditiveMask.r, rBottom);
      DRes<SInt> rTop = numericBuilder.mult(inverseOfTwo, sub);
      return () -> new Pair<>(rBottom, rTop);
    }, (parSubSequential, randomAdditiveMask) -> {
      DRes<SInt> result = parSubSequential.numeric().add(input, () -> randomAdditiveMask.r);
      return parSubSequential.numeric().open(result);
    }).seq((round1, preprocessOutput) -> {
      BigInteger mOpen = preprocessOutput.getSecond();
      DRes<SInt> rBottom = preprocessOutput.getFirst().getFirst();
      DRes<SInt> rTop = preprocessOutput.getFirst().getSecond();
      /*
       * 'carry' is either 0 or 1. It is 1 if and only if the addition m = x + r gave a carry from
       * the first (least significant) bit to the second, ie. if the first bit of both x and r is 1.
       * This happens if and only if the first bit of r is 1 and the first bit of m is 0 which in
       * turn is equal to r_0 * (m + 1 (mod 2)).
       */
      BigInteger mBottomNegated = mOpen.add(BigInteger.ONE).mod(BigInteger.valueOf(2));
      DRes<SInt> carry = round1.numeric().mult(mBottomNegated, rBottom);
      return () -> new Pair<>(new Pair<>(rBottom, rTop), new Pair<>(carry, mOpen));
    }).pairInPar((parSubSequential, inputs) -> {
      BigInteger openShiftOnce = inputs.getSecond().getSecond().shiftRight(1);
      // Now we calculate the shift, x >> 1 = mTop - rTop - carry
      DRes<SInt> sub =
          parSubSequential.numeric().sub(openShiftOnce, inputs.getFirst().getSecond());
      return parSubSequential.numeric().sub(sub, inputs.getSecond().getFirst());
    }, (parSubSequential, inputs) -> {
      if (!calculateRemainders) {
        return null;
      } else {
        // We also need to calculate the remainder, aka. the bit
        // we throw away in the shift:
        // x (mod 2) =
        // xor(r_0, m mod 2) =
        // r_0 + (m mod 2) - 2 (r_0 * (m mod 2)).
        BigInteger mBottom = inputs.getSecond().getSecond().mod(BigInteger.valueOf(2));
        BigInteger twoMBottom = mBottom.shiftLeft(1);

        return parSubSequential.par((productAndSumBuilder) -> {
          Numeric productAndSumNumeric = productAndSumBuilder.numeric();
          DRes<SInt> rBottom = inputs.getFirst().getFirst();
          DRes<SInt> product = productAndSumNumeric.mult(twoMBottom, rBottom);
          DRes<SInt> sum = productAndSumNumeric.add(mBottom, rBottom);
          return () -> new Pair<>(product, sum);
        }).seq((finalBuilder, productAndSum) -> {
          DRes<SInt> result =
              finalBuilder.numeric().sub(productAndSum.getSecond(), productAndSum.getFirst());
          return () -> Collections.singletonList(result.out());
        });
      }
    }).seq((builder, output) -> () -> new RightShiftResult(output.getFirst(), output.getSecond()));
  }
}
