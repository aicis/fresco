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
package dk.alexandra.fresco.lib.compare.gt;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.ConditionalSelectCircuitImpl;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.AddByConstantProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultByConstantFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractCircuitFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.NumericNegateBitFactory;
import dk.alexandra.fresco.lib.math.inv.LocalInversionFactory;
import dk.alexandra.fresco.lib.math.linalg.InnerProductFactory;

public class GreaterThanReducerCircuitImpl implements GreaterThanCircuit {

	
	public GreaterThanReducerCircuitImpl(int bitLength, int securityParameter,
			SInt x, SInt y, SInt output, BasicNumericFactory provider,
			NumericNegateBitFactory bitProvider,
			RandomAdditiveMaskFactory maskProvider,
			ZeroTestProtocolFactory ztProvider,
			MiscOIntGenerators miscOIntGenerator,
			InnerProductFactory innerProdProvider,
			LocalInversionFactory invProvider) {
		super();
		this.bitLength = bitLength;
		this.bitLengthBottom = bitLength/2;
		this.bitLengthTop = bitLength - bitLengthBottom;
		
		this.securityParameter = securityParameter;
		this.x = x;
		this.y = y;
		this.output = output;
		this.provider = provider;
		this.bitProvider = bitProvider;
		this.abcProvider = provider;
		this.mbcProvider = provider;
		this.subProvider = provider;
		this.maskProvider = maskProvider;
		this.ztProvider = ztProvider;
		this.miscOIntGenerator = miscOIntGenerator;
		this.innerProdProvider = innerProdProvider;
		this.invProvider = invProvider;

		this.twoToBitLength = provider.getOInt();
		this.twoToBitLength.setValue(BigInteger.ONE.shiftLeft(this.bitLength));
		this.twoToBitLengthBottom = provider.getOInt();
		this.twoToBitLengthBottom.setValue(BigInteger.ONE.shiftLeft(this.bitLengthBottom));

		this.twoToNegBitLength = provider.getOInt();
	}

	// params etc
	private final int bitLength;
	private final int bitLengthTop; // == bitlength/2
	private final int bitLengthBottom;
	private final int securityParameter;
	private final SInt x,y;
	private final SInt output;

	private final BasicNumericFactory provider;
	private final NumericNegateBitFactory bitProvider; 

	
	private final AddByConstantProtocolFactory abcProvider;
	private final MultByConstantFactory mbcProvider;

	private final SubtractCircuitFactory subProvider;
	
	private final RandomAdditiveMaskFactory maskProvider;
	private final ZeroTestProtocolFactory ztProvider;
	private final MiscOIntGenerators miscOIntGenerator;
	private final InnerProductFactory innerProdProvider;
	private final LocalInversionFactory invProvider;

	
	// local stuff
//	private final static int numRounds = 5;
	private final static int numRounds = 5;
	private int round=0;
	private ProtocolProducer gp;
	
	private SInt rValue, rTop, rBottom, rBar;
	private SInt[] r;
	private final OInt twoToBitLength;
	private final OInt twoToNegBitLength;
	private final OInt twoToBitLengthBottom;

	SInt z;
	
	private OInt mO, mBot, mTop, mBar;

	private SInt eqResult, subComparisonResult;
	private SInt mPrime, rPrime;
	
	
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
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {
			switch (round){
			case 0:
				// LOAD r + bits
				rValue = provider.getSInt();
				Protocol maskCircuit = maskProvider.getRandomAdditiveMaskCircuit(bitLength, securityParameter, rValue);
				r = (SInt[]) maskCircuit.getOutputValues();
				gp = maskCircuit;
				break;
			case 1:
				// construct r-values (rBar, rBottom, rTop) 
				rTop = provider.getSInt();
				rBottom = provider.getSInt();
				rBar = provider.getSInt();

				SInt tmp1 = provider.getSInt();
				SInt[] rTopBits = new SInt[bitLengthTop];
				SInt[] rBottomBits = new SInt[bitLengthBottom];
				
				System.arraycopy(r, 0, rBottomBits, 0, bitLengthBottom);
				System.arraycopy(r, bitLengthBottom, rTopBits, 0, bitLengthTop);
				
				OInt[] twoPowsTop, twoPowsBottom;

				twoPowsTop = twoPowsBottom = miscOIntGenerator.getTwoPowers(bitLengthBottom);
				// TODO: why should we have two of these; it'd be much more effective if inner-products could take different-length vectors (ignoring subsequent inputs/implicitly padding with 0)
				if (bitLengthTop != bitLengthBottom) {
					// bitLength is odd, i.e., twoPowsTop is wrong...
					twoPowsTop = miscOIntGenerator.getTwoPowers(bitLengthTop); 
				}

				Protocol sumBottom = innerProdProvider.getInnerProductCircuit(rBottomBits, twoPowsBottom, rBottom);
				Protocol sumTop = innerProdProvider.getInnerProductCircuit(rTopBits, twoPowsTop, rTop);

				Protocol shiftCirc0 = mbcProvider.getMultCircuit(twoToBitLengthBottom, rTop, tmp1);
				Protocol addCirc0 = provider.getAddProtocol(tmp1, rBottom, rBar);

				// forget bits of rValue
				r = null;
				
				// Actual work: mask and reveal 2^bitLength+x-y
				// z = 2^bitLength + x -y  
				SInt diff = provider.getSInt();
				z = provider.getSInt();
				
				Protocol subCircuit = provider.getSubtractCircuit(y, x, diff);
				Protocol addCircuit1 = abcProvider.getAddProtocol(diff, twoToBitLength, z);

				// mO = open(z + r)
				SInt mS = provider.getSInt();
				mO = provider.getOInt();
				Protocol addCircuit2 = provider.getAddProtocol(z, rValue, mS);
				OpenIntProtocol openCircuitAddMask = provider.getOpenProtocol(mS, mO);

				gp  = new SequentialProtocolProducer(
						new ParallelProtocolProducer(sumBottom, sumTop), 
						shiftCirc0, 
						addCirc0, 
						subCircuit, 
						addCircuit1, 
						addCircuit2, 
						openCircuitAddMask);
				break;
			case 2: // 
				// extract mTop and mBot
				mBot = provider.getOInt();
				mTop = provider.getOInt();
				mBar = provider.getOInt();
				compute_mTopmBot(mO, mTop, mBot, mBar);
				// mO = null;
				
				
				// dif = mTop - rTop
				SInt dif = provider.getSInt();
				Protocol subCirc = provider.getSubtractCircuit(mTop, rTop, dif);
				
				
				// eqResult <- execute eq.test
				eqResult = provider.getSInt();
				Protocol eqCirc = ztProvider.getZeroCircuit(bitLengthTop, securityParameter, dif, eqResult);
				gp = new SequentialProtocolProducer(subCirc, eqCirc);
				break;
			case 3:
				// [eqResult]? BOT : TOP (for m and r) (store as mPrime,rPrime)
				rPrime = provider.getSInt();
				mPrime = provider.getSInt();

				// TODO: get a provider to handle this.....
				Protocol selectrPrime = new ConditionalSelectCircuitImpl(eqResult, rBottom, rTop, rPrime, provider);
				// TODO: get a conditional selector for public values...
				SInt negEqResult =  provider.getSInt();
				Protocol negCirc = bitProvider.getNegatedBitCircuit(eqResult, negEqResult);
				SInt prod1 = provider.getSInt();
				SInt prod2 = provider.getSInt();
				Protocol mult1 = mbcProvider.getMultCircuit(mBot, eqResult, prod1);
				Protocol mult2 = mbcProvider.getMultCircuit(mTop, negEqResult, prod2);
				Protocol sumCirc = provider.getAddProtocol(prod1, prod2, mPrime);
				ProtocolProducer selectmPrime = new SequentialProtocolProducer(negCirc, mult1, mult2, sumCirc);

				// subComparisonResult = Compare(rPrime,mPrime)

				ProtocolProducer selectSubProblemGP = new ParallelProtocolProducer(selectmPrime, selectrPrime);

				subComparisonResult = provider.getSInt();
				if (bitLength == 2) {
					// sub comparison is of length 1: mPrime >= rPrime: 
					// NOT (rPrime AND NOT mPrime)
					SInt mPrimeNegated = provider.getSInt();
					Protocol negCirc3_1 = bitProvider.getNegatedBitCircuit(mPrime, mPrimeNegated);
					
					SInt rPrimeStrictlyGTmPrime = provider.getSInt();
					Protocol multCirc3 = provider.getMultCircuit(mPrimeNegated, rPrime, rPrimeStrictlyGTmPrime);
					Protocol negCirc3_2 = bitProvider.getNegatedBitCircuit(rPrimeStrictlyGTmPrime, subComparisonResult);
					gp = new SequentialProtocolProducer(selectSubProblemGP, negCirc3_1, multCirc3, negCirc3_2);
				} else {
					// compare the half-length inputs
					int nextBitLength = (bitLength+1)/2;
					Protocol compCirc = new GreaterThanReducerCircuitImpl(
							nextBitLength, 
							securityParameter, 
							rPrime, 
							mPrime, 
							subComparisonResult, 
							provider, 
							bitProvider, 
							maskProvider, 
							ztProvider, 
							miscOIntGenerator, 
							innerProdProvider, 
							invProvider); 
					gp = new SequentialProtocolProducer(selectSubProblemGP, compCirc);
				}
				
				break;
			case 4:
				// u = 1 - subComparisonResult
				SInt u = provider.getSInt();
				Protocol negCirc4 = bitProvider.getNegatedBitCircuit(subComparisonResult, u);
				
				// res = z - ((m mod 2^bitLength) - (r mod 2^bitlength) + u*2^bitLength)
				SInt reducedWithError = provider.getSInt();
				SInt reducedNoError = provider.getSInt();
				SInt additiveError = provider.getSInt();
				SInt resUnshifted = provider.getSInt();

				Protocol subCirc4_1 =  subProvider.getSubtractCircuit(mBar, rBar, reducedWithError);
				Protocol mbcCirc4 = mbcProvider.getMultCircuit(twoToBitLength, u, additiveError);
				Protocol addCirc4 = provider.getAddProtocol(additiveError, reducedWithError, reducedNoError);

				Protocol subCirc4_2 = provider.getSubtractCircuit(z, reducedNoError, resUnshifted);
				// res >> 2^bitLength
				
				Protocol localInvCirc4 = invProvider.getLocalInversionCircuit(twoToBitLength, twoToNegBitLength);
				Protocol shiftCirc4 = mbcProvider.getMultCircuit(twoToNegBitLength, resUnshifted, output);
				
				ProtocolProducer computeUnshifted = new SequentialProtocolProducer(
						new ParallelProtocolProducer(negCirc4, subCirc4_1), 
						mbcCirc4, addCirc4, subCirc4_2);
				ProtocolProducer computeTwoToNeg = new ParallelProtocolProducer(computeUnshifted,localInvCirc4);
				gp = new SequentialProtocolProducer(computeTwoToNeg, shiftCirc4);
				//gp = new SequentialGateProducer(negCirc4, subCirc4_1, mbcCirc4, addCirc4, subCirc4_2, localInvCirc4, shiftCirc4);				
				break;
			default:
				// TODO: handle bad stuff if we ever get here
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

	private void compute_mTopmBot(OInt m, OInt mTop, OInt mBot, OInt mBar) {
		// TODO: put const generators into miscOIntGenerators and use them
		BigInteger mMod = m.getValue().mod(BigInteger.ONE.shiftLeft(bitLength));

		mBar.setValue(mMod);
		mBot.setValue(mMod.mod(BigInteger.ONE.shiftLeft(bitLengthBottom)));
		mTop.setValue(mMod.shiftRight(bitLengthBottom));
	}

	@Override
	public boolean hasNextProtocols() {
		return round < numRounds;
	}
}
