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

import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;

/**
 * This protocol is an implementation Euclidean division (finding quotient and
 * remainder) on integers with a secret shared divedend and a known divisor. In
 * the implementation we calculate a constant <i>m</i> such that multiplication
 * with <i>m</i> will correpsond to the desired division -- just shifted a
 * number of bits to the left. To get the right result we just need to shift
 * back again.
 * 
 * The protocol does <code>maxInputLength + log(divisor)</code> shifts, and this
 * being the heaviest operation, keeping these numbers small will improve
 * performance.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class KnownDivisorProtocol extends AbstractSimpleProtocol implements DivisionProtocol {

	// Input
	private SInt dividend;
	private OInt divisor;
	private SInt result, remainder;
	private int maxInputLength;

	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;

	// Variables used for calculation
	private int divisorBitLength;
	private OInt m;

	public KnownDivisorProtocol(SInt dividend, int maxLength, OInt divisor, SInt result,
			BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory) {
		this.dividend = dividend;
		this.divisor = divisor;
		this.maxInputLength = maxLength;

		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	public KnownDivisorProtocol(SInt x, int maxLength, OInt divisor, SInt result, SInt remainder,
			BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory) {
		this(x, maxLength, divisor, result, basicNumericFactory, rightShiftFactory);
		this.remainder = remainder;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {
		SequentialProtocolProducer euclidianDivisionProtocol = new SequentialProtocolProducer();

		BigInteger dValue = divisor.getValue();
		divisorBitLength = dValue.bitLength();

		/*
		 * If we let m = floor((2^{N+l} + 2^l) / d) where d has length < l, then
		 * floor(x/d) = floor(x * m >> N+l) for all x of length < N (see Thm 4.2
		 * of "Division by Invariant Integers using Multiplication" by Granlund
		 * and Montgomery).
		 * 
		 * Actually it is enough that m*d is between 2^{N+l} and 2^{N+l} + 2^l,
		 * so it might in some cases be possible to use a smaller l than
		 * log_2(d) (the paper mentioned above gives an algorithm for doing
		 * this) which will give a better performance since we then have to do
		 * fewer shifts, but it has not yet been implemented here.
		 * 
		 * To improve performance it is desireable to keep maxLength as small as
		 * possible.
		 */
		BigInteger mValue = BigInteger.ONE.shiftLeft(maxInputLength + divisorBitLength)
				.add(BigInteger.ONE.shiftLeft(divisorBitLength)).divide(dValue);
		m = basicNumericFactory.getOInt(mValue);

		// Calculate quotient = m * x >> maxLength + l
		SInt divisionProduct = basicNumericFactory.getSInt();
		Protocol mTimesX = basicNumericFactory.getMultProtocol(m, dividend, divisionProduct);
		Protocol shift = rightShiftFactory.getRepeatedRightShiftProtocol(divisionProduct,
				maxInputLength + divisorBitLength, result);
		ProtocolProducer divisionProtocol = new SequentialProtocolProducer(mTimesX, shift);
		euclidianDivisionProtocol.append(divisionProtocol);

		if (remainder != null) {
			// Calculate remainder = x - result * divisor
			SInt product = basicNumericFactory.getSInt();
			Protocol dTimesQ = basicNumericFactory.getMultProtocol(divisor, result, product);
			Protocol subtract = basicNumericFactory.getSubtractProtocol(dividend, product, remainder);
			ProtocolProducer remainderProtocol = new SequentialProtocolProducer(dTimesQ, subtract);
			euclidianDivisionProtocol.append(remainderProtocol);
		}

		return euclidianDivisionProtocol;
	}

}
