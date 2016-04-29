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
package dk.alexandra.fresco.lib.compare.bool.eq;


import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.helper.builder.tree.TreeCircuit;
import dk.alexandra.fresco.lib.helper.builder.tree.TreeCircuitNodeGenerator;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

/**
 * An experimental implementation of the BinaryEqualityCircuit
 * 
 * @author psn
 *
 */
public class AltBinaryEqualityProtocol extends AbstractRoundBasedProtocol
		implements BinaryEqualityProtocol, TreeCircuitNodeGenerator {
	
	private AbstractBinaryFactory provider;
	private SBool[] inLeft;
	private SBool[] inRight;
	private SBool out;
	private final int length;
	private SBool[] xnorOuts;
	
	private int round = 0;

	public AltBinaryEqualityProtocol(SBool[] inLeft, SBool[] inRight,
			SBool out, AbstractBinaryFactory provider) {
		this.provider = provider;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;
		if (inLeft.length != inRight.length) {
			throw new IllegalArgumentException(
					"Binary strings must be of equal length");
		}
		this.length = inLeft.length;
	}
	
	@Override
	public ProtocolProducer nextProtocolProducer() {
		BasicLogicBuilder blb = new BasicLogicBuilder(provider);
		if (round == 0) {
			xnorOuts = blb.xor(inLeft, inRight);
			round++;
		} else if (round == 1) {
			blb.notInPlace(xnorOuts, xnorOuts);
			blb.copy(xnorOuts[0], out);
			round++;
		} else if (round == 2) {
			xnorOuts[0] = out;
			blb.addProtocolProducer(new TreeCircuit(this));
			round++;
		} else if (round == 3){
			return null;
		}
		return blb.getProtocol();
	}

	@Override
	public ProtocolProducer getNode(int i, int j) {
		ProtocolProducer gp = provider.getAndCircuit(xnorOuts[i], xnorOuts[j], xnorOuts[i]);
		return gp;
	}
	
	@Override
	public int getLength() {
		return length;
	}
}
