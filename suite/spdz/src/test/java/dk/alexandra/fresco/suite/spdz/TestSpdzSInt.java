package dk.alexandra.fresco.suite.spdz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import org.junit.Test;

public class TestSpdzSInt {

  @Test
  public void testEquals() {
    SpdzSInt element = new SpdzSInt(getI(25), getI(15));

    assertTrue(element.equals(element));
    assertFalse(element.equals("This is a String"));
    assertFalse(element.equals(null));

    SpdzSInt element2 = new SpdzSInt(getI(25), null);
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(getI(25), getI(11)
    );
    assertFalse(element.equals(element2));
    element = new SpdzSInt(getI(25), null);
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(getI(25), null);
    assertTrue(element.equals(element2));

    element2 = new SpdzSInt(getI(25), null);
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(getI(25), null);
    assertFalse(element.equals(element2));
    element = new SpdzSInt(getI(25), null);
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(getI(25), null);
    assertTrue(element.equals(element2));

    element = new SpdzSInt(null, getI(11));
    element2 = new SpdzSInt(getI(25), getI(11));
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(null, getI(11));
    assertTrue(element.equals(element2));
    element = new SpdzSInt(getI(25), getI(11));
    assertFalse(element.equals(element2));
  }

  private FieldElement getI(int i) {
    return BigInt.fromConstant(get(i), get(123456));
  }

  private BigInteger get(int i) {
    return BigInteger.valueOf(i);
  }

  @Test
  public void testHashCode() {
    SpdzSInt e1 = new SpdzSInt(getI(25), getI(15));
    SpdzSInt e2 = new SpdzSInt(null, getI(15));
    SpdzSInt e3 = new SpdzSInt(getI(25), null);
    SpdzSInt e4 = new SpdzSInt(getI(25), getI(15));
    assertAllDifferent(new int[]{
        e1.hashCode(),
        e2.hashCode(),
        e3.hashCode(),
        e4.hashCode()
    });
  }

  private void assertAllDifferent(int[] elements) {
    for (int i = 0; i < elements.length; i++) {
      for (int j = 0; j < elements.length; j++) {
        if (i != j) {
          assertNotEquals(elements[i], elements[j]);
        }
      }
    }
  }
}
