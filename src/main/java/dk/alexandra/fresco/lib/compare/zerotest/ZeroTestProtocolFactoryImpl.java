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

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.HammingDistanceFactory;
import dk.alexandra.fresco.lib.math.HammingDistanceFactoryImpl;
import dk.alexandra.fresco.lib.math.NumericNegateBitFactory;
import dk.alexandra.fresco.lib.math.PreprocessedNumericBitFactory;
import dk.alexandra.fresco.lib.math.add.IncrementByOneCircuitFactory;
import dk.alexandra.fresco.lib.math.add.IncrementByOneCircuitFactoryImpl;
import dk.alexandra.fresco.lib.math.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.linalg.InnerProductFactoryImpl;

public class ZeroTestProtocolFactoryImpl implements ZeroTestProtocolFactory {

	private final BasicNumericFactory bnf;
	private final MiscOIntGenerators miscOIntGenerator;
	private final InnerProductFactory ipf;
	private final ExpFromOIntFactory expFromOIntFactory;
	private final PreprocessedExpPipeFactory expFactory;
	private final IncrementByOneCircuitFactory incFactory;
	private final RandomAdditiveMaskFactory maskFactory;
	private final HammingDistanceFactory hammingFactory;

	public ZeroTestProtocolFactoryImpl(BasicNumericFactory bnf,
			ExpFromOIntFactory expFromOIntFactory,
			PreprocessedNumericBitFactory numericBitFactory,
			NumericNegateBitFactory numericNegateBitFactory,
			PreprocessedExpPipeFactory expFactory) {
		this.bnf = bnf;
		this.miscOIntGenerator = new MiscOIntGenerators(bnf);
		this.ipf = new InnerProductFactoryImpl(bnf);
		this.incFactory = new IncrementByOneCircuitFactoryImpl(bnf);
		this.expFromOIntFactory = expFromOIntFactory;
		this.expFactory = expFactory;
		this.maskFactory = new RandomAdditiveMaskFactoryImpl(bnf,
				numericBitFactory);
		this.hammingFactory = new HammingDistanceFactoryImpl(bnf,
				numericNegateBitFactory);
	}

	@Override
	public ZeroTestBruteforceCircuit getZeroTestBruteforceCircuit(int maxInput,
			SInt input, SInt output) {
		return new ZeroTestBruteforceCircuitImpl(maxInput, input, output, bnf,
				expFromOIntFactory, miscOIntGenerator, ipf, expFactory,
				incFactory);
	}

	@Override
	public ZeroTestReducerCircuit getZeroTestReducerCircuit(int bitLength,
			int securityParameter, SInt input, SInt output) {
		return new ZeroTestReducerCircuitImpl(bitLength, securityParameter,
				input, output, maskFactory, bnf, hammingFactory);
	}

	@Override
	public ZeroTestProtocol getZeroCircuit(int bitLength,
			int securityParameter, SInt input, SInt output) {
		return new ZeroTestCircuitImpl(bitLength, securityParameter, input,
				output, this, this, bnf);
	}

}
