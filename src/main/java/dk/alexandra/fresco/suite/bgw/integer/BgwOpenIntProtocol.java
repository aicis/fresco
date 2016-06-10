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

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.suite.bgw.BgwProtocol;
import dk.alexandra.fresco.suite.bgw.ShamirShare;

/**
 * A gate that opens a secret value to all or a target player.
 * 
 * Each party sends its share to all other parties, and then all parties
 * recombine.
 *
 */
public class BgwOpenIntProtocol extends BgwProtocol implements OpenIntProtocol {

	public BgwSInt input;
	public BgwOInt output;

	private int targetId = -1;

	public BgwOpenIntProtocol(SInt in, OInt out) {
		input = (BgwSInt) in;
		output = (BgwOInt) out;
	}

	public BgwOpenIntProtocol(int targetId, SInt in, OInt out) {
		this.targetId = targetId;
		input = (BgwSInt) in;
		output = (BgwOInt) out;
	}

	public BgwOpenIntProtocol(BgwSInt in, BgwOInt out) {
		input = in;
		output = out;
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		switch (round) {
		case 0:
			if (targetId != -1) {
				network.send(targetId, this.input.value);
				if (targetId == resourcePool.getMyId()) {
					network.expectInputFromAll();
				}
			} else {
				network.sendToAll(this.input.value);
				network.expectInputFromAll();
			}
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			List<ShamirShare> shares = network.receiveFromAll();
			BigInteger recombined = ShamirShare
					.recombine(shares, shares.size());
			this.output.setValue(recombined);
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1.");
		}
	}

	@Override
	public String toString() {
		return "ShamirOpenIntGate(" + input + "," + output + ")";
	}

	@Override
	public Value[] getInputValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value[] getOutputValues() {
		// TODO Auto-generated method stub
		return null;
	}
}
