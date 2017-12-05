package dk.alexandra.fresco.tools.mascot.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dk.alexandra.fresco.tools.mascot.ArithmeticElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class BatchArithmetic {

  public static <T extends ArithmeticElement<T>> List<T> addGroups(List<T> group,
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

  public static <T extends ArithmeticElement<T>> List<List<T>> addRows(List<List<T>> row,
      List<List<T>> otherRow) {
    if (row.size() != otherRow.size()) {
      throw new IllegalArgumentException("Rows must be same size");
    }
    List<List<T>> rowStreams = IntStream.range(0, row.size())
        .mapToObj(idx -> {
          List<T> group = row.get(idx);
          List<T> otherGroup = otherRow.get(idx);
          return addGroups(group, otherGroup);
        })
        .collect(Collectors.toList());
    return rowStreams;
  }

  static <T> List<List<T>> naiveTranspose(List<List<T>> mat) {
    int height = mat.size();
    int width = mat.get(0).size();
    List<List<T>> transposed = new ArrayList<>(width);
    for (int w = 0; w < width; w++) {
      List<T> newRow = new ArrayList<>(height);
      for (int h = 0; h < height; h++) {
        newRow.add(mat.get(h).get(w));
      }
      transposed.add(newRow);
    }
    return transposed;
  }
  
  public static <T extends ArithmeticElement<T>> T sum(List<T> summands) {
    return summands.stream().reduce((l, r) -> l.add(r)).get();
  }
  
  public static <T extends ArithmeticElement<T>> List<T> pairWiseAddRows(List<List<T>> rows) {
    List<List<T>> tilted = naiveTranspose(rows);
    List<T> sums = new ArrayList<>(tilted.size()); 
    for (List<T> row : tilted) {
      sums.add(sum(row));
    }
    return sums;
  }

  public static <T extends ArithmeticElement<T>> List<T> scalarMultiply(List<T> leftFactors,
      T rightFactor) {
    return leftFactors.stream()
        .map(lf -> lf.multiply(rightFactor))
        .collect(Collectors.toList());
  }

  public static <T extends ArithmeticElement<T>> List<T> pairWiseMultiply(
      List<T> leftFactorGroups, List<T> rightFactors) {
    if (leftFactorGroups.size() != rightFactors.size()) {
      throw new IllegalArgumentException("Rows must be same size");
    }
    return IntStream.range(0, leftFactorGroups.size())
        .mapToObj(idx -> {
          T l = leftFactorGroups.get(idx);
          T r = rightFactors.get(idx);
          return l.multiply(r);
        })
        .collect(Collectors.toList());
  }

  public static List<FieldElement> stretch(List<FieldElement> elements, int stretchBy) {
    List<FieldElement> stretched = new ArrayList<>(elements.size() * stretchBy);
    for (FieldElement element : elements) {
      for (int c = 0; c < stretchBy; c++) {
        stretched.add(new FieldElement(element));
      }
    }
    return stretched;
  }
  
}
