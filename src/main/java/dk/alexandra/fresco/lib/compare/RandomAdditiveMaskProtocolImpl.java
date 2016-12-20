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

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;

/**
 * Load random value used as additive mask + bits
 * 
 * @author ttoft
 *
 */
public class RandomAdditiveMaskProtocolImpl extends AbstractSimpleProtocol implements
		RandomAdditiveMaskProtocol {

	private final int bitLength;
	private final int securityParameter;
	private final InnerProductFactory innerProdProvider;
	private final NumericBitFactory bitProvider;
	private final MiscOIntGenerators miscOIntGenerator;
	private final SInt[] rBits;
	private final SInt r;
	private BasicNumericFactory basicNumericFactory;

	/**
	 * Protocol taking no input and generating uniformly random r in
	 * Z<sub>2<sup>l+k</sup></sub> along with the bits of r (mod 2<sup>l</sup>)
	 * 
	 * @param bitLength
	 *            The desired number of least significant bits, l
	 * @param securityParameter
	 *            The security parameter k. Assuming that we are about to open
	 *            m+r where m is a secret shared value and r is the value
	 *            calculated by this protocol, there is a chance of leakage if
	 *            the top bit of both r and m are 1 since this will give a
	 *            carry. To avoid this we make r longer than the max bit length
	 *            of m, and each bit we make it longer decreases the risk of
	 *            leakage (k extra bits give a risk of leakage at most
	 *            2<sup>-k</sup>).
	 * @param bits
	 *            The first l bits of r
	 * @param r
	 * 
	 */
	public RandomAdditiveMaskProtocolImpl(int securityParameter, SInt[] bits, SInt r,
			BasicNumericFactory basicNumericFactory, NumericBitFactory bitProvider,
			MiscOIntGenerators miscOIntGenerator, InnerProductFactory innerProdProvider) {
		// Copy inputs, setup stuff
		this.bitLength = bits.length;
		this.securityParameter = securityParameter;
		this.rBits = bits;
		this.r = r;
		this.basicNumericFactory = basicNumericFactory;
		this.innerProdProvider = innerProdProvider;
		this.miscOIntGenerator = miscOIntGenerator;
		this.bitProvider = bitProvider;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {

		// loadRandBits
		// bits[i] = i'th bit; 0 <= i < bitLength
		SInt[] allbits = new SInt[bitLength + securityParameter];
		ParallelProtocolProducer randomBits = new ParallelProtocolProducer();
		int i;
		for (i = 0; i < bitLength; i++) {
			randomBits.append(bitProvider.createRandomSecretSharedBitProtocol(rBits[i]));
			allbits[i] = rBits[i];
		}
		for (i = bitLength; i < bitLength + securityParameter; i++) {
			allbits[i] = basicNumericFactory.getSInt();
			randomBits.append(bitProvider.createRandomSecretSharedBitProtocol(allbits[i]));

		}

		OInt[] twoPows = miscOIntGenerator.getTwoPowers(securityParameter + bitLength);
		return new SequentialProtocolProducer(randomBits, innerProdProvider.getInnerProductProtocol(
				allbits, twoPows, r));
	}

}
