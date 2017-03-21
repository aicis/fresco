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
package dk.alexandra.fresco.lib.math.integer.log;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;

/**
 * This class implements a protocol for finding the natural logarithm of a
 * secret shared integer. It is based on approximating the logarithm of base 2
 * using the bitlength of a number and then scaling it to the natural logarithm.
 * 
 * Since the bitlength of a number is only an approximation of the logarithm of
 * base 2, this protocol is not nessecarily correct on the least significant
 * bit.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class LogarithmProtocolImpl extends AbstractSimpleProtocol implements LogarithmProtocol {

	// Input
	private SInt input;
	private int maxInputLength;
	private SInt result;

	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;
	private final BitLengthFactory bitLengthFactory;

	
	public LogarithmProtocolImpl(SInt input, int maxInputLength, SInt result,
			BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory, BitLengthFactory bitLengthFactory) {
		this.input = input;
		this.maxInputLength = maxInputLength;
		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
		this.bitLengthFactory = bitLengthFactory;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {
		NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);

		/*
		 * ln(2) = 45426 >> 16;
		 */
		OInt ln2 = basicNumericFactory.getOInt(BigInteger.valueOf(45426));
		int shifts = 16;
		
		builder.beginSeqScope();

		/*
		 * Find the bit length of the input. Note that bit length - 1 is the
		 * floor of the the logartihm with base 2 of the input.
		 */
		SInt bitLength = builder.getSInt();
		builder.addProtocolProducer(bitLengthFactory.getBitLengthProtocol(input, bitLength, maxInputLength));
		SInt log2 = builder.sub(bitLength, basicNumericFactory.getOInt(BigInteger.ONE));
		
		/*
		 * ln(x) = log_2(x) * ln(2), and we use 45426 >> 16 as an approximation of ln(2).
		 */
		SInt scaledLog = builder.mult(ln2, log2);
		builder.addProtocolProducer(rightShiftFactory.getRepeatedRightShiftProtocol(scaledLog, shifts, result));
		
		builder.endCurScope();

		return builder.getProtocol();
	}

}
