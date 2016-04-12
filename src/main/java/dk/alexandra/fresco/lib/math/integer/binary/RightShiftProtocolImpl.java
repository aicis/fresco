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
package dk.alexandra.fresco.lib.math.integer.binary;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;

public class RightShiftProtocolImpl implements RightShiftProtocol {
	
	// Input
	private SInt x;
	private SInt result;
	private int bitLength;
	private int securityParameter;
	
	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RandomAdditiveMaskFactory randomAdditiveMaskFactory;
	private final MiscOIntGenerators miscOIntGenerator;
	private final InnerProductFactory innerProductFactory;

	// Variables used for calculation
	private int round = 0;
	private SInt[] rExpansion;
	private SInt rTop, rBottom;
	private OInt mOpen;
	private ProtocolProducer gp;

	public RightShiftProtocolImpl(SInt x, SInt result, int bitLength, int securityParameter,
			BasicNumericFactory basicNumericFactory, 
			RandomAdditiveMaskFactory randomAdditiveMaskFactory, 
			MiscOIntGenerators miscOIntGenerators, 
			InnerProductFactory innerProductFactory) {
		
		this.x = x;
		this.result = result;
		this.bitLength = bitLength;
		this.securityParameter = securityParameter;
		
		this.basicNumericFactory = basicNumericFactory;
		this.randomAdditiveMaskFactory = randomAdditiveMaskFactory;
		this.miscOIntGenerator = miscOIntGenerators;
		this.innerProductFactory = innerProductFactory;
	}

	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {

			switch (round) {
			case 0:
				// Load random r including binary expansion
				SInt rOutput = basicNumericFactory.getSInt();
				Protocol additiveMaskProtocol = randomAdditiveMaskFactory.getRandomAdditiveMaskCircuit(bitLength,
						securityParameter, rOutput); // TODO: It seems the r we get here is wrong, so we need to calculate it in next round 
				rExpansion = (SInt[]) additiveMaskProtocol.getOutputValues();
				gp = additiveMaskProtocol;
				break;

			case 1:

				// rBottom is the least significant bit of r
				rBottom = rExpansion[0];
				
				// Calculate 1, 2, 2^2, ..., 2^{bitLength - 2}
				int l = rExpansion.length - 2;
				OInt[] twoPowers = miscOIntGenerator.getTwoPowers(l);
				
				// Calculate rTop = (r - rBottom) >> 1
				rTop = basicNumericFactory.getSInt();
				SInt[] rTopBits = new SInt[l];
				System.arraycopy(rExpansion, 1, rTopBits, 0, l);
				Protocol findRTop = innerProductFactory.getInnerProductCircuit(rTopBits, twoPowers, rTop);

				// r = 2 * rTop + rBottom
				SInt r = basicNumericFactory.getSInt();
				SInt tmp = basicNumericFactory.getSInt();
				Protocol twoTimesRTop = basicNumericFactory.getMultCircuit(basicNumericFactory.getOInt(BigInteger.valueOf(2)), rTop, tmp);
				Protocol addRBottom = basicNumericFactory.getAddProtocol(tmp, rBottom, r);
				
				// mOpen = open(x + r)
				SInt mClosed = basicNumericFactory.getSInt();
				mOpen = basicNumericFactory.getOInt();
				Protocol addR = basicNumericFactory.getAddProtocol(x, r, mClosed);
				OpenIntProtocol openAddMask = basicNumericFactory.getOpenProtocol(mClosed, mOpen);
				
				gp = new SequentialProtocolProducer(findRTop, twoTimesRTop, addRBottom, addR, openAddMask);
				break;

			case 2:
				// 'carry' is either 0 or 1. It is 1 if and only if the addition
				// m = x + r gave a carry from the first (least significant) bit
				// to the second, ie. if the first bit of both x and r is 1.
				// This happens if and only if the first bit of r is 1 and the
				// first bit of m is 0 which in turn is equal to r * (m + 1 (mod 2)).
				SInt carry = basicNumericFactory.getSInt();
				OInt mBottomNegated = basicNumericFactory
						.getOInt(mOpen.getValue().add(BigInteger.ONE).mod(BigInteger.valueOf(2)));
				Protocol calculateCarryCircuit = basicNumericFactory.getMultCircuit(mBottomNegated, rBottom, carry);

				// Now we calculate result = x >> 1 = mTop - rTop - carry
				SInt mTopMinusRTop = basicNumericFactory.getSInt();
				OInt mTop = basicNumericFactory.getOInt(mOpen.getValue().shiftRight(1));
				Protocol subtractCircuit = basicNumericFactory.getSubtractCircuit(mTop, rTop, mTopMinusRTop);
				Protocol addCarryCircuit = basicNumericFactory.getSubtractCircuit(mTopMinusRTop, carry, result);

				gp = new SequentialProtocolProducer(
						new ParallelProtocolProducer(calculateCarryCircuit, subtractCircuit), 
						addCarryCircuit);

			default:
				// ...
			}
		}
		if (gp.hasNextProtocols()) {
			pos = gp.getNextProtocols(gates, pos);
		} else if (!gp.hasNextProtocols()) {
			round++;
			gp = null;
		}
		return pos;
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
	public boolean hasNextProtocols() {
		return round < 3;
	}

}
