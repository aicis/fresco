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
package dk.alexandra.fresco.lib.compare.bool;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;

/**
 * Represents a comparison protocol between two bitstrings. Concretely, the
 * protocol computes the 'greater than' relation of strings A and B, i.e., it
 * computes C := A > B.
 * 
 * This uses the method of GenericBinaryComparison2 but is implemented a lot
 * cleaner and fixes some bugs.
 * 
 * @author psn
 * 
 */
public class BinaryGreaterThanProtocolImpl extends AbstractRoundBasedProtocol
		implements BinaryGreaterThanProtocol {

	private SBool[] inA;
	private SBool[] inB;
	private SBool outC;
	private SBool tmp;
	private SBool xor;

	private int round = 0;
	private int length;

	/**
	 * Construct a protocol to compare strings A and B. The bitstrings A and B
	 * are assumed to be even length and to be ordered from most- to least
	 * significant bit.
	 * 
	 * @param inA
	 *            input string A
	 * @param inB
	 *            input string B
	 * @param outC
	 *            a bit to hold the output C := A > B.
	 * @param factory
	 *            a protocol provider
	 */
	public BinaryGreaterThanProtocolImpl(SBool[] inA, SBool[] inB, SBool outC,
			Object factory) {

		this.outC = outC;
		this.inA = inA;
		this.inB = inB;
		this.length = inA.length;
		/*
		SBool[] inputs = new SBool[inA.length + inB.length];
		System.arraycopy(inA, 0, inputs, 0, inA.length);
		System.arraycopy(inB, 0, inputs, inA.length, inB.length);
		setInputValues(inputs);
		setOutputValues(new SBool[] { outC });
		*/
	}

	
	@Override
	public ProtocolProducer nextProtocolProducer() {
	/*	BasicLogicBuilder blb = new BasicLogicBuilder(factory);
		if (round == 0) {
			xor = blb.xor(inA[length - 1], inB[length - 1]);
			tmp = factory.getSBool();
			round++;
		} else if (round == 1) {
			blb.andInPlace(outC, inA[length - 1], xor);
			round++;
		} else if (round <= length) {
			int i = length - round;
			blb.xorInPlace(xor, inA[i], inB[i]);
			blb.xorInPlace(tmp, inA[i], outC);
			blb.andInPlace(tmp, xor, tmp);
			blb.xorInPlace(outC, tmp, outC);
			round++;
		} else {*/
			return null;
		//}
		//return blb.getProtocol();
	}
}