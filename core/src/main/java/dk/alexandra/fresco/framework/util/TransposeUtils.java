package dk.alexandra.fresco.framework.util;

import java.util.ArrayList;
import java.util.List;

public class TransposeUtils<T> {

  private TransposeUtils() {
  }

  /**
   * Transposes matrix. <p>Rows become columns and columns become rows.</p>
   *
   * @param mat matrix to transpose
   * @return transposed matrix
   */
  public static <T> List<List<T>> transpose(List<List<T>> mat) {
    int height = mat.size();
    int width = mat.get(0).size();
    List<List<T>> transposed = new ArrayList<>(width);
    for (int w = 0; w < width; w++) {
      List<T> newRow = new ArrayList<>(height);
      for (List<T> aMat : mat) {
        newRow.add(aMat.get(w));
      }
      transposed.add(newRow);
    }
    return transposed;
  }

}
