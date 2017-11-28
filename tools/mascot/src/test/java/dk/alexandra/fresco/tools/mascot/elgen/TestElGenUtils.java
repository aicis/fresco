package dk.alexandra.fresco.tools.mascot.elgen;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestElGenUtils {

  // TODO
  
  @Test
  public void testTranspose() {
    int[][] rows = {
        {1, 2, 3}, 
        {4, 5, 6}, 
        {7, 8, 9}, 
        {10, 11, 12}
    };
    List<List<FieldElement>> mat =
        MascotTestUtils.generateLeftInput(rows, new BigInteger("251"), 8);

    int[][] actualRows = {
        {1, 4, 7, 10},
        {2, 5, 8, 11},
        {3, 6, 9, 12}
    };
    List<List<FieldElement>> expected =
        MascotTestUtils.generateLeftInput(actualRows, new BigInteger("251"), 8);
    List<List<FieldElement>> actual = ElGenUtils.transpose(mat);
    assertEquals(expected, actual);
  }

}
