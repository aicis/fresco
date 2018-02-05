package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
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
    Computation<List<List<DRes<SInt>>>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> data;
  private final List<DRes<SInt>> mean;

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
  CovarianceMatrix(List<List<DRes<SInt>>> data, List<DRes<SInt>> mean) {
    this.data = data;
    this.mean = Objects.requireNonNull(mean);

    int sampleSize = data.get(0).size();
    for (List<DRes<SInt>> datum : data) {
      if (datum.size() != sampleSize) {
        throw new IllegalArgumentException(
            "Not a data matrix - all columns must have same size.");
      }
    }
  }

  CovarianceMatrix(List<List<DRes<SInt>>> data) {
    this(data, Collections.emptyList());
  }

  @Override
  public DRes<List<List<DRes<SInt>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.par((par) -> {
      /*
       * If (some of) the sample means has not been provided, we calculate
       * them here.
       */
      List<DRes<SInt>> allMeans = new ArrayList<>(data.size());
      Iterator<DRes<SInt>> means = mean.iterator();
      for (List<DRes<SInt>> datum : data) {
        DRes<SInt> currentMean = null;
        if (means.hasNext()) {
          currentMean = means.next();
        }
        if (currentMean == null) {
          currentMean = par.seq(new Mean(datum));
        }
        allMeans.add(currentMean);
      }
      return () -> allMeans;
    }).par((par, means) -> {
      //Iterate using ListIterator instead of indexed loop to avoid RandomAccess in lists
      List<List<DRes<SInt>>> result = new ArrayList<>(data.size());
      ListIterator<List<DRes<SInt>>> dataIterator = data.listIterator();
      while (dataIterator.hasNext()) {
        int currentIndex = dataIterator.nextIndex();
        List<DRes<SInt>> dataRow = dataIterator.next();
        List<DRes<SInt>> row = new ArrayList<>(currentIndex + 1);
        result.add(row);
        ListIterator<List<DRes<SInt>>> innerIterator = data.listIterator();
        while (innerIterator.nextIndex() < currentIndex) {
          int innerIndex = innerIterator.nextIndex();
          List<DRes<SInt>> dataRow2 = innerIterator.next();
          row.add(par.seq(new Covariance(
              dataRow, dataRow2,
              means.get(currentIndex), means.get(innerIndex)
              )));
        }
        // When i == j we are calculating the variance of data[i]
        // which saves us one subtraction per data entry compared to
        // calculating the covariance
        row.add(par.seq(new Variance(dataRow, means.get(currentIndex))));
      }
      return () -> result;
    });
  }

}
