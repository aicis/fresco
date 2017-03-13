/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.math.polynomial.evaluator;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.math.polynomial.Polynomial;

public class PolynomialEvaluatorProtocolImpl extends AbstractSimpleProtocol implements
		PolynomialEvaluatorProtocol {

	private final SInt x;
	private final Polynomial p;
	private SInt result;
	private final BasicNumericFactory basicNumericFactory;

	public PolynomialEvaluatorProtocolImpl(SInt x, Polynomial p, SInt result,
			BasicNumericFactory basicNumericFactory) {
		this.x = x;
		this.p = p;
		this.result = result;
		this.basicNumericFactory = basicNumericFactory;
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {

		NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);

		int degree = p.getMaxDegree();

		/*
		 * We use Horner's method, p(x) = (( ... ((p_{n-1} x + p_{n-2})x +
		 * p_{n-3}) ... )x + a_1)x + a_0
		 */
		SInt tmp = p.getCoefficient(degree - 1);
		builder.beginSeqScope();
		if (tmp == null) {
			tmp = builder.getSInt(0);
		}
		for (int i = degree - 2; i >= 0; i--) {
			tmp = builder.mult(tmp, x);
			if (p.getCoefficient(i) != null) {
				tmp = builder.add(tmp, p.getCoefficient(i));
			}
		}
		
		builder.copy(result, tmp);
		builder.endCurScope();

		return builder.getProtocol();
	}

}
