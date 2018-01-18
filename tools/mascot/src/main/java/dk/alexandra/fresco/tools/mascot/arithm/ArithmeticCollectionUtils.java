package dk.alexandra.fresco.tools.mascot.arithm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A utility class that provides various arithmetic methods, such as batch addition, over
 * collections of {@link Addable} instances.
 */
public class ArithmeticCollectionUtils<T extends Addable<T>> {

  /**
   * Transposes matrix. <br> Rows become columns and columns become rows.
   *
   * @param mat matrix to transpose
   * @return transposed matrix
   */
  public List<List<T>> transpose(List<List<T>> mat) {
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

  public T sum(Stream<T> summands) {
    return summands.reduce(Addable::add).get();
  }

  public T sum(List<T> summands) {
    return sum(summands.stream());
  }

  /**
   * Adds up elements in each column.
   *
   * @param rows rows to be added up
   * @return sum of rows
   */
  public List<T> sumRows(List<List<T>> rows) {
    List<List<T>> tilted = transpose(rows);
    List<T> sums = new ArrayList<>(tilted.size());
    for (List<T> row : tilted) {
      sums.add(sum(row));
    }
    return sums;
  }

}
