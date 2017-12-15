package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MascotTestUtils {

  public static List<FieldElement> generateSingleRow(int[] factors, BigInteger modulus,
      int modBitLength) {
    return Arrays.stream(factors).mapToObj(val -> new FieldElement(val, modulus, modBitLength))
        .collect(Collectors.toList());
  }

  /**
   * Converts integer matrix into field-element matrix.
   * 
   * @param rows integer matrix
   * @param modulus field modulus
   * @param modBitLength bit length of modulus
   * @return field element matrix
   */
  public static List<List<FieldElement>> generateMatrix(int[][] rows, BigInteger modulus,
      int modBitLength) {
    int numMults = rows.length;
    List<List<FieldElement>> input = new ArrayList<>(numMults);
    for (int[] leftFactorRow : rows) {
      List<FieldElement> row =
          Arrays.stream(leftFactorRow).mapToObj(val -> new FieldElement(val, modulus, modBitLength))
              .collect(Collectors.toList());
      input.add(row);
    }
    return input;
  }

  /**
   * Wraps field element so we can check equality on it.
   * 
   * @param element element to be wrapped
   * @return wrapped element that has an equals method
   */
  public static FieldElementWrapper wrap(FieldElement element) {
    return new FieldElementWrapper(element);
  }
  
  public static List<FieldElementWrapper> wrap(List<FieldElement> elements) {
    return elements.stream().map(MascotTestUtils::wrap).collect(Collectors.toList());
  }
  
}
