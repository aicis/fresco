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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.suite.bgw.BgwFactory;
import dk.alexandra.fresco.suite.bgw.BgwProtocolSuite;

/**
 * Helper class. Given a ShamirSInt s, it computes s^-1 mod p, for the prime p used in the ShamirShare class
 * Useful for computing AND on an array of SBools in constant rounds. 
 * @author mstaus
 *
 */

public class BgwInvertIntProtocol implements Protocol {

	public BgwSInt output;
	public BgwSInt input;
	private BgwSInt prod;
	private BgwOInt oprod;
	private BgwFactory factory;
	private Protocol pp;
	private int innerRound = -1;
	
	public BgwInvertIntProtocol(BgwFactory factory, SInt in, SInt out) {
		this(factory, (BgwSInt)in, (BgwSInt)out);
	}
	

	public BgwInvertIntProtocol(BgwFactory factory, BgwSInt in, BgwSInt out) {
		this.factory = factory;
		output = out;
		input = in;
		oprod = factory.getOInt();
		prod = factory.getSInt();
//		this.parties = noOfParties;
//		this.treshold = treshold;
	}
	
	
	@Override
	public String toString() {
		return "ShamirInvertIntGate(" + output + ")";
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


	@Override
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if(innerRound == -1){
			if(pp == null) {
				pp =factory.getRandomIntGate(output); 
			}
			if(pp.hasNextProtocols()) pos = pp.getNextProtocols(nativeProtocols, pos);
			if(!pp.hasNextProtocols()){
				innerRound++;
				pp = null;
			}
			return pos;
		}else if(innerRound == 0){
			if(pp == null) pp =factory.getMultProtocol(input, output, prod);
			if(pp.hasNextProtocols()) pos = pp.getNextProtocols(nativeProtocols, pos);
			if(!pp.hasNextProtocols()){
				innerRound++;
				pp = null;
			}
			return pos;
		}else if(innerRound == 1){
			if(pp == null) 	pp =factory.getOpenProtocol(prod,oprod); 
			if(pp.hasNextProtocols()) pos = pp.getNextProtocols(nativeProtocols, pos);
			if(!pp.hasNextProtocols()){
				innerRound++;
				pp = null;
			}
			return pos;
		}else if(innerRound == 2){
			BgwProtocolSuite suite = BgwProtocolSuite.getInstance();
			BigInteger mod = suite.getModulus();
			BigInteger rinv = oprod.getValue().modInverse(mod);
			output.value.setField(rinv.multiply(output.value.getField()).mod(mod));
			innerRound++;
			return pos;
		}
		return 0;
	}


	@Override
	public boolean hasNextProtocols() {
		return innerRound <3;
	}

	
}
