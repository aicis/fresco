package dk.alexandra.fresco.tools.mascot.arithm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestAddable {

  @Test
  public void testSum() {
    List<MockAddable> summands =
        Arrays.asList(new MockAddable(1), new MockAddable(2), new MockAddable(3));
    MockAddable expected = new MockAddable(6);
    MockAddable actual = Addable.sum(summands);
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
    List<MockAddable> actual = Addable.sumRows(Arrays.asList(rowOne, rowTwo));
    assertEquals(expected, actual);
  }

  private class MockAddable implements Addable<MockAddable> {
    int value;

    MockAddable(int value) {
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

    private TestAddable getOuterType() {
      return TestAddable.this;
    }
  }

}
