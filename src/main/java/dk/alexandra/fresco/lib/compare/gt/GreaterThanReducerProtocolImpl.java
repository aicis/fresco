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
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocolImpl;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.AddByConstantProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultByConstantFactory;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;

public class GreaterThanReducerProtocolImpl implements GreaterThanProtocol {

	
	public GreaterThanReducerProtocolImpl(int bitLength, int securityParameter,
			SInt x, SInt y, SInt output, BasicNumericFactory factory,
			NumericNegateBitFactory bitFactory,
			RandomAdditiveMaskFactory maskFactory,
			ZeroTestProtocolFactory ztFactory,
			MiscOIntGenerators miscOIntGenerator,
			InnerProductFactory innerProdFactory,
			LocalInversionFactory invFactory) {
		super();
		this.bitLength = bitLength;
		this.bitLengthBottom = bitLength/2;
		this.bitLengthTop = bitLength - bitLengthBottom;
		
		this.securityParameter = securityParameter;
		this.x = x;
		this.y = y;
		this.output = output;
		this.factory = factory;
		this.bitFactory = bitFactory;
		this.abcFactory = factory;
		this.mbcFactory = factory;
		this.subFactory = factory;
		this.maskFactory = maskFactory;
		this.ztFactory = ztFactory;
		this.miscOIntGenerator = miscOIntGenerator;
		this.innerProdFactory = innerProdFactory;
		this.invFactory = invFactory;

		this.twoToBitLength = factory.getOInt();
		this.twoToBitLength.setValue(BigInteger.ONE.shiftLeft(this.bitLength));
		this.twoToBitLengthBottom = factory.getOInt();
		this.twoToBitLengthBottom.setValue(BigInteger.ONE.shiftLeft(this.bitLengthBottom));

		this.twoToNegBitLength = factory.getOInt();
	}

	// params etc
	private final int bitLength;
	private final int bitLengthTop; // == bitlength/2
	private final int bitLengthBottom;
	private final int securityParameter;
	private final SInt x,y;
	private final SInt output;

	private final BasicNumericFactory factory;
	private final NumericNegateBitFactory bitFactory; 

	
	private final AddByConstantProtocolFactory abcFactory;
	private final MultByConstantFactory mbcFactory;

	private final SubtractProtocolFactory subFactory;
	
	private final RandomAdditiveMaskFactory maskFactory;
	private final ZeroTestProtocolFactory ztFactory;
	private final MiscOIntGenerators miscOIntGenerator;
	private final InnerProductFactory innerProdFactory;
	private final LocalInversionFactory invFactory;

	
	// local stuff
//	private final static int numRounds = 5;
	private final static int numRounds = 5;
	private int round=0;
	private ProtocolProducer pp;
	
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
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if (pp == null) {
			switch (round){
			case 0:
				// LOAD r + bits
				rValue = factory.getSInt();
				Protocol mask = maskFactory.getRandomAdditiveMaskProtocol(bitLength, securityParameter, rValue);
				r = (SInt[]) mask.getOutputValues();
				pp = mask;
				break;
			case 1:
				// construct r-values (rBar, rBottom, rTop) 
				rTop = factory.getSInt();
				rBottom = factory.getSInt();
				rBar = factory.getSInt();

				SInt tmp1 = factory.getSInt();
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

				Protocol sumBottom = innerProdFactory.getInnerProductProtocol(rBottomBits, twoPowsBottom, rBottom);
				Protocol sumTop = innerProdFactory.getInnerProductProtocol(rTopBits, twoPowsTop, rTop);

				Protocol shiftCirc0 = mbcFactory.getMultProtocol(twoToBitLengthBottom, rTop, tmp1);
				Protocol addCirc0 = factory.getAddProtocol(tmp1, rBottom, rBar);

				// forget bits of rValue
				r = null;
				
				// Actual work: mask and reveal 2^bitLength+x-y
				// z = 2^bitLength + x -y  
				SInt diff = factory.getSInt();
				z = factory.getSInt();
				
				Protocol subprotocol = factory.getSubtractProtocol(y, x, diff);
				Protocol addprotocol1 = abcFactory.getAddProtocol(diff, twoToBitLength, z);

				// mO = open(z + r)
				SInt mS = factory.getSInt();
				mO = factory.getOInt();
				Protocol addprotocol2 = factory.getAddProtocol(z, rValue, mS);
				OpenIntProtocol openprotocolAddMask = factory.getOpenProtocol(mS, mO);

				pp  = new SequentialProtocolProducer(
						new ParallelProtocolProducer(sumBottom, sumTop), 
						shiftCirc0, 
						addCirc0, 
						subprotocol, 
						addprotocol1, 
						addprotocol2, 
						openprotocolAddMask);
				break;
			case 2: // 
				// extract mTop and mBot
				mBot = factory.getOInt();
				mTop = factory.getOInt();
				mBar = factory.getOInt();
				compute_mTopmBot(mO, mTop, mBot, mBar);
				// mO = null;
				
				
				// dif = mTop - rTop
				SInt dif = factory.getSInt();
				Protocol subCirc = factory.getSubtractProtocol(mTop, rTop, dif);
				
				
				// eqResult <- execute eq.test
				eqResult = factory.getSInt();
				Protocol eqCirc = ztFactory.getZeroProtocol(bitLengthTop, securityParameter, dif, eqResult);
				pp = new SequentialProtocolProducer(subCirc, eqCirc);
				break;
			case 3:
				// [eqResult]? BOT : TOP (for m and r) (store as mPrime,rPrime)
				rPrime = factory.getSInt();
				mPrime = factory.getSInt();

				// TODO: get a provider to handle this.....
				Protocol selectrPrime = new ConditionalSelectProtocolImpl(eqResult, rBottom, rTop, rPrime, factory);
				// TODO: get a conditional selector for public values...
				SInt negEqResult =  factory.getSInt();
				Protocol negCirc = bitFactory.getNegatedBitProtocol(eqResult, negEqResult);
				SInt prod1 = factory.getSInt();
				SInt prod2 = factory.getSInt();
				Protocol mult1 = mbcFactory.getMultProtocol(mBot, eqResult, prod1);
				Protocol mult2 = mbcFactory.getMultProtocol(mTop, negEqResult, prod2);
				Protocol sumCirc = factory.getAddProtocol(prod1, prod2, mPrime);
				ProtocolProducer selectmPrime = new SequentialProtocolProducer(negCirc, mult1, mult2, sumCirc);

				// subComparisonResult = Compare(rPrime,mPrime)

				ProtocolProducer selectSubProblemGP = new ParallelProtocolProducer(selectmPrime, selectrPrime);

				subComparisonResult = factory.getSInt();
				if (bitLength == 2) {
					// sub comparison is of length 1: mPrime >= rPrime: 
					// NOT (rPrime AND NOT mPrime)
					SInt mPrimeNegated = factory.getSInt();
					Protocol negCirc3_1 = bitFactory.getNegatedBitProtocol(mPrime, mPrimeNegated);
					
					SInt rPrimeStrictlyGTmPrime = factory.getSInt();
					Protocol multCirc3 = factory.getMultProtocol(mPrimeNegated, rPrime, rPrimeStrictlyGTmPrime);
					Protocol negCirc3_2 = bitFactory.getNegatedBitProtocol(rPrimeStrictlyGTmPrime, subComparisonResult);
					pp = new SequentialProtocolProducer(selectSubProblemGP, negCirc3_1, multCirc3, negCirc3_2);
				} else {
					// compare the half-length inputs
					int nextBitLength = (bitLength+1)/2;
					Protocol compCirc = new GreaterThanReducerProtocolImpl(
							nextBitLength, 
							securityParameter, 
							rPrime, 
							mPrime, 
							subComparisonResult, 
							factory, 
							bitFactory, 
							maskFactory, 
							ztFactory, 
							miscOIntGenerator, 
							innerProdFactory, 
							invFactory); 
					pp = new SequentialProtocolProducer(selectSubProblemGP, compCirc);
				}
				
				break;
			case 4:
				// u = 1 - subComparisonResult
				SInt u = factory.getSInt();
				Protocol negCirc4 = bitFactory.getNegatedBitProtocol(subComparisonResult, u);
				
				// res = z - ((m mod 2^bitLength) - (r mod 2^bitlength) + u*2^bitLength)
				SInt reducedWithError = factory.getSInt();
				SInt reducedNoError = factory.getSInt();
				SInt additiveError = factory.getSInt();
				SInt resUnshifted = factory.getSInt();

				Protocol subCirc4_1 =  subFactory.getSubtractProtocol(mBar, rBar, reducedWithError);
				Protocol mbcCirc4 = mbcFactory.getMultProtocol(twoToBitLength, u, additiveError);
				Protocol addCirc4 = factory.getAddProtocol(additiveError, reducedWithError, reducedNoError);

				Protocol subCirc4_2 = factory.getSubtractProtocol(z, reducedNoError, resUnshifted);
				// res >> 2^bitLength
				
				Protocol localInvCirc4 = invFactory.getLocalInversionProtocol(twoToBitLength, twoToNegBitLength);
				Protocol shiftCirc4 = mbcFactory.getMultProtocol(twoToNegBitLength, resUnshifted, output);
				
				ProtocolProducer computeUnshifted = new SequentialProtocolProducer(
						new ParallelProtocolProducer(negCirc4, subCirc4_1), 
						mbcCirc4, addCirc4, subCirc4_2);
				ProtocolProducer computeTwoToNeg = new ParallelProtocolProducer(computeUnshifted,localInvCirc4);
				pp = new SequentialProtocolProducer(computeTwoToNeg, shiftCirc4);
				//gp = new SequentialGateProducer(negCirc4, subCirc4_1, mbcCirc4, addCirc4, subCirc4_2, localInvCirc4, shiftCirc4);				
				break;
			default:
				// TODO: handle bad stuff if we ever get here
			}
		}
		if (pp.hasNextProtocols()) {
			pos = pp.getNextProtocols(nativeProtocols, pos);
		} else if (!pp.hasNextProtocols()) {
			round++;
			pp = null;
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
