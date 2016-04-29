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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.AppendableProtocolProducer;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * Does a simple compare like this: out = (a1 XNOR b1) AND (a2 XNOR b2) AND (a3
 * XNOR b3) AND ...
 * 
 * The XNORs are done in parallel and the ANDs are done by a log-depth tree
 * structured protocol. 
 * 
 * @author Kasper Damgaard
 * 
 */
public class BinaryEqualityProtocolImpl implements BinaryEqualityProtocol {

	private BasicLogicFactory factory;
	private SBool[] inLeft;
	private SBool[] inRight;
	private SBool out;
	private final int length;

	private SBool[] xnorOuts;
	
	private int step = 1;
	private boolean xnorDone = false;

	private AppendableProtocolProducer curPP = null;

	public BinaryEqualityProtocolImpl(SBool[] inLeft, SBool[] inRight,
			SBool out, BasicLogicFactory factory) {
		this.factory = factory;
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
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if (xnorOuts == null) {
			xnorOuts = new SBool[length];
			for (int i = 1; i < length; i++) {
				xnorOuts[i] = factory.getSBool();
			}
			xnorOuts[0] = out;
		}
		if (curPP == null) {
			if (!xnorDone) {
				ParallelProtocolProducer parXOR = new ParallelProtocolProducer();
				ParallelProtocolProducer parNOT = new ParallelProtocolProducer();
				for (int i = 0; i < length; i++) {
					parXOR.append(factory.getXorProtocol(inLeft[i], inRight[i],
							xnorOuts[i]));
					parNOT.append(factory.getNotProtocol(xnorOuts[i],
							xnorOuts[i]));
				}
				curPP = new SequentialProtocolProducer(parXOR, parNOT);
			} else {
				ParallelProtocolProducer parAND = new ParallelProtocolProducer();
				int i = 0;
				while (i + step < length) {
					Protocol and = factory.getAndProtocol(xnorOuts[i],
							xnorOuts[i + step], xnorOuts[i]);
					parAND.append(and);
					i += 2 * step;
				}
				curPP = parAND;
			}
		}
		if (curPP.hasNextProtocols()) {
			pos = curPP.getNextProtocols(nativeProtocols, pos);
		} else if (!curPP.hasNextProtocols()) {
			if (!xnorDone) {
				xnorDone = true;
			} else {
				step *= 2;
			}
			curPP = null;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return step < length;
	}

	@Override
	public Value[] getInputValues() {
		Value[] res = new Value[this.length];
		for (int i = 0; i < this.length; i++) {
			res[i] = inLeft[i];
			res[i + this.length] = inRight[i];
		}
		return res;
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}

}
