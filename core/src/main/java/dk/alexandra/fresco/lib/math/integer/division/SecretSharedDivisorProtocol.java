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
package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.ProtocolBuilder;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;
import java.math.BigInteger;

/**
 * <p> This protocol implements integer division where both numerator and denominator are secret
 * shared. If the denominator is a known number {@link KnownDivisorProtocol} should be used instead.
 * </p>
 *
 * <p> The protocol uses <a href= "https://en.wikipedia.org/wiki/Division_algorithm#Goldschmidt_division"
 * >Goldschmidt Division</a> (aka. the 'IBM Method'). </p>
 *
 * Its results approximate regular integer division with n bits, where n is equal to {@link
 * BasicNumericFactory#getMaxBitLength()} / 4. Just like regular integer division, this division
 * will always truncate the result instead of rounding.
 */
public class SecretSharedDivisorProtocol extends SimpleProtocolProducer implements
    DivisionProtocol {

  private SInt numerator;
  private SInt denominator;
  private SInt result;
  private OInt precision;

  private final BasicNumericFactory basicNumericFactory;
  private final RightShiftFactory rightShiftFactory;
  private final BitLengthFactory bitLengthFactory;
  private final ExponentiationFactory exponentiationFactory;
  private final ComparisonProtocolFactory comparisonFactory;

  SecretSharedDivisorProtocol(SInt numerator, SInt denominator,
      SInt result, BasicNumericFactory basicNumericFactory,
      RightShiftFactory rightShiftFactory,
      BitLengthFactory bitLengthFactory,
      ExponentiationFactory exponentiationFactory,
      ComparisonProtocolFactory comparisonFactory) {
    this.numerator = numerator;
    this.denominator = denominator;
    this.result = result;

    this.basicNumericFactory = basicNumericFactory;
    this.rightShiftFactory = rightShiftFactory;
    this.bitLengthFactory = bitLengthFactory;
    this.exponentiationFactory = exponentiationFactory;
    this.comparisonFactory = comparisonFactory;
  }

  SecretSharedDivisorProtocol(SInt numerator, SInt denominator,
      SInt result, OInt precision,
      BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory,
      BitLengthFactory bitLengthFactory,
      ExponentiationFactory exponentiationFactory,
      ComparisonProtocolFactory comparisonFactory) {

    this(numerator, denominator, result, basicNumericFactory,
        rightShiftFactory, bitLengthFactory, exponentiationFactory, comparisonFactory);
    this.precision = precision;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {

    NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);

    // Calculate maximum number of bits we can represent without overflows.
    // We lose half of the precision because we need to multiply two numbers without overflow.
    // And we lose half again because we need to be able to shift the numerator left,
    // depending on the bit length of the denominator
    int maximumBitLength = basicNumericFactory.getMaxBitLength() / 4;

    // Calculate amount of iterations that are needed to get a precise answer in all decimal bits
    int amountOfIterations = log2(maximumBitLength);

    // Convert 2 to fixed point notation with 'maximumBitLength' decimals.
    OInt two = builder.knownOInt(BigInteger.valueOf(2).shiftLeft(maximumBitLength));

    // Determine sign of numerator and denominator
    SInt numeratorSign = sign(builder, numerator);
    SInt denominatorSign = sign(builder, denominator);

    // Ensure that numerator and denominator are both positive
    numerator = builder.mult(numeratorSign, numerator);
    denominator = builder.mult(denominatorSign, denominator);

    // Determine the actual number of bits in the denominator.
    SInt denominatorBitLength = getBitLength(builder, denominator, maximumBitLength);

    // Determine the maximum number of bits we can shift the denominator left in order to gain more precision.
    SInt leftShift = builder.sub(builder.knownOInt(maximumBitLength), denominatorBitLength);

    // Left shift numerator and denominator for greater precision.
    // We're allowed to do this because shifting numerator and denominator by the same amount
    // doesn't change the outcome of the division.
    SInt leftShiftFactor = exp2(builder, leftShift, log2(maximumBitLength));
    builder.beginParScope();
    SInt n = builder.mult(numerator, leftShiftFactor);
    SInt d = builder.mult(denominator, leftShiftFactor);
    builder.endCurScope();

    // Goldschmidt iteration
    for (int i = 0; i < amountOfIterations; i++) {
      SInt f = builder.sub(two, d);
      builder.beginParScope();
      n = builder.mult(f, n);
      d = builder.mult(f, d);
      builder.endCurScope();
      builder.beginParScope();
      n = shiftRight(builder, n, maximumBitLength);
      d = shiftRight(builder, d, maximumBitLength);
      builder.endCurScope();
    }

    // Right shift to remove decimals, rounding last decimal up.
    n = builder.add(n, builder.knownOInt(1));
    n = shiftRight(builder, n, maximumBitLength);

    // Ensure that result has the correct sign
    n = builder.mult(n, numeratorSign);
    n = builder.mult(n, denominatorSign);

    builder.copy(result, n);

    // Set precision to number of correct bits in the result
    if (precision != null) {
      precision.setValue(BigInteger.valueOf(maximumBitLength));
    }

    return builder.getProtocol();
  }

  private int log2(int number) {
    return (int) Math.ceil(Math.log(number) / Math.log(2));
  }

  private SInt getBitLength(ProtocolBuilder builder, SInt input, int maximumBitLength) {
    SInt result = basicNumericFactory.getSInt();
    ProtocolProducer protocol = bitLengthFactory
        .getBitLengthProtocol(input, result, maximumBitLength);
    builder.addProtocolProducer(protocol);
    return result;
  }

  private SInt sign(NumericProtocolBuilder builder, SInt input) {
    SInt result = gte(builder, input, builder.known(0));
    result = builder.mult(builder.knownOInt(2), result);
    result = builder.sub(result, builder.knownOInt(1));
    return result;
  }

  private SInt gte(NumericProtocolBuilder builder, SInt left, SInt right) {

    // TODO: workaround for the fact that the GreaterThanProtocol actually calculated left <= right.
    SInt actualLeft = right;
    SInt actualRight = left;

    SInt result = basicNumericFactory.getSInt();
    ProtocolProducer protocol = comparisonFactory
        .getGreaterThanProtocol(actualLeft, actualRight, result, false);
    builder.addProtocolProducer(protocol);
    return result;
  }

  private SInt exp2(ProtocolBuilder builder, SInt exponent, int maxExponentLength) {
    SInt result = basicNumericFactory.getSInt();
    OInt base = basicNumericFactory.getOInt(BigInteger.valueOf(2));
    ProtocolProducer protocol = exponentiationFactory
        .getExponentiationCircuit(base, exponent, maxExponentLength, result);
    builder.addProtocolProducer(protocol);
    return result;
  }

  private SInt shiftRight(ProtocolBuilder builder, SInt input, int numberOfPositions) {
    SInt result = basicNumericFactory.getSInt();
    ProtocolProducer protocol = rightShiftFactory
        .getRepeatedRightShiftProtocol(input, numberOfPositions, result);
    builder.addProtocolProducer(protocol);
    return result;
  }
}
