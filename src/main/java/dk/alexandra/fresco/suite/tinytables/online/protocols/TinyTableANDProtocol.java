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
package dk.alexandra.fresco.suite.tinytables.online.protocols;

import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.tinytables.online.TinyTableProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTableSBool;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;

public class TinyTableANDProtocol extends TinyTableProtocol implements AndProtocol{

	private int id;
	private TinyTableSBool inLeft, inRight, out;
	
	public TinyTableANDProtocol(int id, TinyTableSBool inLeft, TinyTableSBool inRight, TinyTableSBool out) {
		super();
		this.id = id;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] {inLeft, inRight};
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] {out};
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
		TinyTableProtocolSuite ps = TinyTableProtocolSuite.getInstance(resourcePool.getMyId());	
		
		switch(round) {
		case 0: 
			boolean myShare = ps.getStorage().lookupTinyTable(id, inLeft.getValue(), inRight.getValue());
			
			network.expectInputFromAll();
			network.sendToAll(new byte[] { Encoding.encodeBoolean(myShare) });
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			List<byte[]> shares = network.receiveFromAll();		
			boolean res = false;
			for(byte[] share : shares) {
				res = res ^ Encoding.decodeBoolean(share[0]);
			}
			this.out.setValue(res);
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 0");
		}
	}

}
