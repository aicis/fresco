package dk.alexandra.fresco.tools.mascot.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dk.alexandra.fresco.tools.mascot.ArithmeticElement;

public class BatchArithmetic {

  public static <T extends ArithmeticElement<T>> List<T> pairWiseAdd(List<T> group,
      List<T> otherGroup) {
    if (group.size() != otherGroup.size()) {
      throw new IllegalArgumentException("Groups must be same size");
    }
    Stream<T> feStream = IntStream.range(0, group.size())
        .mapToObj(idx -> {
          T el = group.get(idx);
          T otherEl = otherGroup.get(idx);
          return el.add(otherEl);
        });
    return feStream.collect(Collectors.toList());
  }

  public static <T extends ArithmeticElement<T>> List<List<T>> pairWiseAddRows(List<List<T>> row,
      List<List<T>> otherRow) {
    if (row.size() != otherRow.size()) {
      throw new IllegalArgumentException("Rows must be same size");
    }
    List<List<T>> rowStreams = IntStream.range(0, row.size())
        .mapToObj(idx -> {
          List<T> group = row.get(idx);
          List<T> otherGroup = otherRow.get(idx);
          return pairWiseAdd(group, otherGroup);
        })
        .collect(Collectors.toList());
    return rowStreams;
  }

  public static <T extends ArithmeticElement<T>> List<List<T>> pairWiseAdd(
      List<List<List<T>>> rows) {
    return rows.stream()
        .reduce((top, bottom) -> {
          return pairWiseAddRows(top, bottom);
        })
        .get();
  }

  public static <T extends ArithmeticElement<T>> List<T> scalarMultiply(List<T> leftFactors,
      T rightFactor) {
    return leftFactors.stream()
        .map(lf -> lf.multiply(rightFactor))
        .collect(Collectors.toList());
  }

  public static <T extends ArithmeticElement<T>> List<List<T>> pairWiseMultiply(
      List<List<T>> leftFactorGroups, List<T> rightFactors) {
    if (leftFactorGroups.size() != rightFactors.size()) {
      throw new IllegalArgumentException("Rows must be same size");
    }
    return IntStream.range(0, leftFactorGroups.size())
        .mapToObj(idx -> {
          List<T> lfg = leftFactorGroups.get(idx);
          T rf = rightFactors.get(idx);
          return scalarMultiply(lfg, rf);
        })
        .collect(Collectors.toList());
  }

}
