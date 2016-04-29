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
package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.XnorProtocol;

public class XnorFromXorAndNotProtocolImpl implements XnorProtocol{

	SBool left; 
	SBool right; 
	SBool out;
	BasicLogicFactory provider;
	private ProtocolProducer curGP = null;
	private boolean done = false;
	private boolean xorDone = false;
	private SBool tmpOut;
	
	public XnorFromXorAndNotProtocolImpl(SBool left, SBool right, SBool out,
			BasicLogicFactory provider) {
		this.left = left;
		this.right = right;
		this.out = out;
		this.provider = provider;
	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (curGP == null) {
			tmpOut = provider.getSBool();
			curGP = provider.getXorProtocol(left, right, tmpOut);
			pos = curGP.getNextProtocols(gates, pos);
			return pos;
		}
		if (!curGP.hasNextProtocols()) {
			if (!xorDone) {
				curGP = provider.getNotCircuit(tmpOut, out);
				xorDone = true;
				pos = curGP.getNextProtocols(gates, pos);
				return pos;
			} else {
				done = true;
			}
		} else {
			pos = curGP.getNextProtocols(gates, pos);
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return !done;
	}
	
	@Override
	public Value[] getInputValues() {
		return new Value[]{left, right};
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[]{out};
	}

}
