package dk.alexandra.fresco.tools.mascot.arithm;

import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;

public class TestTransposeUtils {

  @Test
  public void testTranspose() {
    int[][] rows = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {10, 11, 12}};
    List<List<FieldElement>> mat = MascotTestUtils.generateMatrix(rows, new BigInteger("251"));

    int[][] actualRows = {{1, 4, 7, 10}, {2, 5, 8, 11}, {3, 6, 9, 12}};
    List<List<FieldElement>> expected =
        MascotTestUtils.generateMatrix(actualRows, new BigInteger("251"));

    List<List<FieldElement>> actual = TransposeUtils.transpose(mat);
    CustomAsserts.assertMatrixEquals(expected, actual);
  }

}
