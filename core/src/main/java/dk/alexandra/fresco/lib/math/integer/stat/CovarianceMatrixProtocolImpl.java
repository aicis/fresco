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
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class CovarianceMatrixProtocolImpl extends SimpleProtocolProducer implements
    CovarianceMatrixProtocol {

  private SInt[][] data;
  private SInt[] mean;

  private SInt[][] result;

  private final BasicNumericFactory basicNumericFactory;
  private final VarianceFactory varianceFactory;
  private final CovarianceFactory covarianceFactory;
  private MeanFactory meanFactory;

  /**
   * Construct a protocol for calculating the covariance matrix for the given
   * data. If (some of) the sample means have already been calculated they
   * should be provided here such that <code>mean[i]</code> is the mean of
   * <code>data[i]</code>, but not all means has to be provided in which case
   * they should be left as <code>null</code>.
   *
   * @param data The data, one sample set for each column
   * @param maxInputLength An upper bound for the size of each data entry
   * @param mean The means
   * @param result The covariance matrix <i>(c<sub>ij</sub>)</i> such that <i>c_ij =
   * Cov(<code>data[i]</code>, <code>data[j]</code>)</i>
   */
  public CovarianceMatrixProtocolImpl(SInt[][] data, SInt[] mean,
      SInt[][] result, BasicNumericFactory basicNumericFactory, MeanFactory meanFactory,
      VarianceFactory varianceFactory, CovarianceFactory covarianceFactory) {
    
    int numOfDataSets = data.length;
    int sampleSize = data[0].length;
    for (int i = 1; i < numOfDataSets; i++) {
      if (data[i].length != sampleSize) {
        throw new IllegalArgumentException(
            "Not a data matrix - all columns must have same size.");
      }
    }
    
    this.data = data;
    this.mean = mean;

    this.result = result;

    this.basicNumericFactory = basicNumericFactory;
    this.meanFactory = meanFactory;
    this.varianceFactory = varianceFactory;
    this.covarianceFactory = covarianceFactory;
  }

  /**
   * Wrapper constructor for when the means are not known. Will just pass on parameters to {@link
   * #CovarianceMatrixProtocolImpl(SInt[][], int, SInt[], SInt[][], BasicNumericFactory,
   * MeanFactory, VarianceFactory, CovarianceFactory)} with <code>mean = null</code>.
   */
  public CovarianceMatrixProtocolImpl(SInt[][] data, SInt[][] result,
      BasicNumericFactory basicNumericFactory, MeanFactory meanFactory,
      VarianceFactory varianceFactory, CovarianceFactory covarianceFactory) {
    this(data, null, result, basicNumericFactory, meanFactory, varianceFactory,
        covarianceFactory);
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {

    int numOfDataSets = data.length;

    SequentialProtocolProducer gp = new SequentialProtocolProducer();

		/*
     * If (some of) the sample means has not been provided, we calculate
		 * them here.
		 */
    if (mean == null) {
      mean = new SInt[numOfDataSets];
    }
    ParallelProtocolProducer findMeans = new ParallelProtocolProducer();
    for (int i = 0; i < numOfDataSets; i++) {
      if (mean[i] == null) {
        mean[i] = basicNumericFactory.getSInt();
        findMeans.append(meanFactory.getMeanProtocol(data[i], mean[i]));
      }
    }
    if (!findMeans.getProducers().isEmpty()) {
      gp.append(findMeans);
    }

		/*
		 * Calculate the covariance matrix (c_ij) such that c_ij = Cov(data[i],
		 * data[j])
		 */
    ParallelProtocolProducer findCovariances = new ParallelProtocolProducer();
    for (int i = 0; i < numOfDataSets; i++) {
      for (int j = i; j < numOfDataSets; j++) {
        if (i == j) {
          // When i == j we are calculating the variance of data[i]
          // which saves us one subtraction per data entry compared to
          // calculating the covariance
          findCovariances.append(varianceFactory.getVarianceProtocol(data[i],
              mean[i], result[i][j]));
        } else {
          SequentialProtocolProducer findCovariance = new SequentialProtocolProducer();

          findCovariance.append(covarianceFactory.getCovarianceProtocol(data[i],
              data[j], result[i][j]));
          // Covariance matrix is symmetric
          findCovariance.append(new CopyProtocolImpl<SInt>(result[i][j], result[j][i]));

          findCovariances.append(findCovariance);
        }
      }
    }
    gp.append(findCovariances);
    return gp;
  }
}
