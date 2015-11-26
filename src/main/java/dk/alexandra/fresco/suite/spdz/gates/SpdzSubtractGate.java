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
package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.SubtractCircuit;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;

public class SpdzSubtractGate extends SpdzNativeProtocol implements SubtractCircuit {

	private SpdzSInt left, right, out;
	private SpdzOInt openLeft;
	private SpdzFactory provider;

	public SpdzSubtractGate(SInt left, SInt right, SInt out,
			SpdzFactory provider) {
		this.left = (SpdzSInt) left;
		this.right = (SpdzSInt) right;
		this.out = (SpdzSInt) out;
		this.provider = provider;
	}

	public SpdzSubtractGate(SpdzSInt left, SpdzSInt right, SpdzSInt out,
			SpdzFactory provider) {
		this.left = left;
		this.right = right;
		this.out = out;
		this.provider = provider;
	}

	public SpdzSubtractGate(OInt left, SInt right, SInt out,
			SpdzFactory provider) {
		this.openLeft = (SpdzOInt) left;
		this.right = (SpdzSInt) right;
		this.out = (SpdzSInt) out;
		this.provider = provider;
	}

	@Override
	public String toString() {
		if (openLeft != null) {
			return "SpdzSubtractGate(" + openLeft.getValue() + ", "
					+ right.value + ", " + out.value + ")";
		}
		return "SpdzSubtractGate(" + left.value + ", " + right.value + ", "
				+ out.value + ")";
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] { left, right };
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		if (openLeft != null) {
			SpdzSInt converted = (SpdzSInt) provider.getSInt(openLeft
					.getValue());
			out.value = converted.value.subtract(right.value);
		} else {
			SpdzElement elm = left.value.subtract(right.value);
			out.value = elm;
		}
		return EvaluationStatus.IS_DONE;
	}

}
