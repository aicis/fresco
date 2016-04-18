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
package dk.alexandra.fresco.lib.math.integer.stat;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;

public class VarianceProtocolImpl extends AbstractSimpleProtocol implements VarianceProtocol {

	private final BasicNumericFactory basicNumericFactory;
	private final DivisionFactory divisionFactory;

	private SInt[] data;
	private SInt result;
	private int maxInputLength;
	private SInt mean;

	public VarianceProtocolImpl(SInt[] data, int maxInputLength, SInt mean, SInt result,
			BasicNumericFactory basicNumericFactory,
			DivisionFactory divisionFactory) {
		this.data = data;
		this.maxInputLength = maxInputLength;
		this.mean = mean;
		this.result = result;
		this.basicNumericFactory = basicNumericFactory;
		this.divisionFactory = divisionFactory;
	}

	@Override
	protected ProtocolProducer initializeGateProducer() {

		NumericProtocolBuilder numericProtocolBuilder = new NumericProtocolBuilder(
				basicNumericFactory);
		
		SInt[] terms = new SInt[data.length];
		numericProtocolBuilder.beginParScope();
		for (int i = 0; i < terms.length; i++) {
			numericProtocolBuilder.beginSeqScope();
			SInt tmp = numericProtocolBuilder.sub(data[i], mean);
			terms[i] = numericProtocolBuilder.mult(tmp, tmp);
			numericProtocolBuilder.endCurScope();
		}
		numericProtocolBuilder.endCurScope();
		
		SInt sum = numericProtocolBuilder.sum(terms);
		OInt nMinus1 = basicNumericFactory.getOInt(BigInteger.valueOf(data.length - 1));

		int maxSumLength = 2 * maxInputLength + (int) Math.ceil(Math.log(terms.length) / Math.log(2));
		Protocol divide = divisionFactory.getDivisionProtocol(sum, maxSumLength, nMinus1, result);
		
		return new SequentialProtocolProducer(numericProtocolBuilder.getCircuit(), divide);
	}

}
