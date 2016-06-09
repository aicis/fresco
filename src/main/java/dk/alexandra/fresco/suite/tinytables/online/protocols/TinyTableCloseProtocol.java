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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.suite.tinytables.online.TinyTableProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTableOBool;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTableSBool;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;

public class TinyTableCloseProtocol extends TinyTableProtocol implements CloseBoolProtocol {

	private int inputter;
	private TinyTableOBool in;
	private TinyTableSBool out;
	
	public TinyTableCloseProtocol(int id, int inputter, OBool in, SBool out) {
		this.id = id;
		this.inputter = inputter;
		this.in = (TinyTableOBool)in;
		this.out = (TinyTableSBool)out;
	}
	
	@Override
	public Value[] getInputValues() {
		return new Value[] {in};
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
			if(resourcePool.getMyId() == this.inputter) {
				boolean r = ps.getStorage().getMaskShare(id);
				boolean e = this.in.getValue() ^ r;
				out.setValue(e);
				network.sendToAll(new byte[] { Encoding.encodeBoolean(e) } );
			}
			network.expectInputFromPlayer(this.inputter);
			return EvaluationStatus.HAS_MORE_ROUNDS;
			
		case 1:
			byte[] share = network.receive(this.inputter);
			out.setValue(Encoding.decodeBoolean(share[0]));
			return EvaluationStatus.IS_DONE;
			
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1");
		}
	}

	

}
