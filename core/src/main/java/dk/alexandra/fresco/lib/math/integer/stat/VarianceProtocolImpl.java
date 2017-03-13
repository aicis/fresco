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
package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class VarianceProtocolImpl extends AbstractSimpleProtocol implements VarianceProtocol {

	private SInt[] data;
	private SInt variance;
	private int maxInputLength;
	private SInt mean;

	private final BasicNumericFactory basicNumericFactory;
	private final MeanFactory meanFactory;

	public VarianceProtocolImpl(SInt[] data, int maxInputLength, SInt mean, SInt variance,
			BasicNumericFactory basicNumericFactory, MeanFactory meanFactory) {
		this.data = data;
		this.mean = mean;
		
		this.maxInputLength = maxInputLength;
		this.variance = variance;

		this.basicNumericFactory = basicNumericFactory;
		this.meanFactory = meanFactory;
	}

	public VarianceProtocolImpl(SInt[] data, int maxInputLength, SInt variance,
			BasicNumericFactory basicNumericFactory, MeanFactory meanFactory) {
		this(data, maxInputLength, null, variance, basicNumericFactory, meanFactory);
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {

		SequentialProtocolProducer gp = new SequentialProtocolProducer();

		/*
		 * If a mean was not provided, we first calculate it
		 */
		if (mean == null) {
			this.mean = basicNumericFactory.getSInt();
			gp.append(meanFactory.getMeanProtocol(data, maxInputLength, mean));
		}

		NumericProtocolBuilder numericProtocolBuilder = new NumericProtocolBuilder(
				basicNumericFactory);

		numericProtocolBuilder.beginParScope();
		SInt[] terms = new SInt[data.length];
		for (int k = 0; k < data.length; k++) {
			numericProtocolBuilder.beginSeqScope();
			SInt tmp = numericProtocolBuilder.sub(data[k], mean);
			terms[k] = numericProtocolBuilder.mult(tmp, tmp);
			numericProtocolBuilder.endCurScope();
		}
		numericProtocolBuilder.endCurScope();

		gp.append(numericProtocolBuilder.getProtocol());

		// The sample variance has df = n-1
		gp.append(meanFactory.getMeanProtocol(terms, 2 * maxInputLength, data.length - 1, variance));

		return gp;
	}

}
