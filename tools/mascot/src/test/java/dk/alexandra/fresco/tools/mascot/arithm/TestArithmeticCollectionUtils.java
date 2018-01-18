package dk.alexandra.fresco.tools.mascot.arithm;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestArithmeticCollectionUtils {

  private final ArithmeticCollectionUtils<MockAddable> utils = new ArithmeticCollectionUtils<>();

  @Test
  public void testTranspose() {
    int[][] rows = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {10, 11, 12}};
    List<List<FieldElement>> mat = MascotTestUtils.generateMatrix(rows, new BigInteger("251"));

    int[][] actualRows = {{1, 4, 7, 10}, {2, 5, 8, 11}, {3, 6, 9, 12}};
    List<List<FieldElement>> expected =
        MascotTestUtils.generateMatrix(actualRows, new BigInteger("251"));

    List<List<FieldElement>> actual = new ArithmeticCollectionUtils<FieldElement>().transpose(mat);
    CustomAsserts.assertMatrixEquals(expected, actual);
  }

  @Test
  public void testSum() {
    List<MockAddable> summands =
        Arrays.asList(new MockAddable(1), new MockAddable(2), new MockAddable(3));
    MockAddable expected = new MockAddable(6);
    MockAddable actual = utils.sum(summands);
    assertEquals(expected, actual);
  }

  @Test
  public void testPairWiseSum() {
    List<MockAddable> rowOne =
        Arrays.asList(new MockAddable(1), new MockAddable(2), new MockAddable(3));
    List<MockAddable> rowTwo =
        Arrays.asList(new MockAddable(4), new MockAddable(5), new MockAddable(6));
    List<MockAddable> expected =
        Arrays.asList(new MockAddable(5), new MockAddable(7), new MockAddable(9));
    List<MockAddable> actual = utils.sumRows(Arrays.asList(rowOne, rowTwo));
    assertEquals(expected, actual);
  }

  private class MockAddable implements Addable<MockAddable> {
    int value;

    public MockAddable(int value) {
      this.value = value;
    }

    @Override
    public MockAddable add(MockAddable other) {
      return new MockAddable(value + other.value);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + value;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      MockAddable other = (MockAddable) obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (value != other.value) {
        return false;
      }
      return true;
    }

    private TestArithmeticCollectionUtils getOuterType() {
      return TestArithmeticCollectionUtils.this;
    }
  }

}
