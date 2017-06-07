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
package dk.alexandra.fresco.suite.dummy;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;

public class DummyCloseBoolProtocol extends DummyProtocol implements CloseBoolProtocol {

	public DummyOBool input;
	public DummySBool output;
	
	private int sender;
	
	public DummyCloseBoolProtocol(OBool in, SBool out, int sender) {
		input = (DummyOBool)in;
		output = (DummySBool)out;
		this.sender = sender;
	}
	
	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
		switch (round) {
		case 0:
			if (resourcePool.getMyId() == sender) {
				network.sendToAll(BooleanSerializer.toBytes(input.getValue()));
			}
			network.expectInputFromPlayer(sender);
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			boolean r = BooleanSerializer.fromBytes(network.receive(sender));
			this.output.setValue(r);
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Bad round: " + round);
		}
	}
	
	@Override
	public String toString() {
		return "DummyCloseBoolGate(" + input + "," + output + ")";
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] {this.output};
	}

	
}
