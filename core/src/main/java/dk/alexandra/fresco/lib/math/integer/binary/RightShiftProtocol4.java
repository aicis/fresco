/*******************************************************************************
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

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.RandomAdditiveMaskBuilder;
import dk.alexandra.fresco.framework.builder.RandomAdditiveMaskBuilder.RandomAdditiveMask;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import java.math.BigInteger;

public class RightShiftProtocol4<SIntT extends SInt> extends
    SimpleProtocolProducer implements Computation<SIntT> {

  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  // Input
  private Computation<SIntT> input;
  private Computation<SIntT> result;
  private SIntT remainder;
  private int bitLength;

  // Variables used for calculation
  private int round = 0;
  private Computation<SIntT> rTop, rBottom;
  private Computation<RandomAdditiveMask<SIntT>> mask;
  private Computation<OInt> mOpen;
  private Computation<SIntT> carry;

  /**
   * @param bitLength An upper bound for the bitLength of the input.
   * @param input The input.
   */
  public RightShiftProtocol4(
      BuilderFactoryNumeric<SIntT> factoryNumeric,
      int bitLength,
      Computation<SIntT> input) {
    this.factoryNumeric = factoryNumeric;
    this.bitLength = bitLength;
    this.input = input;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    return ProtocolBuilder.createRoot(factoryNumeric, (sequential) -> {
      sequential.createSequentialSubFactory((round0) -> {
        round0.createSequentialSubFactory((builder) -> {
          RandomAdditiveMaskBuilder<SIntT> additiveMaskBuilder = builder
              .createAdditiveMaskBuilder();
          mask = additiveMaskBuilder.additiveMask(bitLength);
        });
        round0.createParallelSubFactory((parallel) -> {
          parallel.createSequentialSubFactory((parSubSequential) -> {
            OInt two = parSubSequential.getOIntFactory().getOInt(BigInteger.valueOf(2));
            NumericBuilder<SIntT> numericBuilder = parSubSequential.createNumericBuilder();
            Computation<? extends OInt> inverseOfTwo = numericBuilder.invert(two);
            RandomAdditiveMask<SIntT> randomAdditiveMask = mask.out();
            rBottom = () -> randomAdditiveMask.bits.get(0);
            Computation<SIntT> sub = numericBuilder.sub(() -> randomAdditiveMask.r, rBottom);
            rTop = numericBuilder.mult(inverseOfTwo.out(), sub);
          });
          parallel.createSequentialSubFactory((parSubSequential) -> {
            NumericBuilder<SIntT> numericBuilder = parSubSequential.createNumericBuilder();
            Computation<SIntT> result = numericBuilder.add(input, () -> mask.out().r);
            mOpen = parSubSequential.createOpenBuilder().open(result);
          });
        });
      });
      sequential.createSequentialSubFactory((round1) -> {
          /*
           * 'carry' is either 0 or 1. It is 1 if and only if the
					 * addition m = x + r gave a carry from the first (least
					 * significant) bit to the second, ie. if the first bit of
					 * both x and r is 1. This happens if and only if the first
					 * bit of r is 1 and the first bit of m is 0 which in turn
					 * is equal to r_0 * (m + 1 (mod 2)).
					 */
        NumericBuilder<SIntT> numericBuilder = round1.createNumericBuilder();
        OInt two = round1.getOIntFactory().getOInt(BigInteger.valueOf(2));
        OInt mBottomNegated = round1.getOIntFactory().getOInt(mOpen.out().getValue()
            .add(BigInteger.ONE).mod(BigInteger.valueOf(2)));
        carry = numericBuilder.mult(mBottomNegated, rBottom);
        round1.createParallelSubFactory((parallel) -> {
          // The carry is needed by both the calculation of the shift
          // and the remainder, but the shift and the remainder can be
          // calculated in parallel.
          parallel.createSequentialSubFactory((parSubSequential) -> {
            NumericBuilder<SIntT> parSubSeqNumericBuilder = parSubSequential.createNumericBuilder();
            BigInteger openShiftOnce = mOpen.out().getValue().shiftRight(1);
            OInt mTop = parSubSequential.getOIntFactory().getOInt(openShiftOnce);
            // Now we calculate the shift, x >> 1 = mTop - rTop - carry
            Computation<SIntT> sub = parSubSeqNumericBuilder.sub(mTop, rTop);
            result = parSubSeqNumericBuilder.sub(sub, carry);
          });
        });
        // Consider remainder
/*
        if (remainder != null) {
          builder.beginSeqScope();
            */
/*
             * We also need to calculate the remainder, aka. the bit
						 * we throw away in the shift: x (mod 2) = xor(r_0, m
						 * mod 2) = r_0 + (m mod 2) - 2 (r_0 * (m mod 2)).
						 *//*

          OInt mBottom = basicNumericFactory.getOInt(mOpen.getValue().mod(
              BigInteger.valueOf(2)));
          OInt twoMBottom = basicNumericFactory.getOInt(mBottom.getValue().shiftLeft(
              1));

          builder.beginParScope();
          SInt product = builder.mult(twoMBottom, rBottom);
          SInt sum = builder.add(rBottom, mBottom);
          builder.endCurScope();

          builder.copy(remainder, builder.sub(sum, product));

          builder.endCurScope();
        }
*/
      });
    }).build();
  }

  @Override
  public SIntT out() {
    return result.out();
  }
}
