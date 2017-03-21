/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;

public class IntegerToBitsByShiftProtocolImpl extends AbstractSimpleProtocol implements
		IntegerToBitsProtocol {

	private final SInt input;
	private final int maxInputLength;
	private final SInt[] output;

	private final BasicNumericFactory basicNumericFactory;
	private final RightShiftFactory rightShiftFactory;

	/**
	 * Create a protocol which finds the bit representation of a given integer.
	 * This is done by repeadetly shifting the input to the right, so we need to
	 * supply the number of bits we want to find as a parameter.
	 * 
	 * @param input
	 *            An integer.
	 * @param maxInputLength
	 *            The number of bits we want to find.
	 * @param output
	 *            An array of bits.
	 * @param basicNumericFactory
	 * @param rightShiftFactory
	 */
	public IntegerToBitsByShiftProtocolImpl(SInt input, int maxInputLength, SInt[] output,
			BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory) {
		this.input = input;
		this.maxInputLength = maxInputLength;
		this.output = output;

		this.basicNumericFactory = basicNumericFactory;
		this.rightShiftFactory = rightShiftFactory;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {

		/*
		 * The result of the shift is ignored - we only need the bits that was
		 * thrown away.
		 */
		SInt tmp = basicNumericFactory.getSInt();

		ProtocolProducer rightShiftProtocol = rightShiftFactory.getRepeatedRightShiftProtocol(
				input, maxInputLength, tmp, output);
		return rightShiftProtocol;
	}

}
