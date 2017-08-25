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
 */
package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.math.BigInteger;

/**
 * <p> This protocol implements integer division where both numerator and denominator are secret
 * shared. If the denominator is a known number {@link KnownDivisor} should be used instead. </p>
 *
 * <p> The protocol uses <a href= "https://en.wikipedia.org/wiki/Division_algorithm#Goldschmidt_division"
 * >Goldschmidt Division</a> (aka. the 'IBM Method'). </p>
 *
 * Its results approximate regular integer division with n bits, where n is equal to {@link
 * dk.alexandra.fresco.lib.field.integer.BasicNumericFactory#getMaxBitLength()} / 4. Just like
 * regular integer division, this division will always truncate the result instead of rounding.
 */
public class SecretSharedDivisor
    implements ComputationBuilder<SInt, SequentialNumericBuilder> {

  private Computation<SInt> numerator;
  private Computation<SInt> denominator;

  private BuilderFactoryNumeric builderFactory;

  SecretSharedDivisor(
      Computation<SInt> numerator,
      Computation<SInt> denominator,
      BuilderFactoryNumeric builderFactory) {
    this.numerator = numerator;
    this.denominator = denominator;
    this.builderFactory = builderFactory;
  }

  @Override
  public Computation<SInt> build(SequentialNumericBuilder builder) {

    BasicNumericFactory basicNumericFactory = builderFactory.getBasicNumericFactory();

    // Calculate maximum number of bits we can represent without overflows.
    // We lose half of the precision because we need to multiply two numbers without overflow.
    // And we lose half again because we need to be able to shift the numerator left,
    // depending on the bit length of the denominator
    int maximumBitLength = basicNumericFactory.getMaxBitLength() / 4;

    // Calculate amount of iterations that are needed to get a precise answer in all decimal bits
    int amountOfIterations = log2(maximumBitLength);

    // Convert 2 to fixed point notation with 'maximumBitLength' decimals.
    BigInteger two = BigInteger.valueOf(2).shiftLeft(maximumBitLength);

    return builder.seq(seq -> Pair.lazy(numerator, denominator)
    ).par((pair, seq) -> {
      // Determine sign of numerator and ensure positive
      Computation<SInt> numerator = pair.getFirst();
      Computation<SInt> sign = sign(seq, numerator);

      return Pair.lazy(sign, seq.numeric().mult(sign, numerator));
    }, (pair, seq) -> {
      // Determine sign of denominator and ensure positive
      Computation<SInt> denominator = pair.getSecond();
      Computation<SInt> sign = sign(seq, denominator);

      return Pair.lazy(sign, seq.numeric().mult(sign, denominator));
    }).seq((pair, seq) -> {
      Computation<SInt> denominator = pair.getSecond().getSecond();
      // Determine the actual number of bits in the denominator.
      Computation<SInt> denominatorBitLength = getBitLength(seq, denominator, maximumBitLength);
      // Determine the maximum number of bits we can shift the denominator left in order to gain more precision.
      BigInteger maxBitLength = BigInteger.valueOf(maximumBitLength);
      Computation<SInt> leftShift = seq.numeric().sub(maxBitLength, denominatorBitLength);
      Computation<SInt> leftShiftFactor = exp2(seq, leftShift, log2(maximumBitLength));
      return Pair.lazy(leftShiftFactor, pair);
      // Left shift numerator and denominator for greater precision.
      // We're allowed to do this because shifting numerator and denominator by the same amount
      // doesn't change the outcome of the division.
    }).par((pair, seq) -> {
          Computation<SInt> numeratorSign = pair.getSecond().getFirst().getFirst();
          Computation<SInt> numerator = pair.getSecond().getFirst().getSecond();
          Computation<SInt> shiftNumerator = seq.numeric().mult(pair.getFirst(), numerator);
          return Pair.lazy(numeratorSign, shiftNumerator);
        },
        (pair, seq) -> {
          Computation<SInt> denomintator = pair.getSecond().getSecond().getSecond();
          Computation<SInt> denomintatorSign = pair.getSecond().getSecond().getFirst();
          Computation<SInt> shiftedDenominator = seq.numeric().mult(pair.getFirst(), denomintator);
          return Pair.lazy(denomintatorSign, shiftedDenominator);
        }
    ).seq((pair, seq) -> {
      Computation<Pair<SInt, SInt>> iterationPair = Pair
          .lazy(pair.getFirst().getSecond().out(), pair.getSecond().getSecond().out());
      // Goldschmidt iteration
      for (int i = 0; i < amountOfIterations; i++) {
        Computation<Pair<SInt, SInt>> finalPair = iterationPair;
        iterationPair = seq.seq((innerSeq) -> {
          Pair<SInt, SInt> iteration = finalPair.out();
          Computation<SInt> n = iteration::getFirst;
          Computation<SInt> d = iteration::getSecond;
          Computation<SInt> f = innerSeq.numeric().sub(two, d);
          return Pair.lazy(f, iteration);
        }).par((innerPair, innerSeq) -> {
          NumericBuilder innerNumeric = innerSeq.numeric();
          Computation<SInt> n = () -> innerPair.getSecond().getFirst();
          Computation<SInt> f = innerPair.getFirst();
          return shiftRight(innerSeq, innerNumeric.mult(f, n), maximumBitLength);
        }, (innerPair, innerSeq) -> {
          NumericBuilder innerNumeric = innerSeq.numeric();
          Computation<SInt> d = () -> innerPair.getSecond().getSecond();
          Computation<SInt> f = innerPair.getFirst();
          return shiftRight(innerSeq, innerNumeric.mult(f, d), maximumBitLength);
        });
      }
      Computation<Pair<SInt, SInt>> iterationResult = iterationPair;
      return () -> new Pair<>(
          iterationResult.out().getFirst(),
          new Pair<>(pair.getFirst().getFirst(), pair.getSecond().getFirst()));
    }).seq((pair, seq) -> {
      Computation<SInt> n = pair::getFirst;
      Pair<Computation<SInt>, Computation<SInt>> signs = pair.getSecond();
      NumericBuilder numeric = seq.numeric();
      // Right shift to remove decimals, rounding last decimal up.
      n = numeric.add(BigInteger.ONE, n);
      n = shiftRight(seq, n, maximumBitLength);
      // Ensure that result has the correct sign
      n = numeric.mult(n, signs.getFirst());
      n = numeric.mult(n, signs.getSecond());
      return n;
    });
  }

  private int log2(int number) {
    return (int) Math.ceil(Math.log(number) / Math.log(2));
  }

  private Computation<SInt> getBitLength(SequentialNumericBuilder builder, Computation<SInt> input,
      int maximumBitLength) {
    return builder.advancedNumeric()
        .bitLength(input, maximumBitLength);
  }

  private Computation<SInt> sign(SequentialNumericBuilder builder, Computation<SInt> input) {
    Computation<SInt> result = gte(builder, input,
        builder.numeric().known(BigInteger.valueOf(0)));
    BigInteger two = BigInteger.valueOf(2);
    BigInteger one = BigInteger.valueOf(1);
    result = builder.numeric().mult(two, result);
    result = builder.numeric().sub(result, one);
    return result;
  }

  private Computation<SInt> gte(SequentialNumericBuilder builder, Computation<SInt> left,
      Computation<SInt> right) {

    // TODO: workaround for the fact that the GreaterThanProtocol actually calculated left <= right.
    Computation<SInt> actualLeft = right;
    Computation<SInt> actualRight = left;

    return builder.comparison().compareLEQ(actualLeft, actualRight);
  }

  private Computation<SInt> exp2(SequentialNumericBuilder builder, Computation<SInt> exponent,
      int maxExponentLength) {
    return builder.advancedNumeric().exp(
        BigInteger.valueOf(2),
        exponent,
        maxExponentLength
    );
  }

  private Computation<SInt> shiftRight(SequentialNumericBuilder builder, Computation<SInt> input,
      int numberOfPositions) {
    return builder.advancedNumeric()
        .rightShift(input, numberOfPositions);
  }
}
