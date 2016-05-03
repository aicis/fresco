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
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskCircuit;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;

public class RightShiftProtocolImpl implements RightShiftProtocol {
	
	// Input
	private SInt input;
	private SInt result;
	private SInt remainder;
	private int bitLength;
	private int securityParameter;
	
	// Factories
	private final BasicNumericFactory basicNumericFactory;
	private final RandomAdditiveMaskFactory randomAdditiveMaskFactory;
	private final LocalInversionFactory localInversionFactory;

	// Variables used for calculation
	private int round = 0;
	private SInt r;
	private SInt[] rExpansion;
	private SInt rTop, rBottom;
	private OInt mOpen;
	private ProtocolProducer gp;
	
	/**
	 * 
	 * @param input The input.
	 * @param result The input shifted one bit to the right, input >> 1.
	 * @param bitLength An upper bound for the bitLength of the input.
	 * @param securityParameter The security parameter used in {@link RandomAdditiveMaskCircuit}. Leakage of a bit with propability at most 2<sup>-<code>securityParameter</code></sup>.
	 * @param basicNumericFactory
	 * @param randomAdditiveMaskFactory
	 * @param localInversionFactory
	 */
	public RightShiftProtocolImpl(SInt input, SInt result, int bitLength, int securityParameter, BasicNumericFactory basicNumericFactory,
			RandomAdditiveMaskFactory randomAdditiveMaskFactory, LocalInversionFactory localInversionFactory) {
		
		this.input = input;
		this.result = result;
		this.remainder = null;
		this.bitLength = bitLength;
		this.securityParameter = securityParameter;
		
		this.basicNumericFactory = basicNumericFactory;
		this.randomAdditiveMaskFactory = randomAdditiveMaskFactory;
		this.localInversionFactory = localInversionFactory;
	}

	/**
	 * 
	 * @param input The input.
	 * @param result The input shifted one bit to the right, input >> 1.
	 * @param remainder The least significant bit of the input (aka the bit that is thrown away in the shift).
	 * @param bitLength An upper bound for the bitLength of the input.
 	 * @param securityParameter The security parameter used in {@link RandomAdditiveMaskCircuit}. Leakage of a bit with propability at most 2<sup>-<code>securityParameter</code></sup>.
	 * @param basicNumericFactory
	 * @param randomAdditiveMaskFactory
	 * @param localInversionFactory
	 */
	public RightShiftProtocolImpl(SInt input, SInt result, SInt remainder, 
			int bitLength, int securityParameter, BasicNumericFactory basicNumericFactory,
			RandomAdditiveMaskFactory randomAdditiveMaskFactory, LocalInversionFactory localInversionFactory) {

		this(input, result, bitLength, securityParameter, basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
		this.remainder = remainder;
	}
	
	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {

			switch (round) {
			case 0:
				// Load random r including binary expansion
				r = basicNumericFactory.getSInt();
				rExpansion = new SInt[bitLength];
				for (int i = 0; i < rExpansion.length; i++) {
					rExpansion[i] = basicNumericFactory.getSInt();
				}
				Protocol additiveMaskProtocol = randomAdditiveMaskFactory.getRandomAdditiveMaskCircuit(securityParameter, rExpansion, r);
				gp = additiveMaskProtocol;
				break;

			case 1:
				
				// rBottom is the least significant bit of r
				rBottom = rExpansion[0];
				
				/* Calculate rTop = (r - rBottom) * 2^{-1}. Note that r - rBottom must be even and the division in the field are equivalent of a division in the integers. */
				NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);
				OInt inverseOfTwo = basicNumericFactory.getOInt();
				builder.addGateProducer(localInversionFactory.getLocalInversionCircuit(basicNumericFactory.getOInt(BigInteger.valueOf(2)), inverseOfTwo));
				rTop = builder.mult(inverseOfTwo, builder.sub(r, rBottom));
				
				// mOpen = open(x + r)
				SInt mClosed = basicNumericFactory.getSInt();
				mOpen = basicNumericFactory.getOInt();
				Protocol addR = basicNumericFactory.getAddProtocol(input, r, mClosed);
				OpenIntProtocol openAddMask = basicNumericFactory.getOpenProtocol(mClosed, mOpen);
				
				gp = new SequentialProtocolProducer(builder.getCircuit(), addR, openAddMask);
				break;

			case 2:
				/* 
				 * 'carry' is either 0 or 1. It is 1 if and only if the addition
				 * m = x + r gave a carry from the first (least significant) bit
				 * to the second, ie. if the first bit of both x and r is 1.
				 * This happens if and only if the first bit of r is 1 and the
				 * first bit of m is 0 which in turn is equal to r_0 * (m + 1 (mod 2)).
				 */
				SInt carry = basicNumericFactory.getSInt();
				OInt mBottomNegated = basicNumericFactory
						.getOInt(mOpen.getValue().add(BigInteger.ONE).mod(BigInteger.valueOf(2)));
				Protocol calculateCarry = basicNumericFactory.getMultCircuit(mBottomNegated, rBottom, carry);

				// The carry is needed by both the calculation of the shift and the remainder
				SequentialProtocolProducer protocol = new SequentialProtocolProducer(calculateCarry);

				// The shift and the remainder can be calculated in parallel
				ParallelProtocolProducer findShiftAndRemainder = new ParallelProtocolProducer();
				
				// Now we calculate the shift, x >> 1 = mTop - rTop - carry
				SInt mTopMinusRTop = basicNumericFactory.getSInt();
				OInt mTop = basicNumericFactory.getOInt(mOpen.getValue().shiftRight(1));
				Protocol subtractCircuit = basicNumericFactory.getSubtractCircuit(mTop, rTop, mTopMinusRTop);
				Protocol addCarryCircuit = basicNumericFactory.getSubtractCircuit(mTopMinusRTop, carry, result);
				SequentialProtocolProducer calculateShift = new SequentialProtocolProducer(subtractCircuit, addCarryCircuit);

				findShiftAndRemainder.append(calculateShift);
				
				if (remainder != null) {
					/* We also need to calculate the remainder, aka. the bit we throw away in the shift: 
					 * x (mod 2) = xor(r_0, m mod 2) = r_0 + (m mod 2) - 2 (r_0 * (m mod 2)).
					 */
					OInt mBottom = basicNumericFactory.getOInt(mOpen.getValue().mod(BigInteger.valueOf(2)));
					OInt twoMBottom = basicNumericFactory.getOInt(mBottom.getValue().shiftLeft(1)); 
					SInt product = basicNumericFactory.getSInt();
					SInt sum = basicNumericFactory.getSInt();
					Protocol remainderProtocolMult = basicNumericFactory.getMultCircuit(twoMBottom, rBottom, product);
					Protocol remainderProtocolAdd = basicNumericFactory.getAddProtocol(rBottom, mBottom, sum);
					Protocol remainderProtodolSubtract = basicNumericFactory.getSubtractCircuit(sum, product, remainder);
					
					SequentialProtocolProducer calculateRemainder = new SequentialProtocolProducer(
							new ParallelProtocolProducer(remainderProtocolMult, remainderProtocolAdd),
							remainderProtodolSubtract);
					
					findShiftAndRemainder.append(calculateRemainder);
				}
				
				protocol.append(findShiftAndRemainder);
				gp = protocol;

				break;
				
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
		return new Value[] {input};
	}

	@Override
	public Value[] getOutputValues() {
		if (remainder != null) {
			return new Value[] {result, remainder};
		} else {
			return new Value[] {result};
		}
	}

	@Override
	public boolean hasNextProtocols() {
		return round < 3;
	}

}
