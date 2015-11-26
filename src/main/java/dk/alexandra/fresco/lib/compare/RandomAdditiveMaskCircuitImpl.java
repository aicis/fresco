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
package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.PreprocessedNumericBitFactory;
import dk.alexandra.fresco.lib.math.linalg.InnerProductFactory;

/**
 * Load random value used as additive mask + bits
 * @author ttoft
 *
 */
public class RandomAdditiveMaskCircuitImpl implements RandomAdditiveMaskCircuit {
	
	private final int bitLength;
	private final SInt[] r;
	private SInt[] allbits;
	private boolean done;
	private ProtocolProducer gp;
	private final InnerProductFactory innerProdProvider;
	private final OInt[] twoPows;

	
	/** 
	 * Circuit taking no input and generating uniformly random r in Z_{2^{l+k}} along with the bits of r mod 2^l
	 * @param bitLength -- the desired number of least significant bits, l
	 * @param securityParameter -- the desired security parameter, k, (leakage with probability 2^{-k}
	 * @param r - r[i] = r_i for 0<=i<l; r[l] = r
	 */
	public RandomAdditiveMaskCircuitImpl(
			int bitLength, 
			int securityParameter, 
			SInt rValue, 
			BasicNumericFactory provider, 
			PreprocessedNumericBitFactory bitProvider, 
			MiscOIntGenerators miscOIntGenerator, 
			InnerProductFactory innerProdProvider) {
		// Copy inputs, setup stuff
		this.bitLength = bitLength;
		this.innerProdProvider = innerProdProvider;
		this.twoPows = miscOIntGenerator.getTwoPowers(securityParameter + bitLength);		
		
		done = false;
		gp = null;

		// loadRandBits
		// r[i] = i'th bit; 0 <= i < bitLength
		// r[bitLenght] = r
		this.allbits = new SInt[bitLength+securityParameter];
		this.r = new SInt[bitLength+1];

		int i;
		for (i=0; i<bitLength; i++) {
			allbits[i] = bitProvider.getRandomSecretSharedBit();
			r[i] = allbits[i];
		}
		for (i = bitLength; i<bitLength+securityParameter; i++) {
			allbits[i] = bitProvider.getRandomSecretSharedBit();
		}
		this.r[bitLength] = rValue;
	}
	
	@Override
	public Value[] getInputValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value[] getOutputValues() {
		return r;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {
			// compute r[bitLenght] = r = \sum_i 2^i*allbits[i]
			// r[bitLength] initialized in constructor
			gp = innerProdProvider.getInnerProductCircuit(allbits, twoPows, r[bitLength]);
		}
		if (gp.hasNextProtocols()){
			pos = gp.getNextProtocols(gates, pos);
		}
		else if (!gp.hasNextProtocols()){
			gp = null;
			done = true;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return !done;
	}

}
