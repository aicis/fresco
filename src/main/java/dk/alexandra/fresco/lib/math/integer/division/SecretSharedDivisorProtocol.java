/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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

import java.math.BigInteger;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;

/**
 * <p>
 * This protocol approximates division where both dividend and divisor is secret
 * shared. If the divisor is a known number {@link KnownDivisorProtocol} should
 * be used instead.
 * </p>
 * 
 * <p>
 * The protocol uses <a href=
 * "https://en.wikipedia.org/wiki/Division_algorithm#Goldschmidt_division"
 * >Goldschmidt Division</a> (aka. the 'IBM Method'). Here we calculate <i>n / d
 * = n / (1-x) = n(1+x) / (1-x<sup>2</sup>) = n(1+x)(1+x<sup>2</sup>) /
 * (1-x<sup>4</sup>) = n(1+x)(1+x<sup>2</sup>) ...
 * (1+x<sup>2<sup>p-1</sup></sup>) / (1-x<sup>2<sup>p</sup></sup>),</i> where
 * <i>x = 1-d</i>, repeatedly exploiting that <i>(1+x)(1-x) =
 * 1-x<sup>2</sup></i>, and note that if we scale <i>n</i> and <i>d</i> such
 * that <i>1/2 < d < 1</i> then the denominator will converge to <i>1</i> as
 * <i>p</i> increases, se we can use the numerator as an approximation for <i>n
 * / d</i>. Now, to keep everything integer we need to shift all numbers to the
 * left and then shift back when we are done.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SecretSharedDivisorProtocol extends AbstractSimpleProtocol implements DivisionProtocol {

	// Input
	private SInt dividend;
	private int maxDividendLength;
	private SInt divisor;
	private int maxDivisorLength;
	private SInt result;
	private OInt precision;

	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;
	private final BitLengthFactory bitLengthFactory;
	private final ExponentiationFactory exponentiationFactory;

	public SecretSharedDivisorProtocol(SInt dividend, int maxDividendLength, SInt divisor,
			int maxDivisorLength, SInt result, BasicNumericFactory basicNumericFactory,
			RightShiftFactory rightShiftFactory,
			BitLengthFactory bitLengthFactory,
			ExponentiationFactory exponentiationFactory) {
		this.dividend = dividend;
		this.maxDividendLength = maxDividendLength;
		this.divisor = divisor;
		this.maxDivisorLength = maxDivisorLength;

		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
		this.bitLengthFactory = bitLengthFactory;
		this.exponentiationFactory = exponentiationFactory;
	}

	public SecretSharedDivisorProtocol(SInt dividend, int maxDividendLength, SInt divisor,
			int maxDivisorLength, SInt result, OInt precision,
			BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory,
			BitLengthFactory bitLengthFactory,
			ExponentiationFactory exponentiationFactory) {
		
		this(dividend, maxDividendLength, divisor, maxDivisorLength, result, basicNumericFactory,
				rightShiftFactory, bitLengthFactory, exponentiationFactory);

		this.precision = precision;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {

		NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);

		/*
		 * 2^{(2^p - 1) * M + p} * dividend has to be representable, so the
		 * logarithm of this has to be smaller than
		 * basicNumericFactory.getMaxBitLength(). This is the case when p is
		 * defined as follows:
		 * 
		 * Note that we are comparing the logarithm with the bitlength, so we
		 * may add one to the logarithm.
		 */
		int precision = log2((basicNumericFactory.getMaxBitLength() - maxDivisorLength)
				/ maxDividendLength) + 1; 

		if (precision < 2) {
			throw new MPCException("Division protocol can only guarantee " + precision + " bits of precision. This is likely because the inputs are to large.");
		}
		
		builder.beginSeqScope();

		/*
		 * Calculate 2^M, 2^{2M}, ..., 2^{2^{p-1} M} where M = maxDivisorLength
		 * and p = precision.
		 */
		OInt[] twoPowers = new OInt[precision];
		twoPowers[0] = basicNumericFactory.getOInt(BigInteger.valueOf(1)
				.shiftLeft(maxDivisorLength));
		for (int i = 1; i < precision; i++) {
			BigInteger previous = twoPowers[i - 1].getValue();
			twoPowers[i] = basicNumericFactory.getOInt(previous.multiply(previous));
		}

		/*
		 * Find the bitlength m of the divisor
		 */
		SInt mostSignificantBit = builder.getSInt();
		builder.addProtocolProducer(bitLengthFactory.getBitLengthProtocol(divisor,
				mostSignificantBit, maxDivisorLength));
		SInt boundDifference = builder.sub(
				basicNumericFactory.getOInt(BigInteger.valueOf(maxDivisorLength)),
				mostSignificantBit);
		SInt boundDifferencePower = builder.getSInt();
		builder.addProtocolProducer(exponentiationFactory.getExponentiationCircuit(
				basicNumericFactory.getOInt(BigInteger.valueOf(2)), boundDifference,
				log2(maxDivisorLength), boundDifferencePower));

		SInt y = builder.sub(twoPowers[0], builder.mult(boundDifferencePower, divisor));

		/*
		 * Calculate y, y^2, ..., y^{2{p-1}}.
		 */
		SInt[] yPowers = new SInt[precision];
		yPowers[0] = y;
		for (int i = 1; i < precision; i++) {
			yPowers[i] = builder.mult(yPowers[i - 1], yPowers[i - 1]);
		}

		/*
		 * Calculate x(2^M + y)(2^{2M} + y^2) ... (2^{2^{p-1} M} y^{2^{p-1}}).
		 */
		builder.beginParScope();
		SInt[] factors = new SInt[precision + 1];
		factors[0] = dividend;
		for (int i = 0; i < precision; i++) {
			factors[i + 1] = builder.add(yPowers[i], twoPowers[i]);
		}
		builder.endCurScope();

		builder.beginSeqScope();
		SInt numerator = builder.mult(factors);

		/*
		 * To get the right result we need to shift back (2^p - 1) M + m steps.
		 * We do this by shifting (2^p - 2) M bits. Then we multiply by 2^{M -
		 * m} and finally shift 2M bits.
		 */
		int shifts = ((1 << precision) - 2) * maxDivisorLength;
		SInt tmp = builder.getSInt();
		builder.addProtocolProducer(rightShiftFactory.getRepeatedRightShiftProtocol(numerator, shifts,
				tmp));
		SInt tmp2 = builder.mult(tmp, boundDifferencePower);
		builder.addProtocolProducer(rightShiftFactory.getRepeatedRightShiftProtocol(tmp2,
				2 * maxDivisorLength, result));
		builder.endCurScope();

		builder.endCurScope();

		/*
		 * We get at least 2^precision bits of precision.
		 */
		if (this.precision != null) {
			this.precision.setValue(BigInteger.valueOf(2).pow(precision));
		}

		return builder.getProtocol();
	}

	/**
	 * Calculate the base-2 logarithm of <i>x</i>, <i>log<sub>2</sub>(x)</i>.
	 * 
	 * @param x
	 * @return
	 */
	private static int log2(int x) {
		return (int) (Math.log(x) / Math.log(2));
	}

}
