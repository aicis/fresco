package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestTransposeUtils {

  @Test
  public void testTranspose() {
    List<List<Integer>> mat = new ArrayList<>(4);
    mat.add(Arrays.asList(1, 2, 3));
    mat.add(Arrays.asList(4, 5, 6));
    mat.add(Arrays.asList(7, 8, 9));
    mat.add(Arrays.asList(10, 11, 12));

    List<List<Integer>> expected = new ArrayList<>(3);
    expected.add(Arrays.asList(1, 4, 7, 10));
    expected.add(Arrays.asList(2, 5, 8, 11));
    expected.add(Arrays.asList(3, 6, 9, 12));
    List<List<Integer>> actual = TransposeUtils.transpose(mat);
    assertEquals(expected, actual);
  }

}
