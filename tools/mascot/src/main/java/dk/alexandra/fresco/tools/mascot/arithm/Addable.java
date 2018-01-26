package dk.alexandra.fresco.tools.mascot.arithm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An implementing class must support arithmetic addition with instances of type {@link T}.
 */
public interface Addable<T> {

  T add(T other);

  /**
   * Computes sum of values in stream.
   */
  static <A extends Addable<A>> A sum(Stream<A> summands) {
    return summands.reduce(Addable::add).get();
  }

  /**
   * Computes sum of values in list.
   */
  static <A extends Addable<A>> A sum(List<A> summands) {
    return sum(summands.stream());
  }

  /**
   * Adds up elements in each column.
   *
   * @param rows rows to be added up
   * @return sum of rows
   */
  static <A extends Addable<A>> List<A> sumRows(List<List<A>> rows) {
    List<List<A>> tilted = TransposeUtils.transpose(rows);
    List<A> sums = new ArrayList<>(tilted.size());
    for (List<A> row : tilted) {
      sums.add(sum(row));
    }
    return sums;
  }

}
