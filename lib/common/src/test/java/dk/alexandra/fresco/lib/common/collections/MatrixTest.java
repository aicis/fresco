package dk.alexandra.fresco.lib.common.collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import org.junit.Assert;
import org.junit.Test;

public class MatrixTest {

  @Test
  public void testToString() {
    BiFunction<Integer, Integer, Integer> mockFunction = mock(BiFunction.class);
    when(mockFunction.apply(any(Integer.class), any(Integer.class))).thenReturn(0);
    Matrix matrix = new Matrix(1, 2, mockFunction);

    String stringOut = matrix.toString();
    Assert.assertTrue(stringOut.contains("width=2,"));
    Assert.assertTrue(stringOut.contains("height=1,"));
    Assert.assertTrue(stringOut.contains("matrix=[[0, 0]]"));
  }
}
