package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MascotTestUtils {
  
  public static List<FieldElement> generateSingleRow(int[] factors, BigInteger modulus,
      int modBitLength) {
    return Arrays.stream(factors).mapToObj(val -> new FieldElement(val, modulus, modBitLength))
        .collect(Collectors.toList());
  }

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
  
}
