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
package dk.alexandra.fresco.suite.ninja.protocols;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaOBool;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.NinjaSBool;
import dk.alexandra.fresco.suite.ninja.storage.PrecomputedInputNinja;
import dk.alexandra.fresco.suite.ninja.util.NinjaUtil;

public class NinjaCloseProtocol extends NinjaProtocol implements CloseBoolProtocol {

	private int inputter;
	private NinjaOBool in;
	private NinjaSBool out;
	private PrecomputedInputNinja inputNinja;
	
	public NinjaCloseProtocol(int id, int inputter, OBool in, SBool out) {
		this.id = id;
		this.inputter = inputter;
		this.in = (NinjaOBool)in;
		this.out = (NinjaSBool)out;
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
		NinjaProtocolSuite ps = NinjaProtocolSuite.getInstance(resourcePool.getMyId()); 
		switch(round) {
		case 0: 			
			if(resourcePool.getMyId() == this.inputter) {
				inputNinja = ps.getStorage().getPrecomputedInputNinja(this.getId());
				boolean real = inputNinja.getRealValue();
				boolean x = this.in.getValue();
				boolean y = x ^ real;
				network.sendToAll(new byte[] { NinjaUtil.encodeBoolean(y) });
			}
			network.expectInputFromPlayer(inputter);
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			boolean y = NinjaUtil.decodeBoolean(network.receive(inputter)[0]);
			this.out.setValue(y);
			
			inputNinja = null;
			in = null;			
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1");
		}
	}


}
