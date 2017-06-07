/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionProtocol;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class SpdzLocalInversionProtocol extends SpdzNativeProtocol implements
		LocalInversionProtocol {

	private SpdzOInt in, out;

	public SpdzLocalInversionProtocol(OInt in, OInt out) {
		this.in = (SpdzOInt) in;
		this.out = (SpdzOInt) out;
	}

	public SpdzLocalInversionProtocol(SpdzOInt in, SpdzOInt out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		try {
			out.setValue(in.getValue().modInverse(Util.getModulus()));
		} catch (ArithmeticException e) {
			System.out.println("Non invertable value: " + in.getValue());
			throw e;
		}
		return EvaluationStatus.IS_DONE;
	}

	public String toString() {
		return "SpdzLocalInversionGate(" + in.getValue() + ", "
				+ out.getValue() + ")";
	}

}
