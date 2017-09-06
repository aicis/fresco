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
package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
import java.math.BigInteger;
import java.util.List;

public class LessThanOrEquals implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  public LessThanOrEquals(int bitLength, int securityParameter,
      Computation<SInt> x, Computation<SInt> y) {
    this.bitLength = bitLength;
    this.securityParameter = securityParameter;
    this.x = x;
    this.y = y;
  }

  // params etc
  private final int bitLength;
  private final int securityParameter;
  private final Computation<SInt> x;
  private final Computation<SInt> y;


  @Override
  public Computation<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final BigInteger modulus = builder.getBasicNumericContext().getModulus();

    final int bitLengthBottom = bitLength / 2;
    final int bitLengthTop = bitLength - bitLengthBottom;

    final BigInteger twoToBitLength = BigInteger.ONE.shiftLeft(this.bitLength);
    final BigInteger twoToBitLengthBottom = BigInteger.ONE.shiftLeft(bitLengthBottom);
    final BigInteger twoToNegBitLength = twoToBitLength.modInverse(modulus);

    final BigInteger one = BigInteger.ONE;

    return builder.seq((seq) -> seq.advancedNumeric().additiveMask(bitLength)
    ).pairInPar(
        (seq, mask) -> {
          List<Computation<SInt>> bits = mask.bits;
          List<Computation<SInt>> rBottomBits = bits.subList(0, bitLengthBottom);
          List<BigInteger> twoPowsBottom =
              seq.getBigIntegerHelper().getTwoPowersList(bitLengthBottom);
          return
              Pair.lazy(
                  mask.r,
                  seq.advancedNumeric().openDot(twoPowsBottom, rBottomBits)
              );
        },
        (seq, mask) -> {
          List<Computation<SInt>> rTopBits = mask.bits
              .subList(bitLengthBottom, bitLengthBottom + bitLengthTop);
          List<BigInteger> twoPowsTop =
              seq.getBigIntegerHelper().getTwoPowersList(bitLengthTop);
          AdvancedNumericBuilder innerProduct = seq.advancedNumeric();

          return innerProduct.openDot(twoPowsTop, rTopBits);
        }
    ).seq((seq, pair) -> {
      Computation<SInt> rTop = pair::getSecond;
      Computation<SInt> rBottom = pair.getFirst().getSecond();
      SInt r = pair.getFirst().getFirst();

      // construct r-values (rBar, rBottom, rTop)
      Computation<SInt> rBar;

      NumericBuilder numeric = seq.numeric();

      Computation<SInt> tmp1 = numeric.mult(twoToBitLengthBottom, rTop);
      rBar = numeric.add(tmp1, rBottom);

      // Actual work: mask and reveal 2^bitLength+x-y
      // z = 2^bitLength + x -y
      Computation<SInt> diff = numeric.sub(y, x);
      Computation<SInt> z = numeric.add(twoToBitLength, diff);

      // mO = open(z + r)
      Computation<SInt> mS = numeric.add(z, () -> r);
      Computation<BigInteger> mO = seq.numeric().open(mS);

      return () -> new Object[]{mO, rBottom, rTop, rBar, z};
    }).seq((ProtocolBuilderNumeric seq, Object[] input) -> {
      BigInteger mO = ((Computation<BigInteger>) input[0]).out();
      Computation<SInt> rBottom = (Computation<SInt>) input[1];
      Computation<SInt> rTop = (Computation<SInt>) input[2];
      Computation<SInt> rBar = (Computation<SInt>) input[3];
      Computation<SInt> z = (Computation<SInt>) input[4];

      // extract mTop and mBot
      BigInteger mMod = mO.mod(BigInteger.ONE.shiftLeft(bitLength));
      BigInteger mBar = mMod;
      BigInteger mBot = mMod.mod(BigInteger.ONE.shiftLeft(bitLengthBottom));
      BigInteger mTop = mMod.shiftRight(bitLengthBottom);

      NumericBuilder numeric = seq.numeric();
      // dif = mTop - rTop
      Computation<SInt> dif = numeric.sub(mTop, rTop);

      // eqResult <- execute eq.test
      Computation<SInt> eqResult =
          seq.comparison().compareZero(dif, bitLengthTop);
      return () -> new Object[]{eqResult, rBottom, rTop, mBot, mTop, mBar, rBar, z};
    }).seq((ProtocolBuilderNumeric seq, Object[] input) -> {
      Computation<SInt> eqResult = (Computation<SInt>) input[0];
      Computation<SInt> rBottom = (Computation<SInt>) input[1];
      Computation<SInt> rTop = (Computation<SInt>) input[2];
      BigInteger mBot = (BigInteger) input[3];
      BigInteger mTop = (BigInteger) input[4];
      BigInteger mBar = (BigInteger) input[5];
      Computation<SInt> rBar = (Computation<SInt>) input[6];
      Computation<SInt> z = (Computation<SInt>) input[7];
      // [eqResult]? BOT : TOP (for m and r) (store as mPrime,rPrime)

      //TODO rPrime and mPrime can be computed in parallel
      Computation<SInt> rPrime = seq.seq(new ConditionalSelect(eqResult, rBottom, rTop));

      NumericBuilder numeric = seq.numeric();
      Computation<SInt> negEqResult = numeric.sub(one, eqResult);

      Computation<SInt> prod1 = numeric.mult(mBot, eqResult);
      Computation<SInt> prod2 = numeric.mult(mTop, negEqResult);

      Computation<SInt> mPrime = numeric.add(prod1, prod2);

      Computation<SInt> subComparisonResult;
      if (bitLength == 2) {
        // sub comparison is of length 1: mPrime >= rPrime:
        // NOT (rPrime AND NOT mPrime)
        Computation<SInt> mPrimeNegated = numeric.sub(one, mPrime);

        Computation<SInt> rPrimeStrictlyGTmPrime = numeric.mult(mPrimeNegated, rPrime);
        subComparisonResult = numeric.sub(one, rPrimeStrictlyGTmPrime);
      } else {
        // compare the half-length inputs
        int nextBitLength = (bitLength + 1) / 2;
        subComparisonResult = seq.seq(
            new LessThanOrEquals(
                nextBitLength,
                securityParameter,
                rPrime,
                mPrime
            ));
      }
      return () -> new Object[]{subComparisonResult, mBar, rBar, z};
    }).seq((ProtocolBuilderNumeric seq, Object[] input) -> {
      Computation<SInt> subComparisonResult = (Computation<SInt>) input[0];
      BigInteger mBar = (BigInteger) input[1];
      Computation<SInt> rBar = (Computation<SInt>) input[2];
      Computation<SInt> z = (Computation<SInt>) input[3];

      NumericBuilder numeric = seq.numeric();

      // u = 1 - subComparisonResult
      Computation<SInt> u = numeric.sub(one, subComparisonResult);

      // res = z - ((m mod 2^bitLength) - (r mod 2^bitlength) + u*2^bitLength)
      Computation<SInt> reducedWithError = numeric.sub(mBar, rBar);
      Computation<SInt> additiveError = numeric.mult(twoToBitLength, u);
      Computation<SInt> reducedNoError = numeric.add(additiveError, reducedWithError);
      Computation<SInt> resUnshifted = numeric.sub(z, reducedNoError);

      // res >> 2^bitLength
      return numeric.mult(twoToNegBitLength, resUnshifted);
    });
  }

}
