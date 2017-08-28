package dk.alexandra.fresco.lib.collections;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import dk.alexandra.fresco.framework.Computation;

public class MatrixUtils {

  /**
   * Unwraps rows in matrix to get lists
   * 
   * @param closedRows
   * @return
   */
  public <T> Matrix<Computation<T>> unwrapRows(List<Computation<List<Computation<T>>>> closedRows) {
    ArrayList<ArrayList<Computation<T>>> unwrapped = closedRows.stream()
        .map(row -> new ArrayList<>(row.out())).collect(Collectors.toCollection(ArrayList::new));
    int h = unwrapped.size();
    int w = h > 0 ? unwrapped.get(0).size() : 0;
    return new Matrix<>(h, w, unwrapped);
  }

  /**
   * Unwrap a Matrix<Computation<T>> into a Matrix<T>.
   * 
   * @param mat the matrix to unwrap
   * @return
   */
  public <T> Matrix<T> unwrapMatrix(Matrix<Computation<T>> mat) {
    ArrayList<ArrayList<T>> tmp = new ArrayList<>();
    for (ArrayList<Computation<T>> row : mat.getRows()) {
      tmp.add(row.stream().map(Computation::out).collect(Collectors.toCollection(ArrayList::new)));
    }
    int h = tmp.size();
    int w = h > 0 ? tmp.get(0).size() : 0;
    return new Matrix<>(h, w, tmp);
  }

  /**
   * Create matrix of given dimensions filled with values from 0 to numRows * numCols.
   * 
   * @param numRows
   * @param numCols
   * @return
   */
  public Matrix<BigInteger> getInputMatrix(int numRows, int numCols) {
    ArrayList<ArrayList<BigInteger>> mat = new ArrayList<>();
    int counter = 0;
    for (int r = 0; r < numRows; r++) {
      ArrayList<BigInteger> row = new ArrayList<>();
      for (int c = 0; c < numCols; c++) {
        row.add(BigInteger.valueOf(counter++));
      }
      mat.add(row);
    }
    return new Matrix<>(numRows, numCols, mat);
  }
}
