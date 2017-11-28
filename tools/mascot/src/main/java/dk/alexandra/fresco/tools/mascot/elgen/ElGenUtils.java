package dk.alexandra.fresco.tools.mascot.elgen;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class ElGenUtils {

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
  
  static List<List<FieldElement>> transpose(List<List<FieldElement>> mat) {
    // TODO: should switch to doing fast transpose
    return naiveTranspose(mat);
  }
  
}
