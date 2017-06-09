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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.math.BigInteger;

public class RightShiftProtocolImpl implements RightShiftProtocol {

  // Input
  private SInt input;
  private SInt result;
  private SInt remainder;
  private int bitLength;
  private int securityParameter;

  // Factories
  private final BasicNumericFactory basicNumericFactory;
  private final RandomAdditiveMaskFactory randomAdditiveMaskFactory;
  private final LocalInversionFactory localInversionFactory;

  // Variables used for calculation
  private int round = 0;
  private SInt rTop, rBottom;
  private OInt mOpen;

  private NumericProtocolBuilder builder;
  private ProtocolProducer pp;

  /**
   * @param input The input.
   * @param result The input shifted one bit to the right, input >> 1.
   * @param bitLength An upper bound for the bitLength of the input.
   * @param securityParameter The security parameter .
   * Leakage of a bit with propability at most 2<sup>-<code>securityParameter</code> </sup>.
   */
  public RightShiftProtocolImpl(SInt input, SInt result, int bitLength, int securityParameter,
      BasicNumericFactory basicNumericFactory,
      RandomAdditiveMaskFactory randomAdditiveMaskFactory,
      LocalInversionFactory localInversionFactory) {

    this.input = input;
    this.result = result;
    this.remainder = null;
    this.bitLength = bitLength;
    this.securityParameter = securityParameter;

    this.basicNumericFactory = basicNumericFactory;
    this.randomAdditiveMaskFactory = randomAdditiveMaskFactory;
    this.localInversionFactory = localInversionFactory;

    this.builder = new NumericProtocolBuilder(basicNumericFactory);
  }

  /**
   * @param input The input.
   * @param result The input shifted one bit to the right, input >> 1.
   * @param remainder The least significant bit of the input (aka the bit that is thrown away in the
   * shift).
   * @param bitLength An upper bound for the bitLength of the input.
   * @param securityParameter The security parameter used in {@link RandomAdditiveMaskCircuit}.
   * Leakage of a bit with propability at most 2<sup>-<code>securityParameter</code> </sup>.
   */
  public RightShiftProtocolImpl(SInt input, SInt result, SInt remainder, int bitLength,
      int securityParameter, BasicNumericFactory basicNumericFactory,
      RandomAdditiveMaskFactory randomAdditiveMaskFactory,
      LocalInversionFactory localInversionFactory) {

    this(input, result, bitLength, securityParameter, basicNumericFactory,
        randomAdditiveMaskFactory, localInversionFactory);
    this.remainder = remainder;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {

      switch (round) {

        case 0:
          builder.reset();
          builder.beginSeqScope();

          // Load random r including binary expansion
          SInt r = basicNumericFactory.getSInt();
          SInt[] rExpansion = new SInt[bitLength];
          for (int i = 0; i < rExpansion.length; i++) {
            rExpansion[i] = basicNumericFactory.getSInt();
          }
          builder.addProtocolProducer(randomAdditiveMaskFactory
              .getRandomAdditiveMaskProtocol(securityParameter, rExpansion, r));

          // rBottom is the least significant bit of r
          rBottom = rExpansion[0];

					/*
           * Calculate rTop = (r - rBottom) * 2^{-1}. Note that r -
					 * rBottom must be even so the division in the field are
					 * actually a division in the integers.
					 */
          builder.beginParScope();

          builder.beginSeqScope();
          OInt inverseOfTwo = basicNumericFactory.getOInt();
          builder.addProtocolProducer(
              SingleProtocolProducer.wrap(
                  localInversionFactory.getLocalInversionProtocol(
                      basicNumericFactory.getOInt(BigInteger.valueOf(2)), inverseOfTwo)));
          rTop = builder.mult(inverseOfTwo, builder.sub(r, rBottom));
          builder.endCurScope();

          // mOpen = open(x + r)
          builder.beginSeqScope();
          mOpen = basicNumericFactory.getOInt();
          SInt mClosed = builder.add(input, r);
          builder.addProtocolProducer(
              SingleProtocolProducer.wrap(
                  basicNumericFactory.getOpenProtocol(mClosed, mOpen)));
          builder.endCurScope();

          builder.endCurScope();

          builder.endCurScope();
          pp = builder.getProtocol();
          break;

        case 1:
          builder.reset();

          builder.beginSeqScope();

					/*
           * 'carry' is either 0 or 1. It is 1 if and only if the
					 * addition m = x + r gave a carry from the first (least
					 * significant) bit to the second, ie. if the first bit of
					 * both x and r is 1. This happens if and only if the first
					 * bit of r is 1 and the first bit of m is 0 which in turn
					 * is equal to r_0 * (m + 1 (mod 2)).
					 */
          OInt mBottomNegated = basicNumericFactory.getOInt(mOpen.getValue()
              .add(BigInteger.ONE).mod(BigInteger.valueOf(2)));
          SInt carry = builder.mult(mBottomNegated, rBottom);

          // The carry is needed by both the calculation of the shift
          // and the remainder, but the shift and the remainder can be
          // calculated in parallel.
          builder.beginParScope();

          builder.beginSeqScope();
          // Now we calculate the shift, x >> 1 = mTop - rTop - carry
          OInt mTop = basicNumericFactory.getOInt(mOpen.getValue().shiftRight(1));
          builder.copy(result, builder.sub(builder.sub(mTop, rTop), carry));

          builder.endCurScope();

          if (remainder != null) {
            builder.beginSeqScope();
            /*
             * We also need to calculate the remainder, aka. the bit
						 * we throw away in the shift: x (mod 2) = xor(r_0, m
						 * mod 2) = r_0 + (m mod 2) - 2 (r_0 * (m mod 2)).
						 */
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
          builder.endCurScope();
          builder.endCurScope();
          pp = builder.getProtocol();

          break;

        default:
          throw new MPCException("NativeProtocol only has two rounds.");
      }
    }
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else {
      round++;
      pp = null;
    }
  }

  public Value[] getOutputValues() {
    if (remainder != null) {
      return new Value[]{result, remainder};
    } else {
      return new Value[]{result};
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return round < 2;
  }

}
