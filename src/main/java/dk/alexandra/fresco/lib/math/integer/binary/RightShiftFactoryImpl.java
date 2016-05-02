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
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;

public class RightShiftFactoryImpl implements RightShiftFactory {

	private final BasicNumericFactory basicNumericFactory;
	private final RandomAdditiveMaskFactory randomAdditiveMaskFactory;
	private final MiscOIntGenerators miscOIntGenerators;
	private final InnerProductFactory innerProductFactory;
	
	public RightShiftFactoryImpl(BasicNumericFactory basicNumericFactory, 
			RandomAdditiveMaskFactory randomAdditiveMaskFactory) {
		this.basicNumericFactory = basicNumericFactory;
		this.randomAdditiveMaskFactory = randomAdditiveMaskFactory;
		this.miscOIntGenerators = new MiscOIntGenerators(basicNumericFactory);
		this.innerProductFactory = new InnerProductFactoryImpl(basicNumericFactory);
	}

	@Override
	public RightShiftProtocol getRightShiftProtocol(SInt x, SInt result) {
		return new RightShiftProtocolImpl(x, result, basicNumericFactory.getMaxBitLength(), basicNumericFactory, 
				randomAdditiveMaskFactory, 
				miscOIntGenerators,
				innerProductFactory);
	}
	

	@Override
	public RightShiftProtocol getRightShiftProtocol(SInt x, SInt result, SInt remainder) {
		return new RightShiftProtocolImpl(x, result, remainder, basicNumericFactory.getMaxBitLength(), basicNumericFactory, 
				randomAdditiveMaskFactory, 
				miscOIntGenerators,
				innerProductFactory);
	}

	@Override
	public RepeatedRightShiftProtocol getRepeatedRightShiftProtocol(SInt x, int n, SInt result) {
		return new RepeatedRightShiftProtocolImpl(x, n, result, basicNumericFactory, this);
	}

	@Override
	public RepeatedRightShiftProtocol getRepeatedRightShiftProtocol(SInt x, int n, SInt result, SInt[] remainders) {
		return new RepeatedRightShiftProtocolImpl(x, n, result, remainders, basicNumericFactory, this);
	}
}
