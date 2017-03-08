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
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementProtocol;
import dk.alexandra.fresco.suite.bgw.BgwProtocol;
import dk.alexandra.fresco.suite.bgw.BgwProtocolSuite;
import dk.alexandra.fresco.suite.bgw.ShamirShare;

public class BgwRandomIntProtocol extends BgwProtocol implements RandomFieldElementProtocol {

	public BgwSInt output;
	private int parties, treshold;

	public BgwRandomIntProtocol(SecureRandom rand, SInt out) {
		output = (BgwSInt) out;
	}

	public BgwRandomIntProtocol(SInt out, int noOfParties, int treshold) {
		output = (BgwSInt) out;
		this.parties = noOfParties;
		this.treshold = treshold;
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool,
			SCENetwork network) {
		switch (round) {
		case 0:
			BgwProtocolSuite suite = BgwProtocolSuite.getInstance();
			BigInteger mod = suite.getModulus();
			BigInteger secret = new BigInteger(
					mod.bitLength(), resourcePool.getSecureRandom())
					.mod(mod);
			ShamirShare[] reshares = ShamirShare.createShares(secret, parties,
					treshold);
			byte[][] data = new byte[reshares.length][];
			for(int i = 0; i < reshares.length; i++) {
				data[i] = reshares[i].toByteArray();
			}
			network.sendSharesToAll(data);
			network.expectInputFromAll();
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			List<ByteBuffer> buffers = network.receiveFromAll();
			List<ShamirShare> shares = new ArrayList<>();
			for(int i = 0; i < buffers.size(); i++) {
				byte[] tmp = new byte[ShamirShare.getSize()];
				shares.add(ShamirShare.deSerialize(tmp, 0));
			}
			reshares = (ShamirShare[]) shares.toArray();
			BigInteger ll = ShamirShare.recombine(reshares, parties);
			this.output.value = new ShamirShare(resourcePool.getMyId(), ll);
			return EvaluationStatus.IS_DONE;
		}
		throw new MPCException("Cannot evaluate rounds larger than 1");
	}

	@Override
	public String toString() {
		return "ShamirRandomIntGate(" + output + ")";
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
