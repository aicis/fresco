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
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;

public class SecretSharedDivisorProtocol extends AbstractSimpleProtocol implements
		DivisionProtocol {

	// Input
	private SInt x;
	private SInt divisor;
	private int maxDivisorLength;
	private SInt result;

	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;
	private int precision;

	public SecretSharedDivisorProtocol(SInt x, SInt divisor,
			int maxDivisorLength, int precision, SInt result, BasicNumericFactory basicNumericFactory,
			RightShiftFactory rightShiftFactory) {
		this.x = x;
		this.divisor = divisor;
		this.maxDivisorLength = maxDivisorLength;
		this.precision = precision;
		
		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	@Override
	protected ProtocolProducer initializeGateProducer() {

		SequentialProtocolProducer divisionProtocol = new SequentialProtocolProducer();
		
		/*
		 * Calculate 2^m, 2^{2m}, ..., 2^{2^{p-1} m} where m = maxDivisorLength and p = precision.
		 */
		OInt[] twoPowers = new OInt[precision];
		twoPowers[0] = basicNumericFactory.getOInt(BigInteger.valueOf(1).shiftLeft(maxDivisorLength));
		for (int i = 1; i < precision; i++) {
			BigInteger previous = twoPowers[i-1].getValue();
			twoPowers[i] = basicNumericFactory.getOInt(previous.multiply(previous));
		}
		
		/*
		 * y = 2^m - divisor > 0.
		 */
		SInt y = basicNumericFactory.getSInt();
		Protocol findY = basicNumericFactory.getSubtractCircuit(twoPowers[0], divisor, y);
		divisionProtocol.append(findY);
		
		/*
		 * Calculate y, y^2, ..., y^{2{p-1}}.
		 */
		SInt[] yPowers = new SInt[precision];
		yPowers[0] = y;
		for (int i = 1; i < precision; i++) {
			yPowers[i] = basicNumericFactory.getSInt();
			divisionProtocol.append(basicNumericFactory.getMultCircuit(yPowers[i-1], yPowers[i-1], yPowers[i]));
		}
		
		/*
		 * Calculate (x + 1)(2^m + y)(2^{2m} + y^2) ... (2^{2^{p-1} m}
		 * y^{2^{p-1}}). Note that since the result is slightly smaller than the correct
		 * one and will be rounded towards zero, we use x+1 instead as x.
		 */
		SInt[] factors = new SInt[precision+1];
		factors[0] = basicNumericFactory.getSInt();
		divisionProtocol.append(basicNumericFactory.getAddProtocol(x, basicNumericFactory.getOInt(BigInteger.ONE), factors[0]));		
		ParallelProtocolProducer calculateFactors = new ParallelProtocolProducer();
		for (int i = 1; i < precision + 1; i++) {
			factors[i] = basicNumericFactory.getSInt();
			calculateFactors.append(basicNumericFactory.getAddProtocol(yPowers[i-1], twoPowers[i-1], factors[i]));
		}
		divisionProtocol.append(calculateFactors);
		
		NumericProtocolBuilder numericProtocolBuilder = new NumericProtocolBuilder(basicNumericFactory);
		SInt numerator = numericProtocolBuilder.mult(factors);
		divisionProtocol.append(numericProtocolBuilder.getCircuit());
		
		/*
		 * To get the right result we need to shift back 2^p m steps.
		 */
		int shifts = (1 << precision) * maxDivisorLength;
		divisionProtocol.append(rightShiftFactory.getRepeatedRightShiftProtocol(numerator, shifts, result));
		
		return divisionProtocol;
	}

}
