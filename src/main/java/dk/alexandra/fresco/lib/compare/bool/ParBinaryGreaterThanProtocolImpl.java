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
package dk.alexandra.fresco.lib.compare.bool;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.helper.builder.tree.TreeProtocol;
import dk.alexandra.fresco.lib.helper.builder.tree.TreeProtocolNodeGenerator;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

/**
 * An experimental version of the BinaryComparisonprotocol. This attemps to get
 * better round complexity by doing as much work in parallel as possible. This
 * includes using a log depth tree structure to compute the last part of the protocol. 
 * 
 * Curiously however, this uses more time and memory than our alternatives. 
 * 
 * @author psn
 * 
 */
public class ParBinaryGreaterThanProtocolImpl extends AbstractRoundBasedProtocol
		implements BinaryGreaterThanProtocol, TreeProtocolNodeGenerator {

	private SBool[] inA;
	private SBool[] inB;
	private SBool[] eq;
	private SBool[] gt;
	private SBool outC;
	private int round = 0;
	private int length;

	private AbstractBinaryFactory factory;

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
	 *            a protocol factory
	 */
	public ParBinaryGreaterThanProtocolImpl(SBool[] inA, SBool[] inB, SBool outC,
			AbstractBinaryFactory factory) {
		if (inA.length == inB.length) {
			this.factory = factory;
			this.outC = outC;
			this.inA = inA;
			this.inB = inB;
			this.length = inA.length;
		} else {
			throw new RuntimeException("Comparison failed: bitsize differs");
		}
	}

	@Override
	public ProtocolProducer nextProtocolProducer() {
		BasicLogicBuilder blb = new BasicLogicBuilder(factory);
		if (round == 0) {
			eq = blb.xor(inA, inB);
			round++;
		} else if (round == 1) {
			gt = blb.and(inA, eq);
			round++;
		} else if (round == 2) {
			blb.notInPlace(eq, eq);
			blb.copy(gt[0], outC);
			round++;
		} else if (round == 3) {
			gt[0] = outC;
			blb.addProtocolProducer(new TreeProtocol(this));
			round++;
		} else {
			return null;
		}
		return blb.getProtocol();
	}

	@Override
	public ProtocolProducer getNode(int i, int j) {
		BasicLogicBuilder builder = new BasicLogicBuilder(factory);
		builder.beginSeqScope();
		builder.andInPlace(gt[j], gt[j], eq[i]);
		builder.orInPlace(gt[i], gt[i], gt[j]);
		builder.andInPlace(eq[i], eq[i], eq[j]);
		builder.endCurScope();
		return builder.getProtocol();
	}
	
	@Override
	public int getLength() {
		return length;
	}
}
