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
package dk.alexandra.fresco.lib.math;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.generic.AddProtocolFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * Circuit for computing the Hamming distance between an array of shared bits and a public value
 * @author ttoft
 *
 */
public class HammingDistanceCircuitImpl extends AbstractSimpleProtocol implements HammingDistanceCircuit {
	
	private final SInt[] aBits;
	private final OInt b;
	private final SInt result;
	private final BasicNumericFactory provider;
	private final NumericNegateBitFactory bitProvider; 
	private AddProtocolFactory addProvider;
	private final int length;
	
	public HammingDistanceCircuitImpl(SInt[] aBits, OInt b, SInt result, 
			BasicNumericFactory provider, NumericNegateBitFactory bitProvider) {
		this.aBits = aBits;
		this.b = b;
		this.result = result;
		this.provider = provider;
		this.bitProvider = bitProvider;
		this.addProvider = provider;

		this.length = aBits.length;
	}

	@Override
	protected ProtocolProducer initializeGateProducer() {
		BigInteger m = b.getValue();
		SInt[] XOR = new SInt[length];
		ParallelProtocolProducer XOR_GPs = new ParallelProtocolProducer();
		ProtocolProducer gp;
		if (length == 1) {
			if (m.testBit(0)) {
				gp = bitProvider.getNegatedBitCircuit(aBits[0], result);
			} else {
				SInt tmp = provider.getSInt();
				// TODO: undo this ugly hack -- output should be equal to input, hence we need a NOP-gate A better solution would be to add zero, however, adding OInt's is not provided
				// Someone should incorporate all arithmetic into the BasicNumericProvider. /Tomas
				gp = new SequentialProtocolProducer(bitProvider.getNegatedBitCircuit(aBits[0], tmp), bitProvider.getNegatedBitCircuit(tmp, result));
			}
		} else {
			// handle long-length
			// for each bit i of m negate r_i if m_i is set
			for (int i = 0; i < length; i++) {
				if (m.testBit(i)) {
					XOR[i] = provider.getSInt();
					XOR_GPs.append(bitProvider.getNegatedBitCircuit(
							aBits[i], XOR[i]));
				} else {
					XOR[i] = aBits[i];
				}
			}
			ProtocolProducer[] sumGPs = new ProtocolProducer[length - 1];

			SInt currentSum;
			if (length == 2) {
				currentSum = result;
			} else {
				currentSum = provider.getSInt();
			}
			sumGPs[0] = addProvider.getAddCircuit(XOR[0], XOR[1],
					currentSum);

			for (int i = 2; i < length; i++) {
				SInt newSum;
				// TODO: prettify -- not nice to have if in for-loop
				if (i != length - 1)
					newSum = provider.getSInt();
				else
					newSum = result;
				sumGPs[i - 1] = addProvider.getAddCircuit(currentSum,
						XOR[i], newSum);
				currentSum = newSum;
			}

			ProtocolProducer sumGP = new SequentialProtocolProducer(sumGPs);
			gp = new SequentialProtocolProducer(XOR_GPs, sumGP);
		}
		return gp;
	}
}
