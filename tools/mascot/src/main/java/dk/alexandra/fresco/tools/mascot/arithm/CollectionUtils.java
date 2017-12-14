package dk.alexandra.fresco.tools.mascot.arithm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CollectionUtils {

  public static <T> List<List<T>> transpose(List<List<T>> mat) {
    int height = mat.size();
    int width = mat.get(0)
        .size();
    List<List<T>> transposed = new ArrayList<>(width);
    for (int w = 0; w < width; w++) {
      List<T> newRow = new ArrayList<>(height);
      for (int h = 0; h < height; h++) {
        newRow.add(mat.get(h)
            .get(w));
      }
      transposed.add(newRow);
    }
    return transposed;
  }

  public static <T extends Addable<T>> T sum(Stream<T> summands) {
    return summands.reduce((l, r) -> l.add(r))
        .get();
  }

  public static <T extends Addable<T>> T sum(List<T> summands) {
    return sum(summands.stream());
  }

  public static <T extends Addable<T>> List<T> pairWiseSum(List<List<T>> rows) {
    List<List<T>> tilted = transpose(rows);
    List<T> sums = new ArrayList<>(tilted.size());
    for (List<T> row : tilted) {
      sums.add(sum(row));
    }
    return sums;
  }

}