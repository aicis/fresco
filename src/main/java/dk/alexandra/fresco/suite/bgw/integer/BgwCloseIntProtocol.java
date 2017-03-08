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
import java.util.Arrays;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.CloseIntProtocol;
import dk.alexandra.fresco.suite.bgw.BgwProtocol;
import dk.alexandra.fresco.suite.bgw.BgwProtocolSuite;
import dk.alexandra.fresco.suite.bgw.ShamirShare;

public class BgwCloseIntProtocol extends BgwProtocol implements
		CloseIntProtocol {

	public OInt input;
	public BgwSInt output;
	private int inputter;

	public BgwCloseIntProtocol(OInt in, SInt out, int inputter) {
		input = in;
		output = (BgwSInt) out;
		this.inputter = inputter;
	}

	@Override
	public EvaluationStatus evaluate(int round,
			ResourcePool resourcePool, SCENetwork network) {		
		int myId = resourcePool.getMyId();
		switch (round) {
		case 0:
			if (inputter == myId) {
				BigInteger secret = this.input.getValue();
				ShamirShare[] shares = ShamirShare.createShares(secret,
						resourcePool.getNoOfParties(), BgwProtocolSuite.getInstance().getThreshold());
				byte[][] toSend = new byte[shares.length][];
				for(int i = 0; i < shares.length; i++) {
					toSend[i] = shares[i].toByteArray();
				}				
				network.sendSharesToAll(toSend);
			}
			network.expectInputFromPlayer(inputter);
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			byte[] data = new byte[ShamirShare.getSize()]; 
			network.receive(inputter).get(data);
			this.output.value = ShamirShare.deSerialize(data, 0);
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1.");
		}
	}

	@Override
	public String toString() {
		return "ShamirInputIntGate(" + input + "," + output + ")";
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
