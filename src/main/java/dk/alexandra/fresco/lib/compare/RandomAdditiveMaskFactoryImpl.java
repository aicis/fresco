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
package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;

public class RandomAdditiveMaskFactoryImpl implements RandomAdditiveMaskFactory {

	private final NumericBitFactory numericBitFactory;
	private final InnerProductFactory innerProductFactory;
	private final MiscOIntGenerators misc;
	private final BasicNumericFactory bnf;

	public RandomAdditiveMaskFactoryImpl(BasicNumericFactory bnf,
			NumericBitFactory numericBitFactory) {
		this.bnf = bnf;
		this.misc = new MiscOIntGenerators(bnf);
		this.numericBitFactory = numericBitFactory;
		this.innerProductFactory = new InnerProductFactoryImpl(bnf);
	}

	@Override
	public RandomAdditiveMaskProtocol getRandomAdditiveMaskProtocol(int securityParameter,
			SInt[] bits, SInt r) {
		return new RandomAdditiveMaskProtocolImpl(securityParameter, bits, r, bnf, numericBitFactory,
				misc, innerProductFactory);
	}

}
