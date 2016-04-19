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

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class CovarianceProtocolImpl extends AbstractSimpleProtocol implements CovarianceProtocol {

	private SInt[][] data;
	private SInt[] mean;
	private int maxInputLength;

	private SInt[][] result;

	private boolean givenMean;

	private final BasicNumericFactory basicNumericFactory;
	private MeanFactory arithmeticMeanFactory;

	public CovarianceProtocolImpl(SInt[][] data, int maxInputLength, SInt[] mean, SInt[][] result,
			BasicNumericFactory basicNumericFactory, MeanFactory arithmeticMeanFactory) {
		this.data = data;
		this.mean = mean;
		this.givenMean = true;
		this.maxInputLength = maxInputLength;
		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.arithmeticMeanFactory = arithmeticMeanFactory;
	}

	public CovarianceProtocolImpl(SInt[] data1, SInt[] data2, int maxInputLength, SInt mean2,
			SInt mean1, SInt result, BasicNumericFactory basicNumericFactory,
			MeanFactory arithmeticMeanFactory) {

		this(new SInt[][] { data1, data2 }, maxInputLength, new SInt[] { mean1, mean2 },
				new SInt[][] { new SInt[] { result } }, basicNumericFactory, arithmeticMeanFactory);

	}

	public CovarianceProtocolImpl(SInt[][] data, int maxInputLength, SInt[][] result,
			BasicNumericFactory basicNumericFactory, MeanFactory arithmeticMeanFactory) {
		this(data, maxInputLength, null, result, basicNumericFactory, arithmeticMeanFactory);
		this.givenMean = false;
	}

	public CovarianceProtocolImpl(SInt[] data1, SInt[] data2, int maxInputLength, SInt result,
			BasicNumericFactory basicNumericFactory, MeanFactory arithmeticMeanFactory) {

		this(new SInt[][] { data1, data2 }, maxInputLength, new SInt[][] { new SInt[] { result } },
				basicNumericFactory, arithmeticMeanFactory);

	}

	@Override
	protected ProtocolProducer initializeGateProducer() {

		int numOfDataSets = data.length;
		int sampleSize = data[0].length;
		for (int i = 1; i < numOfDataSets; i++) {
			if (data[i].length != sampleSize) {
				throw new IllegalArgumentException(
						"Not a data matrix - all columns must have same size.");
			}
		}

		SequentialProtocolProducer gp = new SequentialProtocolProducer();

		/*
		 * If a mean was not provided, we first calculate it
		 */
		if (!givenMean) {
			this.mean = new SInt[numOfDataSets];
			ParallelProtocolProducer findMeans = new ParallelProtocolProducer();
			for (int i = 0; i < numOfDataSets; i++) {
				this.mean[i] = basicNumericFactory.getSInt();
				findMeans.append(arithmeticMeanFactory.getMeanProtocol(data[i],
						maxInputLength, mean[i]));
			}
			gp.append(findMeans);
		}

		NumericProtocolBuilder numericProtocolBuilder = new NumericProtocolBuilder(
				basicNumericFactory);

		ParallelProtocolProducer findCovariances = new ParallelProtocolProducer();
		for (int i = 0; i < numOfDataSets; i++) {
			for (int j = i; j < numOfDataSets; j++) {
				SequentialProtocolProducer findCovariance = new SequentialProtocolProducer();
				
				numericProtocolBuilder.beginParScope();
				SInt[] terms = new SInt[sampleSize];
				for (int k = 0; k < sampleSize; k++) {
					numericProtocolBuilder.beginSeqScope();
					if (i == j) {
						// If i == j we are calculating the variance of data[i]
						SInt tmp1 = numericProtocolBuilder.sub(data[i][k], mean[i]);
						terms[k] = numericProtocolBuilder.mult(tmp1, tmp1);
					} else {
						SInt tmp1 = numericProtocolBuilder.sub(data[i][k], mean[i]);
						SInt tmp2 = numericProtocolBuilder.sub(data[j][k], mean[j]);
						terms[k] = numericProtocolBuilder.mult(tmp1, tmp2);
					}
					numericProtocolBuilder.endCurScope();
				}
				numericProtocolBuilder.endCurScope();

				findCovariance.append(numericProtocolBuilder.getCircuit());
				numericProtocolBuilder.reset();

				// The sample variance has df = n-1
				findCovariance.append(arithmeticMeanFactory.getMeanProtocol(terms,
						2 * maxInputLength, sampleSize - 1, result[i][j]));

				if (i != j) {
					// Covariance matrix is symmetric
					result[j][i] = result[i][j];
				}
				findCovariances.append(findCovariance);
			}
		}
		gp.append(findCovariances);
		return new SequentialProtocolProducer(gp);
	}

}
