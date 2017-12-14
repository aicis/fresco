package dk.alexandra.fresco.tools.mascot;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;

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
  
  private void checkTriple(MultTriple triple, FieldElement macKey) {
    AuthenticatedElement left = triple.getLeft();
    AuthenticatedElement right = triple.getRight();
    AuthenticatedElement product = triple.getProduct();

    // check values
    FieldElement leftValue = left.getShare();
    FieldElement rightValue = right.getShare();
    FieldElement productValue = product.getShare();
    assertEquals(leftValue.multiply(rightValue), productValue);

    // check macs
    FieldElement leftMac = left.getMac();
    FieldElement rightMac = right.getMac();
    FieldElement productMac = product.getMac();
    assertEquals(leftMac.multiply(rightMac), productMac.multiply(macKey));
  }
  
}
