/*
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

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Calculates the Covariance matrix for the supplied data.
 * Note that only lower triangle of matrix (i,j for i \geq j) will be computed.
 * The symmetric entry will be a copy of the one from the lower triangle, M[i][j] := M[j][i].
 */
public class CovarianceMatrix implements
    ComputationBuilder<List<List<Computation<SInt>>>, ProtocolBuilderNumeric> {

  private final List<List<Computation<SInt>>> data;
  private final List<Computation<SInt>> mean;

  /**
   * Construct a protocol for calculating the covariance matrix for the given
   * data. If (some of) the sample means have already been calculated they
   * should be provided here such that <code>mean[i]</code> is the mean of
   * <code>data[i]</code>, but not all means has to be provided in which case
   * they should be left as <code>null</code>.
   *
   * @param data The data, one sample set for each column
   * @param mean The means
   */
  CovarianceMatrix(List<List<Computation<SInt>>> data, List<Computation<SInt>> mean) {
    this.data = data;
    this.mean = Objects.requireNonNull(mean);

    int sampleSize = data.get(0).size();
    for (List<Computation<SInt>> datum : data) {
      if (datum.size() != sampleSize) {
        throw new IllegalArgumentException(
            "Not a data matrix - all columns must have same size.");
      }
    }
  }

  CovarianceMatrix(List<List<Computation<SInt>>> data) {
    this(data, Collections.emptyList());
  }

  @Override
  public Computation<List<List<Computation<SInt>>>> build(ProtocolBuilderNumeric builder) {
    return builder.par((par) -> {
      /*
       * If (some of) the sample means has not been provided, we calculate
		   * them here.
		   */
      List<Computation<SInt>> allMeans = new ArrayList<>(data.size());
      Iterator<Computation<SInt>> means = mean.iterator();
      for (List<Computation<SInt>> datum : data) {
        Computation<SInt> currentMean = null;
        if(means.hasNext()) {
          currentMean = means.next();
        }
        if(currentMean == null) {
          currentMean = par.createSequentialSub(new Mean(datum));
        }
        allMeans.add(currentMean);
      }
      return () -> allMeans;
    }).par((means, par) -> {
      //Iterate using ListIterator instead of indexed loop to avoid RandomAccess in lists
      List<List<Computation<SInt>>> result = new ArrayList<>(data.size());
      ListIterator<List<Computation<SInt>>> dataIterator = data.listIterator();
      while (dataIterator.hasNext()) {
        int currentIndex = dataIterator.nextIndex();
        List<Computation<SInt>> dataRow = dataIterator.next();
        List<Computation<SInt>> row = new ArrayList<>(currentIndex + 1);
        result.add(row);
        ListIterator<List<Computation<SInt>>> innerIterator = data.listIterator();
        while (innerIterator.nextIndex() < currentIndex) {
          int innerIndex = innerIterator.nextIndex();
          List<Computation<SInt>> dataRow2 = innerIterator.next();
          row.add(par.createSequentialSub(
              new Covariance(
                  dataRow, dataRow2,
                  means.get(currentIndex), means.get(innerIndex)
              )
          ));
        }
        // When i == j we are calculating the variance of data[i]
        // which saves us one subtraction per data entry compared to
        // calculating the covariance
        row.add(par.createSequentialSub(
            new Variance(dataRow, means.get(currentIndex))));
      }
      return () -> result;
    });
  }

}
