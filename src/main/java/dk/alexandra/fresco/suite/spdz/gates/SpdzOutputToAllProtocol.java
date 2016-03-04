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

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class SpdzOutputToAllProtocol extends SpdzNativeProtocol implements
		OpenIntProtocol {

	private SpdzSInt in;
	private SpdzOInt out;

	public SpdzOutputToAllProtocol(SInt in, OInt out) {
		this.in = (SpdzSInt) in;
		this.out = (SpdzOInt) out;
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		SpdzProtocolSuite spdzpii = SpdzProtocolSuite
				.getInstance(resourcePool.getMyId());
		SpdzStorage storage = spdzpii.getStore(network.getThreadId());
		switch (round) {
		case 0:
			network.sendToAll(Util.convertBigIntToBytes(in.value.getShare()));
			network.expectInputFromAll();
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			List<byte[]> shares = network.receiveFromAll();
			BigInteger openedVal = BigInteger.valueOf(0);
			for (byte[] share : shares) {
				openedVal = openedVal.add(new BigInteger(share));
			}
			openedVal = openedVal.mod(Util.getModulus());
			storage.addOpenedValue(openedVal);
			storage.addClosedValue(in.value);
			BigInteger tmpOut = openedVal;
			tmpOut = Util.convertRepresentation(tmpOut);
			out.setValue(tmpOut);
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("No more rounds to evaluate.");
		}
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] { in };
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}
}
