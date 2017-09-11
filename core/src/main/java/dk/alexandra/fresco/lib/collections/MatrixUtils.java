package dk.alexandra.fresco.lib.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.DRes;

public class MatrixUtils {

  /**
   * Unwraps rows in matrix to get lists
   * 
   * @param closedRows
   * @return
   */
  public <T> Matrix<DRes<T>> unwrapRows(List<DRes<List<DRes<T>>>> closedRows) {
    ArrayList<ArrayList<DRes<T>>> unwrapped = closedRows.stream()
        .map(row -> new ArrayList<>(row.out())).collect(Collectors.toCollection(ArrayList::new));
    int h = unwrapped.size();
    int w = h > 0 ? unwrapped.get(0).size() : 0;
    return new Matrix<>(h, w, unwrapped);
  }

  /**
   * Unwrap a Matrix<DRes<T>> into a Matrix<T>.
   * 
   * @param mat the matrix to unwrap
   * @return
   */
  public <T> Matrix<T> unwrapMatrix(DRes<Matrix<DRes<T>>> mat) {
    ArrayList<ArrayList<T>> tmp = new ArrayList<>();
    for (ArrayList<DRes<T>> row : mat.out().getRows()) {
      tmp.add(row.stream().map(DRes::out).collect(Collectors.toCollection(ArrayList::new)));
    }
    int h = tmp.size();
    int w = h > 0 ? tmp.get(0).size() : 0;
    return new Matrix<>(h, w, tmp);
  }
}
