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
package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocol;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocolImpl;
import dk.alexandra.fresco.lib.compare.gt.GreaterThanReducerCircuitImpl;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactory;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactory;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.PreprocessedNumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;

public class ComparisonProtocolFactoryImpl implements ComparisonProtocolFactory {

	private final BasicNumericFactory bnf;
	private final int secParam;
	private final LocalInversionFactory localInvFactory;
	private final NumericNegateBitFactory numericNegateBitFactory;
	private final RandomAdditiveMaskFactory randomAdditiveMaskFactory;
	private final InnerProductFactory innerProductFactory;
	private final ZeroTestProtocolFactory zeroTestProtocolFactory;
	private final MiscOIntGenerators misc;

	public ComparisonProtocolFactoryImpl(int statisticalSecurityParameter,
			BasicNumericFactory bnf, LocalInversionFactory localInvFactory,
			PreprocessedNumericBitFactory numericBitFactory,
			ExpFromOIntFactory expFromOIntFactory,
			PreprocessedExpPipeFactory expFactory) {
		this.secParam = statisticalSecurityParameter;
		this.bnf = bnf;
		this.localInvFactory = localInvFactory;
		this.numericNegateBitFactory = new NumericNegateBitFactoryImpl(bnf);
		this.innerProductFactory = new InnerProductFactoryImpl(bnf);
		this.randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(bnf,
				numericBitFactory);
		this.misc = new MiscOIntGenerators(bnf);
		this.zeroTestProtocolFactory = new ZeroTestProtocolFactoryImpl(bnf,
				expFromOIntFactory, numericBitFactory, numericNegateBitFactory, expFactory);
	}

	@Override
	public ComparisonProtocol getGreaterThanProtocol(SInt x1, SInt x2,
			SInt result, boolean longCompare) {
		int bitLength = bnf.getMaxBitLength();
		if (longCompare) {
			bitLength *= 2;
		}
		return new GreaterThanReducerCircuitImpl(bitLength, secParam, x1, x2,
				result, bnf, numericNegateBitFactory, randomAdditiveMaskFactory,
				zeroTestProtocolFactory, misc, innerProductFactory,
				localInvFactory);
	}

	@Override
	public EqualityProtocol getEqualityProtocol(int bitLength,
			SInt x, SInt y, SInt result) {
		return new EqualityProtocolImpl(bitLength, this.secParam, x, y, result,
				bnf, zeroTestProtocolFactory);
	}

}
