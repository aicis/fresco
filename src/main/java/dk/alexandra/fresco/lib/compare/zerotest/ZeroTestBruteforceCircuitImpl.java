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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.AddByConstantProtocolFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultByConstantFactory;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.add.IncrementByOneCircuitFactory;
import dk.alexandra.fresco.lib.math.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.linalg.InnerProductFactory;

public class ZeroTestBruteforceCircuitImpl implements ZeroTestBruteforceCircuit {

	// maximal input
	private final int maxInput;

	// I/O-storage
	private final SInt input, output;

	// providers
	private final BasicNumericFactory provider;
	private final MultByConstantFactory mbcProvider;
	private final ExpFromOIntFactory expFromOIntProvider;
	private final MiscOIntGenerators miscOIntGenerator;
	private final InnerProductFactory innerProdProvider;
	private final AddByConstantProtocolFactory abcProvider;
	private final PreprocessedExpPipeFactory expProvider;
	private final IncrementByOneCircuitFactory incrProvider;

	// local stuff
	private static final int numRounds = 2;
	private int round=0;
	private ProtocolProducer gp = null;
	private OInt masked_O;
	private SInt[] R;
	
	public ZeroTestBruteforceCircuitImpl(int maxInput, SInt input, SInt output,
			BasicNumericFactory provider, 
			ExpFromOIntFactory expFromOIntProvider,
			MiscOIntGenerators miscOIntGenerator,
			InnerProductFactory innerProdProvider,
			PreprocessedExpPipeFactory expProvider,
			IncrementByOneCircuitFactory incrProvider) {
		this.maxInput = maxInput;
		this.input = input;
		this.output = output;
		this.provider = provider;
		this.mbcProvider = provider;
		this.expFromOIntProvider = expFromOIntProvider;
		this.miscOIntGenerator = miscOIntGenerator;
		this.innerProdProvider = innerProdProvider;
		this.expProvider = expProvider;
		
		this.abcProvider = provider;
		this.incrProvider = incrProvider;
		
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
			switch (round){
			case 0:
				// load rand, addOne, mult and unmask
				R = expProvider.getExponentiationPipe();

				SInt increased = provider.getSInt();
				SInt masked_S = provider.getSInt();
				
				masked_O = provider.getOInt();
				ProtocolProducer incrCircuit = incrProvider.getIncrementByOneCircuit(input, increased);
				MultProtocol multCircuit = provider.getMultCircuit(increased, R[0], masked_S);
				OpenIntProtocol openCircuit = provider.getOpenCircuit(masked_S, masked_O);
				
				gp = new SequentialProtocolProducer(incrCircuit, multCircuit, openCircuit);

				break;
			case 1:
				// compute powers and evaluate polynomial
				OInt[] maskedPowers = expFromOIntProvider.getExpFromOInt(masked_O, maxInput);

				ProtocolProducer[] unmaskGPs = new ProtocolProducer[maxInput];
				SInt[] powers = new SInt[maxInput];
				for (int i=0; i<maxInput; i++) {
					powers[i] = provider.getSInt();
					unmaskGPs[i] = mbcProvider.getMultCircuit(maskedPowers[i], R[i+1], powers[i]);
				}
				OInt[] polynomialCoefficients = miscOIntGenerator.getPoly(maxInput);
				
				OInt[] mostSignificantPolynomialCoefficients = new OInt[maxInput];
				System.arraycopy(polynomialCoefficients, 1, 
						mostSignificantPolynomialCoefficients, 0, maxInput);
				SInt tmp = provider.getSInt();
				ProtocolProducer polynomialGP = innerProdProvider.getInnerProductCircuit(powers, 
						mostSignificantPolynomialCoefficients, tmp);
				ProtocolProducer addCircuit = abcProvider.getAddCircuit(tmp, 
						polynomialCoefficients[0], output);
				gp = new SequentialProtocolProducer(new ParallelProtocolProducer(unmaskGPs), 
						polynomialGP, addCircuit);
				break;
			default:
				// TODO: handle bad stuff
			}
		}
		if (gp.hasNextProtocols()){
			pos = gp.getNextProtocols(gates, pos);
		}
		else if (!gp.hasNextProtocols()){
			round++;
			gp = null;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return round < numRounds;
	}
}
