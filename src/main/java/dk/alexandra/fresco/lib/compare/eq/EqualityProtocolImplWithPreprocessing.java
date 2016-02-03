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
package dk.alexandra.fresco.lib.compare.eq;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.field.integer.AddProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultByConstantFactory;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractCircuit;
import dk.alexandra.fresco.lib.field.integer.generic.AddProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.HammingDistanceFactory;
import dk.alexandra.fresco.lib.math.PreprocessedNumericBitFactory;
import dk.alexandra.fresco.lib.math.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.linalg.InnerProductFactory;

/**
 * @author ttoft
 *
 */
public class EqualityProtocolImplWithPreprocessing implements EqualityProtocol {

	// parameters
	private final int bitLength, securityParam;

	// variables for input/output
	private final SInt x, y, result;

	// providers
	private final BasicNumericFactory provider;
	private final MultByConstantFactory mbcProvider;
//	private final NumericBitProvider bitProvider; 
	private final PreprocessedExpPipeFactory expProvider;
//	private final AddCircuitProvider addProvider;
	private final InnerProductFactory innerProdProvider;
	private final ExpFromOIntFactory expFromOIntProvider;
	private final HammingDistanceFactory hammingProvider;	
	private final RandomAdditiveMaskFactory randomAddMaskProvider;
	
	private final MiscOIntGenerators miscOIntGenerator;

//	private final OInt[] twoPows;
	
	private ProtocolProducer gp;
	boolean done;

//	public EqualityCircuitImplWithPreprocessing(int bitLength, int securityParam,
//			SInt x, SInt y, SInt result,
//BasicNumericProvider provider, MultByConstantProvider mbcProvider, NumericBitProvider bitProvider,
//ExponentiationProvider expProvider, AddCircuitProvider addProvider,
//InnerProductProvider innerProdProvider, MiscOIntGenerators miscOIntGenerator,
//ExpFromOIntProvider expFromOIntProvider, HammingDistanceProvider hammingProvider,
//RandomAdditiveMaskProvider randomAddMaskProvider) {
		public EqualityProtocolImplWithPreprocessing(int bitLength, int securityParam,
				SInt x, SInt y, SInt result,
BasicNumericFactory provider, MultByConstantFactory mbcProvider, PreprocessedNumericBitFactory bitProvider,
PreprocessedExpPipeFactory expProvider, AddProtocolFactory addProvider,
InnerProductFactory innerProdProvider, MiscOIntGenerators miscOIntGenerator,
ExpFromOIntFactory expFromOIntProvider) {
		this.bitLength = bitLength;
		this.securityParam = securityParam;
		this.x = x;
		this.y = y;
		this.result = result;
		this.provider = provider;
		this.mbcProvider = mbcProvider;
//		this.bitProvider = bitProvider;
		this.expProvider = expProvider;
//		this.addProvider = addProvider;
		this.innerProdProvider = innerProdProvider;
		this.expFromOIntProvider = expFromOIntProvider;
		this.hammingProvider = null; // hammingProvider;
		this.randomAddMaskProvider = null; //randomAddMaskProvider;
		
		// TODO: should we generate it here, or have a global one? Probably have a global one...
		this.miscOIntGenerator = miscOIntGenerator;
		
		this.gp = null;
		this.done = false;

//		this.twoPows = miscOIntGenerator.getTwoPowers(securityParam+ bitLength);		
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
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null){
			ProtocolProducer problemReducerGP, EqSolverGP;
			SInt reducedProblem = provider.getSInt();
			
			problemReducerGP = reduceProblemSize(reducedProblem);

			EqSolverGP = reducedEqSolverGP(reducedProblem);
			
			this.gp = new SequentialProtocolProducer(problemReducerGP, EqSolverGP);
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


	private SInt[] loadRandomMultiplicativeMask() {
		// R[0] = r^{-1}
		// R[i] = R^i
		SInt[] R = expProvider.getExponentiationPipe();
		return R;
	}

	private ProtocolProducer reduceProblemSize(SInt reducedProblem) {
		// load random r and bits of r mod 2^length
		SInt[] r = new SInt[bitLength+1];
		SInt rValue = provider.getSInt();
		Protocol randLoader = randomAddMaskProvider.getRandomAdditiveMaskCircuit(bitLength, securityParam, rValue);

		// mask and reveal difference
		SInt subResult = provider.getSInt();
		SInt masked_S = provider.getSInt();
		OInt masked_O = provider.getOInt();

		SubtractCircuit subCircuit = provider.getSubtractCircuit(x, y,
				subResult);
		AddProtocol addCircuit = provider.getAddCircuit(subResult, r[bitLength],
				masked_S);
		OpenIntProtocol openCircuitAddMask = provider
				.getOpenProtocol(masked_S, masked_O);


		// Compute Hamming distance
		Protocol hammingCircuit = hammingProvider.getHammingdistanceCircuit(r, masked_O, reducedProblem);

		return  new SequentialProtocolProducer(randLoader, subCircuit, addCircuit, openCircuitAddMask, hammingCircuit);
	}

	
	/**
	 * @param reducedProblem equalToZero-input where the value is at most bitLength
	 * @return SInt stating if the input was zero (1) or non-zero (0)
	 */
	private ProtocolProducer reducedEqSolverGP(SInt reducedProblem) {
		// load random R and relevant powers
		SInt[] R = loadRandomMultiplicativeMask();
		
	
		// mask (multiplicatively) and reveal
		SInt masked_S = provider.getSInt();
		OInt masked_O = provider.getOInt();

		MultProtocol multCircuit = provider.getMultCircuit(reducedProblem, R[0], masked_S);
		OpenIntProtocol openCircuit = provider.getOpenProtocol(masked_S, masked_O);		
		
		
		// compute powers and evaluate polynomial
		OInt[] maskedPowers = expFromOIntProvider.getExpFromOInt(masked_O, bitLength);

		ProtocolProducer[] unmaskGPs = new ProtocolProducer[bitLength];
		SInt[] powers = new SInt[bitLength];
		for (int i=0; i<bitLength; i++) {
			powers[i] = provider.getSInt();
			unmaskGPs[i] = mbcProvider.getMultCircuit(maskedPowers[i], R[i], powers[i]);
		}

		OInt[] polynomialCoefficients = miscOIntGenerator.getPoly(bitLength);
		ProtocolProducer polynomialGP = innerProdProvider.getInnerProductCircuit(powers, polynomialCoefficients, this.result); 

		return new SequentialProtocolProducer(multCircuit, openCircuit, new ParallelProtocolProducer(unmaskGPs), polynomialGP);
	}

}
