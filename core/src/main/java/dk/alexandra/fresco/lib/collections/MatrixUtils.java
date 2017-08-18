package dk.alexandra.fresco.lib.collections;

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
    // unwrap Computations to get enclosed data
    // hey, at least it's not two nested maps...
    for (ArrayList<Computation<T>> row : mat.getRows()) {
      tmp.add(row.stream().map(Computation::out).collect(Collectors.toCollection(ArrayList::new)));
    }
    int h = tmp.size();
    int w = h > 0 ? tmp.get(0).size() : 0;
    return new Matrix<>(h, w, tmp);
  }
}
