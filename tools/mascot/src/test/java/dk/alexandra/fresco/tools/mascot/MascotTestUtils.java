package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MascotTestUtils {

  public static List<MascotFieldElement> generateSingleRow(int[] factors, Modulus modulus) {
    return Arrays.stream(factors).mapToObj(val -> new MascotFieldElement(val, modulus))
        .collect(Collectors.toList());
  }

  /**
   * Converts integer matrix into field-element matrix.
   * 
   * @param rows integer matrix
   * @param modulus field modulus
   * @return field element matrix
   */
  public static List<List<MascotFieldElement>> generateMatrix(int[][] rows, Modulus modulus) {
    int numMults = rows.length;
    List<List<MascotFieldElement>> input = new ArrayList<>(numMults);
    for (int[] leftFactorRow : rows) {
      List<MascotFieldElement> row =
          Arrays.stream(leftFactorRow).mapToObj(val -> new MascotFieldElement(val, modulus))
              .collect(Collectors.toList());
      input.add(row);
    }
    return input;
  }

}
