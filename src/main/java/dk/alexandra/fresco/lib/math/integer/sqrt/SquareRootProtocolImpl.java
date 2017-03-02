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
package dk.alexandra.fresco.lib.math.integer.sqrt;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;

/**
 * This class implements a protocol for approximating the square root of a
 * secret shared integer using the <a href=
 * "https://en.wikipedia.org/wiki/Methods_of_computing_square_roots#Babylonian_method"
 * >Babylonian Method</a>.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SquareRootProtocolImpl extends AbstractSimpleProtocol implements SquareRootProtocol {

	// Input
	private SInt input;
	private int maxInputLength;
	private SInt result;
	private OInt precision;

	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final DivisionFactory divisionFactory;
	private final RightShiftFactory rightShiftFactory;

	public SquareRootProtocolImpl(SInt input, int maxInputLength, SInt result,
			BasicNumericFactory basicNumericFactory, DivisionFactory divisionFactory,
			RightShiftFactory rightShiftFactory) {
		this.input = input;
		this.maxInputLength = maxInputLength;

		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.divisionFactory = divisionFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	public SquareRootProtocolImpl(SInt input, int maxInputLength, SInt result, OInt precision,
			BasicNumericFactory basicNumericFactory, DivisionFactory divisionFactory,
			RightShiftFactory rightShiftFactory) {
		
		this(input, maxInputLength, result, basicNumericFactory, divisionFactory, rightShiftFactory);
		this.precision = precision;
	}
	
	@Override
	protected ProtocolProducer initializeProtocolProducer() {

		NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);
		builder.beginSeqScope();

		/*
		 * Convergence is quadratic (the number of correct digits rougly doubles
		 * on each iteration) so assuming we have at least one digit correct
		 * after first iteration, we need at about log2(maxInputLength)
		 * iterations in total.
		 */
		int iterations = log2(maxInputLength) + 1;

		/*
		 * First guess is x << maxInputLength / 2 + 1. We add 1 to avoid the
		 * this to be equal to zero since we divide by it later.
		 */
		SInt[] y = new SInt[iterations];
		SInt tmp = builder.getSInt();
		builder.addProtocolProducer(rightShiftFactory.getRepeatedRightShiftProtocol(input,
				maxInputLength / 2, tmp));
		y[0] = builder.add(tmp, basicNumericFactory.getOInt(BigInteger.ONE));

		/*
		 * We iterate y[n+1] = (y[n] + x / y[n]) / 2.
		 */
		for (int i = 1; i < iterations; i++) {
			SInt quotient = builder.getSInt();
			
			/*
			 * The lower bound on the precision from the division protocol will
			 * also be a lower bound of the precision of this protocol.
			 */
			builder.addProtocolProducer(divisionFactory.getDivisionProtocol(input,
					y[i - 1], quotient, precision));
			SInt sum = builder.add(y[i - 1], quotient);
			y[i] = builder.getSInt();
			builder.addProtocolProducer(rightShiftFactory.getRightShiftProtocol(sum, y[i]));
		}

		builder.copy(result, y[iterations - 1]);

		builder.endCurScope();
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

	private static int ceil(double x) {
		return (int) Math.ceil(x);
	}
	
}
