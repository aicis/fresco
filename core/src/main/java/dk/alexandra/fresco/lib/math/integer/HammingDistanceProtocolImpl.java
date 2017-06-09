/******************************************************************************* * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.generic.AddProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import java.math.BigInteger;

/**
 * NativeProtocol for computing the Hamming distance between an array of shared bits and a public value
 * @author ttoft
 *
 */
public class HammingDistanceProtocolImpl extends SimpleProtocolProducer implements
		HammingDistanceProtocol {
	
	private final SInt[] aBits;
	private final OInt b;
	private final SInt result;
	private final BasicNumericFactory factory;
	private final NumericNegateBitFactory bitFactory; 
	private AddProtocolFactory addFactory;
	private final int length;
	
	public HammingDistanceProtocolImpl(SInt[] aBits, OInt b, SInt result, 
			BasicNumericFactory factory, NumericNegateBitFactory bitFactory) {
		this.aBits = aBits;
		this.b = b;
		this.result = result;
		this.factory = factory;
		this.bitFactory = bitFactory;
		this.addFactory = factory;

		this.length = aBits.length;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {
		BigInteger m = b.getValue();
		SInt[] XOR = new SInt[length];
		ParallelProtocolProducer XOR_PPs = new ParallelProtocolProducer();
		ProtocolProducer pp;
		if (length == 1) {
			if (m.testBit(0)) {
				pp = bitFactory.getNegatedBitProtocol(aBits[0], result);
			} else {
				SInt tmp = factory.getSInt();
				// TODO: undo this ugly hack -- output should be equal to input, hence we need a NOP-gate A better solution would be to add zero, however, adding OInt's is not provided
				// Someone should incorporate all arithmetic into the BasicNumericProvider. /Tomas
				pp = new SequentialProtocolProducer(bitFactory.getNegatedBitProtocol(aBits[0], tmp), bitFactory.getNegatedBitProtocol(tmp, result));
			}
		} else {
			// handle long-length
			// for each bit i of m negate r_i if m_i is set
			for (int i = 0; i < length; i++) {
				if (m.testBit(i)) {
					XOR[i] = factory.getSInt();
					XOR_PPs.append(bitFactory.getNegatedBitProtocol(
							aBits[i], XOR[i]));
				} else {
					XOR[i] = aBits[i];
				}
			}
			NativeProtocol[] sumpps = new NativeProtocol[length - 1];

			SInt currentSum;
			if (length == 2) {
				currentSum = result;
			} else {
				currentSum = factory.getSInt();
			}
			sumpps[0] = addFactory.getAddProtocol(XOR[0], XOR[1],
					currentSum);

			for (int i = 2; i < length; i++) {
				SInt newSum;
				// TODO: prettify -- not nice to have if in for-loop
				if (i != length - 1)
					newSum = factory.getSInt();
				else
					newSum = result;
				sumpps[i - 1] = addFactory.getAddProtocol(currentSum,
						XOR[i], newSum);
				currentSum = newSum;
			}

			ProtocolProducer sumpp = new SequentialProtocolProducer(sumpps);
			pp = new SequentialProtocolProducer(XOR_PPs, sumpp);
		}
		return pp;
	}
}
