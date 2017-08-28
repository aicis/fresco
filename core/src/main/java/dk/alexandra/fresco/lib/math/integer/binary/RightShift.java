/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumericBuilder.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Collections;

public class RightShift implements ComputationBuilder<RightShiftResult, ProtocolBuilderNumeric> {

  private final boolean calculateRemainders;
  // Input
  private final Computation<SInt> input;
  private final int bitLength;


  /**
   * @param bitLength An upper bound for the bitLength of the input.
   * @param input The input.
   * @param calculateRemainders true to also calculate remainder. If false remainders in result will
   *        be null.
   */
  public RightShift(int bitLength, Computation<SInt> input, boolean calculateRemainders) {
    this.bitLength = bitLength;
    this.input = input;
    this.calculateRemainders = calculateRemainders;
  }

  @Override
  public Computation<RightShiftResult> buildComputation(ProtocolBuilderNumeric sequential) {
    return sequential.seq((builder) -> {
      AdvancedNumericBuilder additiveMaskBuilder = builder.advancedNumeric();
      return additiveMaskBuilder.additiveMask(bitLength);
    }).par((parSubSequential, randomAdditiveMask) -> {
      BigInteger two = BigInteger.valueOf(2);
      NumericBuilder numericBuilder = parSubSequential.numeric();
      BigInteger inverseOfTwo =
          two.modInverse(parSubSequential.getBasicNumericFactory().getModulus());
      Computation<SInt> rBottom = randomAdditiveMask.bits.get(0);
      Computation<SInt> sub = numericBuilder.sub(randomAdditiveMask.r, rBottom);
      Computation<SInt> rTop = numericBuilder.mult(inverseOfTwo, sub);
      return () -> new Pair<>(rBottom, rTop);
    }, (parSubSequential, randomAdditiveMask) -> {
      Computation<SInt> result = parSubSequential.numeric().add(input, () -> randomAdditiveMask.r);
      return parSubSequential.numeric().open(result);
    }).seq((round1, preprocessOutput) -> {
      BigInteger mOpen = preprocessOutput.getSecond();
      Computation<SInt> rBottom = preprocessOutput.getFirst().getFirst();
      Computation<SInt> rTop = preprocessOutput.getFirst().getSecond();
      /*
       * 'carry' is either 0 or 1. It is 1 if and only if the addition m = x + r gave a carry from
       * the first (least significant) bit to the second, ie. if the first bit of both x and r is 1.
       * This happens if and only if the first bit of r is 1 and the first bit of m is 0 which in
       * turn is equal to r_0 * (m + 1 (mod 2)).
       */
      BigInteger mBottomNegated = mOpen.add(BigInteger.ONE).mod(BigInteger.valueOf(2));
      Computation<SInt> carry = round1.numeric().mult(mBottomNegated, rBottom);
      return () -> new Pair<>(new Pair<>(rBottom, rTop), new Pair<>(carry, mOpen));
    }).par((parSubSequential, inputs) -> {
      BigInteger openShiftOnce = inputs.getSecond().getSecond().shiftRight(1);
      // Now we calculate the shift, x >> 1 = mTop - rTop - carry
      Computation<SInt> sub =
          parSubSequential.numeric().sub(openShiftOnce, inputs.getFirst().getSecond());
      return parSubSequential.numeric().sub(sub, inputs.getSecond().getFirst());
    }, (parSubSequential, inputs) -> {
      if (!calculateRemainders) {
        return () -> null;
      } else {
        // We also need to calculate the remainder, aka. the bit
        // we throw away in the shift:
        // x (mod 2) =
        // xor(r_0, m mod 2) =
        // r_0 + (m mod 2) - 2 (r_0 * (m mod 2)).
        BigInteger mBottom = inputs.getSecond().getSecond().mod(BigInteger.valueOf(2));
        BigInteger twoMBottom = mBottom.shiftLeft(1);

        return parSubSequential.par((productAndSumBuilder) -> {
          NumericBuilder productAndSumNumeric = productAndSumBuilder.numeric();
          Computation<SInt> rBottom = inputs.getFirst().getFirst();
          Computation<SInt> product = productAndSumNumeric.mult(twoMBottom, rBottom);
          Computation<SInt> sum = productAndSumNumeric.add(mBottom, rBottom);
          return () -> new Pair<>(product, sum);
        }).seq((finalBuilder, productAndSum) -> {
          Computation<SInt> result =
              finalBuilder.numeric().sub(productAndSum.getSecond(), productAndSum.getFirst());
          return () -> Collections.singletonList(result.out());
        });
      }
    }).seq((builder, output) -> () -> new RightShiftResult(output.getFirst(), output.getSecond()));
  }
}
