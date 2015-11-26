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
package dk.alexandra.fresco.suite.bgw.integer;

import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.suite.bgw.BgwProtocol;
import dk.alexandra.fresco.suite.bgw.BgwProtocolSuite;
import dk.alexandra.fresco.suite.bgw.ShamirShare;

public class BgwMultProtocol extends BgwProtocol implements MultProtocol {

	private BgwSInt inA;
	private BgwSInt inB;
	private BgwSInt outC;

	public BgwMultProtocol(SInt inA, SInt inB, SInt outC) {
		this.inA = (BgwSInt) inA;
		this.inB = (BgwSInt) inB;
		this.outC = (BgwSInt) outC;
	}

	public BgwMultProtocol(BgwSInt inA, BgwSInt inB, BgwSInt outC) {
		this.inA = inA;
		this.inB = inB;
		this.outC = outC;
	}

	@Override
	public String toString() {
		return "ShamirMultGate(" + inA + "," + inB + "," + outC + ")";
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] { inA, inB };
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { outC };
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		int n = resourcePool.getNoOfParties();
		int threshold = BgwProtocolSuite.getInstance().getThreshold();
		switch (round) {
		case 0:
			outC.value = inA.value.mult(inB.value);
			ShamirShare[] reshares = ShamirShare.createShares(
					outC.value.getField(), n, threshold);
			network.sendSharesToAll(reshares);
			network.expectInputFromAll();
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			List<ShamirShare> shares = network.receiveFromAll();
			outC.value = new ShamirShare(resourcePool.getMyId(),
					ShamirShare.recombine(shares, shares.size()));
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1.");
		}
	}

}
