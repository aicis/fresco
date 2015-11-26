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
package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.HammingDistanceFactory;

public class ZeroTestReducerCircuitImpl extends AbstractSimpleProtocol implements ZeroTestReducerCircuit {

	private final int bitLength;
	private final int securityParameter;
	private final SInt input, output;
	private final RandomAdditiveMaskFactory maskProvider;
	private final BasicNumericFactory provider;
	private ProtocolProducer gp = null;
	private Protocol loadRandC = null;
	private HammingDistanceFactory hammingProvider;
	private SInt[] r;
	
	public ZeroTestReducerCircuitImpl(
			int bitLength, 
			int securityParameter, 
			SInt input, SInt output, 
			RandomAdditiveMaskFactory maskProvider, 
			BasicNumericFactory provider, 
			HammingDistanceFactory hammingProvider) {
		this.bitLength = bitLength;
		this.securityParameter = securityParameter;
		
		this.maskProvider = maskProvider;
		this.provider = provider;
		this.hammingProvider = hammingProvider;
		
		this.input = input;
		this.output = output;
	}

	@Override
	protected ProtocolProducer initializeGateProducer() {
		// LOAD r
		SInt rValue = provider.getSInt();
		loadRandC = maskProvider.getRandomAdditiveMaskCircuit(bitLength, securityParameter, rValue);
		r = (SInt[]) loadRandC.getOutputValues();
		SInt mS = provider.getSInt();
		OInt mO = provider.getOInt();
		Protocol addCircuit = provider.getAddCircuit(input, r[bitLength], mS);
		// open m
		OpenIntProtocol openCircuitAddMask = provider.getOpenCircuit(mS, mO);
		// result = Hamming-dist_l(z,r);
		SInt[] rBits = new SInt[bitLength];
		System.arraycopy(r, 0, rBits, 0, rBits.length);
		Protocol hammingCircuit = hammingProvider.getHammingdistanceCircuit(rBits, mO, output);
		gp = new SequentialProtocolProducer(loadRandC, addCircuit, openCircuitAddMask, hammingCircuit);			
		return gp;
	}
}
