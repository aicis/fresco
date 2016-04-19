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
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class CovarianceMatrixProtocolImpl extends AbstractSimpleProtocol implements
		CovarianceMatrixProtocol {

	private SInt[][] data;
	private SInt[] mean;
	private int maxInputLength;

	private SInt[][] result;

	private boolean givenMean;

	private final MeanFactory meanFactory;
	private final VarianceFactory varianceFactory;
	private final CovarianceFactory covarianceFactory;
	private final BasicNumericFactory basicNumericFactory;

	public CovarianceMatrixProtocolImpl(SInt[][] data, int maxInputLength, SInt[] mean,
			SInt[][] result, BasicNumericFactory basicNumericFactory, MeanFactory meanFactory,
			VarianceFactory varianceFactory, CovarianceFactory covarianceFactory) {
		this.data = data;

		if (mean != null) {
			this.mean = mean;
			this.givenMean = true;
		} else {
			this.mean = null;
			this.givenMean = false;
		}

		this.maxInputLength = maxInputLength;
		this.result = result;

		this.basicNumericFactory = basicNumericFactory;
		this.meanFactory = meanFactory;
		this.varianceFactory = varianceFactory;
		this.covarianceFactory = covarianceFactory;
	}

	public CovarianceMatrixProtocolImpl(SInt[][] data, int maxInputLength, SInt[][] result,
			BasicNumericFactory basicNumericFactory, MeanFactory meanFactory,
			VarianceFactory varianceFactory, CovarianceFactory covarianceFactory) {
		this(data, maxInputLength, null, result, basicNumericFactory, meanFactory, varianceFactory,
				covarianceFactory);
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

		ParallelProtocolProducer findCovariances = new ParallelProtocolProducer();
		for (int i = 0; i < numOfDataSets; i++) {
			for (int j = i; j < numOfDataSets; j++) {
				if (i == j) {
					// If i=j we are calculating the variance of data[i]
					if (givenMean) {
						findCovariances.append(varianceFactory.getVarianceProtocol(data[i],
								maxInputLength, mean[i], result[i][j]));
					} else {
						findCovariances.append(varianceFactory.getVarianceProtocol(data[i],
								maxInputLength, result[i][j]));
					}
				} else {
					if (givenMean) {
						findCovariances.append(covarianceFactory.getCovarianceProtocol(data[i],
								data[j], maxInputLength, mean[i], mean[j], result[i][j]));
					} else {
						findCovariances.append(covarianceFactory.getCovarianceProtocol(data[i],
								data[j], maxInputLength, result[i][j]));
					}
					result[j][i] = result[i][j];
				}
			}
		}
		gp.append(findCovariances);
		return new SequentialProtocolProducer(gp);
	}
}
