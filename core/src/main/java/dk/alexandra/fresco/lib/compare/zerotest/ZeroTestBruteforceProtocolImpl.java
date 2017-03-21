/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
import dk.alexandra.fresco.lib.math.bool.add.IncrementByOneProtocolFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;

public class ZeroTestBruteforceProtocolImpl implements ZeroTestBruteforceProtocol {

	// maximal input
	private final int maxInput;

	// I/O-storage
	private final SInt input, output;

	// Factories
	private final BasicNumericFactory factory;
	private final MultByConstantFactory mbcFactory;
	private final ExpFromOIntFactory expFromOIntFactory;
	private final MiscOIntGenerators miscOIntGenerator;
	private final InnerProductFactory innerProdFactory;
	private final AddByConstantProtocolFactory abcFactory;
	private final PreprocessedExpPipeFactory expFactory;
	private final IncrementByOneProtocolFactory incrFactory;

	// local stuff
	private static final int numRounds = 2;
	private int round=0;
	private ProtocolProducer pp = null;
	private OInt masked_O;
	private SInt[] R;
	
	public ZeroTestBruteforceProtocolImpl(int maxInput, SInt input, SInt output,
			BasicNumericFactory factory, 
			ExpFromOIntFactory expFromOIntFactory,
			MiscOIntGenerators miscOIntGenerator,
			InnerProductFactory innerProdFactory,
			PreprocessedExpPipeFactory expFactory,
			IncrementByOneProtocolFactory incrFactory) {
		this.maxInput = maxInput;
		this.input = input;
		this.output = output;
		this.factory = factory;
		this.mbcFactory = factory;
		this.expFromOIntFactory = expFromOIntFactory;
		this.miscOIntGenerator = miscOIntGenerator;
		this.innerProdFactory = innerProdFactory;
		this.expFactory = expFactory;
		
		this.abcFactory = factory;
		this.incrFactory = incrFactory;
		
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
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if (pp == null){
			switch (round){
			case 0:
				// load rand, addOne, mult and unmask
				R = expFactory.getExponentiationPipe();

				SInt increased = factory.getSInt();
				SInt masked_S = factory.getSInt();
				
				masked_O = factory.getOInt();
				ProtocolProducer incr = incrFactory.getIncrementByOneProtocol(input, increased);
				MultProtocol mult = factory.getMultProtocol(increased, R[0], masked_S);
				OpenIntProtocol open = factory.getOpenProtocol(masked_S, masked_O);
				
				pp = new SequentialProtocolProducer(incr, mult, open);

				break;
			case 1:
				// compute powers and evaluate polynomial
				OInt[] maskedPowers = expFromOIntFactory.getExpFromOInt(masked_O, maxInput);

				ProtocolProducer[] unmaskGPs = new ProtocolProducer[maxInput];
				SInt[] powers = new SInt[maxInput];
				for (int i=0; i<maxInput; i++) {
					powers[i] = factory.getSInt();
					unmaskGPs[i] = mbcFactory.getMultProtocol(maskedPowers[i], R[i+1], powers[i]);
				}
				OInt[] polynomialCoefficients = miscOIntGenerator.getPoly(maxInput);
				
				OInt[] mostSignificantPolynomialCoefficients = new OInt[maxInput];
				System.arraycopy(polynomialCoefficients, 1, 
						mostSignificantPolynomialCoefficients, 0, maxInput);
				SInt tmp = factory.getSInt();
				ProtocolProducer polynomialGP = innerProdFactory.getInnerProductProtocol(powers, 
						mostSignificantPolynomialCoefficients, tmp);
				ProtocolProducer add = abcFactory.getAddProtocol(tmp, 
						polynomialCoefficients[0], output);
				pp = new SequentialProtocolProducer(new ParallelProtocolProducer(unmaskGPs), 
						polynomialGP, add);
				break;
			default:
				// TODO: handle bad stuff
			}
		}
		if (pp.hasNextProtocols()){
			pos = pp.getNextProtocols(nativeProtocols, pos);
		}
		else if (!pp.hasNextProtocols()){
			round++;
			pp = null;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return round < numRounds;
	}
}
