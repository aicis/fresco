/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.RightShiftBuilder.RightShiftResult;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.builder.RandomAdditiveMaskBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class RightShiftProtocol4<SIntT extends SInt>
    implements Function<SequentialProtocolBuilder<SIntT>, Computation<RightShiftResult<SIntT>>> {

  private final boolean calculateRemainders;
  // Input
  private final Computation<SIntT> input;
  private final int bitLength;


  /**
   * @param bitLength An upper bound for the bitLength of the input.
   * @param input The input.
   * @param calculateRemainders true to also calculate remainder. If false remainders in result will
   * be null.
   */
  public RightShiftProtocol4(
      int bitLength,
      Computation<SIntT> input,
      boolean calculateRemainders) {
    this.bitLength = bitLength;
    this.input = input;
    this.calculateRemainders = calculateRemainders;
  }

  @Override
  public Computation<RightShiftResult<SIntT>> apply(SequentialProtocolBuilder<SIntT> sequential) {
    return sequential.seq((builder) -> {
      RandomAdditiveMaskBuilder<SIntT> additiveMaskBuilder = builder
          .createAdditiveMaskBuilder();
      return additiveMaskBuilder.additiveMask(bitLength);
    }).par((randomAdditiveMask, parallel) -> {
      Computation<Pair<Computation<SIntT>, Computation<SIntT>>> topAndBottom =
          parallel.createSequentialSubFactoryReturning((parSubSequential) -> {
            OInt two = parSubSequential.getOIntFactory().getOInt(BigInteger.valueOf(2));
            NumericBuilder<SIntT> numericBuilder = parSubSequential.numeric();
            Computation<? extends OInt> inverseOfTwo = numericBuilder.invert(two);
            Computation<SIntT> rBottom = () -> randomAdditiveMask.bits.get(0);
            Computation<SIntT> sub = numericBuilder
                .sub(() -> randomAdditiveMask.r, rBottom);
            Computation<SIntT> rTop = numericBuilder.mult(inverseOfTwo.out(), sub);
            return () -> new Pair<>(rBottom, rTop);
          });
      Computation<OInt> maskOpen =
          parallel.createSequentialSubFactoryReturning((parSubSequential) -> {
            NumericBuilder<SIntT> numericBuilder = parSubSequential.numeric();
            Computation<SIntT> result = numericBuilder
                .add(input, () -> randomAdditiveMask.r);
            return parSubSequential.createOpenBuilder().open(result);
          });
      return () -> new Pair<>(topAndBottom.out(), maskOpen.out());
    }).seq((preprocessOutput, round1) -> {
      OInt mOpen = preprocessOutput.getSecond();
      Computation<SIntT> rBottom = preprocessOutput.getFirst().getFirst();
      Computation<SIntT> rTop = preprocessOutput.getFirst().getSecond();
          /*
           * 'carry' is either 0 or 1. It is 1 if and only if the
					 * addition m = x + r gave a carry from the first (least
					 * significant) bit to the second, ie. if the first bit of
					 * both x and r is 1. This happens if and only if the first
					 * bit of r is 1 and the first bit of m is 0 which in turn
					 * is equal to r_0 * (m + 1 (mod 2)).
					 */
      NumericBuilder<SIntT> numericBuilder = round1.numeric();
      OInt mBottomNegated = round1.getOIntFactory().getOInt(mOpen.getValue()
          .add(BigInteger.ONE).mod(BigInteger.valueOf(2)));
      Computation<SIntT> carry = numericBuilder.mult(mBottomNegated, rBottom);
      return round1.par((parallel) -> {
            // The carry is needed by both the calculation of the shift
            // and the remainder, but the shift and the remainder can be
            // calculated in parallel.
            Computation<SIntT> shifted =
                parallel.createSequentialSubFactoryReturning((parSubSequential) -> {
                  NumericBuilder<SIntT> parSubSeqNumericBuilder = parSubSequential
                      .numeric();
                  BigInteger openShiftOnce = mOpen.getValue().shiftRight(1);
                  OInt mTop = parSubSequential.getOIntFactory().getOInt(openShiftOnce);
                  // Now we calculate the shift, x >> 1 = mTop - rTop - carry
                  Computation<SIntT> sub = parSubSeqNumericBuilder.sub(mTop, rTop);
                  return parSubSeqNumericBuilder.sub(sub, carry);
                });
            List<Computation<SIntT>> remainders;
            if (calculateRemainders) {
              // We also need to calculate the remainder, aka. the bit
              // we throw away in the shift:
              //   x (mod 2) =
              //     xor(r_0, m mod 2) =
              //     r_0 + (m mod 2) - 2 (r_0 * (m mod 2)).
              remainders =
                  Collections.singletonList(
                      parallel.createSequentialSubFactoryReturning((parSubSequential) -> {
                        OIntFactory oIntFactory = parSubSequential.getOIntFactory();

                        OInt mBottom = oIntFactory.getOInt(
                            mOpen.getValue().mod(BigInteger.valueOf(2))
                        );
                        OInt twoMBottom = oIntFactory.getOInt(
                            mBottom.getValue().shiftLeft(1)
                        );

                        Computation<Pair<Computation<SIntT>, Computation<SIntT>>> productAndSum =
                            parSubSequential.createParallelSubFactoryReturning(
                                (productAndSumBuilder) -> {
                                  NumericBuilder<SIntT> productAndSumNumeric =
                                      productAndSumBuilder.numeric();
                                  Computation<SIntT> product = productAndSumNumeric
                                      .mult(twoMBottom, rBottom);
                                  Computation<SIntT> sum = productAndSumNumeric
                                      .add(mBottom, rBottom);
                                  return () -> new Pair<>(product, sum);
                                }
                            );

                        return parSubSequential
                            .createSequentialSubFactoryReturning((finalBuilder) -> {
                              NumericBuilder<SIntT> finalNumeric =
                                  finalBuilder.numeric();
                              return finalNumeric.sub(
                                  productAndSum.out().getSecond(),
                                  productAndSum.out().getFirst());
                            });
                      }));
            } else {
              remainders = null;
            }
            return () -> new RightShiftResult<>(shifted, remainders);
          }
      );
    });
  }
}