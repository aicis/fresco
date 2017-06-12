/*
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

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;

public class DummyOpenBoolProtocol extends DummyProtocol implements OpenBoolProtocol {

	public DummySBool input;
	public DummyOBool output;
	
	private int target;
	
	/**
	 * Opens to all.
	 * 
	 */
	DummyOpenBoolProtocol(SBool in, OBool out) {
		input = (DummySBool)in;
		output = (DummyOBool)out;
		target = -1; // open to all
	}
	
	/**
	 * Opens to player with targetId.
	 * 
	 */
	DummyOpenBoolProtocol(SBool in, OBool out, int targetId) {
		input = (DummySBool)in;
		output = (DummyOBool)out;
		target = targetId;
	}
	
	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		boolean openToAll = target == -1;
		if (resourcePool.getMyId() == target || openToAll) {
			this.output.setValue(this.input.getValue());
		}
		return EvaluationStatus.IS_DONE;
	}	
	
	@Override
	public String toString() {
		return "DummyOpenBoolGate(" + input + "," + output + ")";
	}

	@Override
  public Value[] out() {
    return new Value[]{this.output};
  }

	
}
