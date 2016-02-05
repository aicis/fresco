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
package dk.alexandra.fresco.suite.bgw;

import java.math.BigInteger;

import org.apache.commons.lang.NotImplementedException;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.value.KnownSIntProtocol;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.AddProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.CloseIntProtocol;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractCircuit;
import dk.alexandra.fresco.lib.math.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.inv.LocalInversionCircuit;
import dk.alexandra.fresco.lib.math.inv.LocalInversionFactory;
import dk.alexandra.fresco.suite.bgw.integer.BgwAddProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwCloseIntProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwInvertIntProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwKnownSIntProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwLocalInvProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwMultProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwMultWithPublicProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwOInt;
import dk.alexandra.fresco.suite.bgw.integer.BgwOpenIntProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwRandomIntProtocol;
import dk.alexandra.fresco.suite.bgw.integer.BgwSInt;
import dk.alexandra.fresco.suite.bgw.integer.BgwSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;

public class BgwFactory implements BasicNumericFactory, LocalInversionFactory, ExpFromOIntFactory {

	private int myId;
	private int noOfParties;
	private int threshold;
	private BigInteger mod;

	public BgwFactory(int myId, int noOfParties, int threshold, BigInteger modulus) {
		this.myId = myId;
		this.noOfParties = noOfParties;
		this.threshold = threshold;
		this.mod = modulus;
	}

	@Override
	public BgwSInt getSInt() {
		return new BgwSInt();
	}

	@Override
	public BgwSInt getSInt(BigInteger i) {
		return new BgwSInt(new ShamirShare(myId, i));
	}

	@Override
	public BgwSInt getSInt(int i) {
		return this.getSInt(BigInteger.valueOf(i));
	}

	public BgwRandomIntProtocol getRandomIntGate(SInt s) {
		BgwRandomIntProtocol ig = new BgwRandomIntProtocol(s, noOfParties,
				threshold);
		return ig;
	}

	@Override
	public BgwSInt getRandomSInt() {
		throw new MPCException("Not implemented yet");
	}

	@Override
	public BgwOInt getOInt() {
		return new BgwOInt();
	}

	@Override
	public BgwOInt getOInt(BigInteger i) {
		return new BgwOInt(i);
	}

	@Override
	public OInt getRandomOInt() {
		throw new MPCException("Not implemented yet");
	}


	@Override
	public AddProtocol getAddProtocol(SInt a, SInt b, SInt out) {
		return new BgwAddProtocol(a, b, out);
	}

	@Override
	public SubtractCircuit getSubtractCircuit(SInt a, SInt b, SInt out) {
		return new BgwSubtractProtocol(a, b, out);
	}

	@Override
	public SubtractCircuit getSubtractCircuit(OInt a, SInt b, SInt out) {
		throw new NotImplementedException(
				"Cannot currently use BGW to subtract a secret value from a public value.");
	}

	@Override
	public MultProtocol getMultCircuit(SInt a, SInt b, SInt out) {
		return new BgwMultProtocol(a, b, out);
	}

	// test purpose only
	public Protocol getInvertIntCircuit(SInt in, SInt out) {
		return new BgwInvertIntProtocol(this, in, out);
	}

	@Override
	public KnownSIntProtocol getSInt(int i, SInt si) {
		return this.getSInt(BigInteger.valueOf(i), si);
	}

	@Override
	public KnownSIntProtocol getSInt(BigInteger i, SInt si) {
		return new BgwKnownSIntProtocol((BgwSInt)si, i);
	}

	public int getMyId() {
		return this.myId;
	}


	@Override
	public CloseIntProtocol getCloseProtocol(int source, OInt open, SInt closed) {
		return new BgwCloseIntProtocol(open, closed, source);
	}

	@Override
	public OpenIntProtocol getOpenProtocol(SInt closed, OInt open) {
		return new BgwOpenIntProtocol(closed, open);
	}

	@Override
	public OpenIntProtocol getOpenProtocol(int target, SInt closed, OInt open) {
		return new BgwOpenIntProtocol(target, closed, open);
	}

	@Override
	public AddProtocol getAddProtocol(SInt input, OInt openInput, SInt out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CloseIntProtocol getCloseProtocol(BigInteger open, SInt closed,
			int targetID) {
		return getCloseProtocol(targetID, this.getOInt(open), closed);
	}


	@Override
	public MultProtocol getMultCircuit(OInt a, SInt b, SInt c) {
		return new BgwMultWithPublicProtocol((BgwOInt)a, (BgwSInt)b, (BgwSInt)c);
	}

	@Override
	public int getMaxBitLength() {
		return BgwProtocolSuite.getInstance().getMaxBitLength();
	}

	@Override
	public SInt getSqrtOfMaxValue() {
		BigInteger two = BigInteger.valueOf(2);
		BigInteger max = mod.subtract(BigInteger.ONE).divide(two);
		int bitlength = max.bitLength();
		BigInteger approxMaxSqrt = two.pow(bitlength / 2);
		return this.getSInt(approxMaxSqrt);
	}

	@Override
	public LocalInversionCircuit getLocalInversionCircuit(OInt x, OInt result) {
		return new BgwLocalInvProtocol((BgwOInt)x, (BgwOInt)result);
	}

	@Override
	public OInt[] getExpFromOInt(OInt value, int maxExp) {
		BigInteger[] exps = new BigInteger[maxExp];
		exps[0] = value.getValue();
		for(int i = 1; i < exps.length; i++){
			exps[i] = exps[i-1].multiply(value.getValue()).mod(this.mod);
		}
		OInt[] expPipe = new OInt[exps.length];
		for (int i = 0; i < exps.length; i++) {
			expPipe[i] = new SpdzOInt(exps[i]);
		}
		return expPipe;
	}

}
