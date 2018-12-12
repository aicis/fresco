package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MascotTestUtils {

  public static List<FieldElement> generateSingleRow(int[] factors, FieldDefinition definition) {
    return Arrays.stream(factors).mapToObj(definition::createElement).collect(Collectors.toList());
  }

  /**
   * Converts integer matrix into field-element matrix.
   *
   * @param rows integer matrix
   * @param definition field definition
   * @return field element matrix
   */
  public static List<List<FieldElement>> generateMatrix(int[][] rows, FieldDefinition definition) {
    int numMults = rows.length;
    List<List<FieldElement>> input = new ArrayList<>(numMults);
    for (int[] leftFactorRow : rows) {
      List<FieldElement> row =
          Arrays.stream(leftFactorRow).mapToObj(definition::createElement)
              .collect(Collectors.toList());
      input.add(row);
    }
    return input;
  }
}
