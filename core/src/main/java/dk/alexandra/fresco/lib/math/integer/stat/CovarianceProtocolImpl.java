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
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class CovarianceProtocolImpl extends AbstractSimpleProtocol implements CovarianceProtocol {

	private SInt[] data1, data2;
	private SInt mean1, mean2;
	private SInt covariance;

	private final BasicNumericFactory basicNumericFactory;
	private final MeanFactory meanFactory;

	public CovarianceProtocolImpl(SInt[] data1, SInt[] data2, SInt mean1, SInt mean2, SInt covariance,
			BasicNumericFactory basicNumericFactory, MeanFactory meanFactory) {

		this.data1 = data1;
		this.data2 = data2;

		this.mean1 = mean1;
		this.mean2 = mean2;

		this.covariance = covariance;

		this.basicNumericFactory = basicNumericFactory;
		this.meanFactory = meanFactory;
	}

	public CovarianceProtocolImpl(SInt[] data1, SInt[] data2, SInt covariance, BasicNumericFactory basicNumericFactory,
			MeanFactory meanFactory) {

		this(data1, data2, null, null, covariance, basicNumericFactory, meanFactory);
	}

	@Override
	protected ProtocolProducer initializeProtocolProducer() {

		if (data1.length != data2.length) {
			throw new IllegalArgumentException("Must have same sample size.");
		}

		SequentialProtocolProducer gp = new SequentialProtocolProducer();

		/*
		 * If a mean was not provided, we first calculate it
		 */
		ParallelProtocolProducer findMeans = new ParallelProtocolProducer();
		if (mean1 == null) {
			this.mean1 = basicNumericFactory.getSInt();
			findMeans.append(meanFactory.getMeanProtocol(data1, mean1));
		}
		if (mean2 == null) {
			this.mean2 = basicNumericFactory.getSInt();
			findMeans.append(meanFactory.getMeanProtocol(data2, mean2));
		}
		if (!findMeans.getProducers().isEmpty()) {
			gp.append(findMeans);
		}

		NumericProtocolBuilder numericProtocolBuilder = new NumericProtocolBuilder(basicNumericFactory);
		numericProtocolBuilder.beginParScope();
		SInt[] terms = new SInt[data1.length];
		for (int k = 0; k < data1.length; k++) {
			numericProtocolBuilder.beginSeqScope();
			SInt tmp1 = numericProtocolBuilder.sub(data1[k], mean1);
			SInt tmp2 = numericProtocolBuilder.sub(data2[k], mean2);
			terms[k] = numericProtocolBuilder.mult(tmp1, tmp2);
			numericProtocolBuilder.endCurScope();
		}
		numericProtocolBuilder.endCurScope();

		gp.append(numericProtocolBuilder.getProtocol());

		// The sample variance has df = n-1
		gp.append(meanFactory.getMeanProtocol(terms, data1.length - 1, covariance));

		return gp;
	}

}
