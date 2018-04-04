package dk.alexandra.fresco.suite.spdz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import org.junit.Test;

public class TestSpdzSInt {

  @Test
  public void testEquals() {
    SpdzSInt element = new SpdzSInt(BigInteger.valueOf(25), BigInteger.valueOf(15),
        BigInteger.valueOf(251));

    assertTrue(element.equals(element));
    assertFalse(element.equals("This is a String"));
    assertFalse(element.equals(null));

    SpdzSInt element2 = new SpdzSInt(BigInteger.valueOf(25), null, BigInteger.valueOf(251));
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(BigInteger.valueOf(25), BigInteger.valueOf(11),
        BigInteger.valueOf(251));
    assertFalse(element.equals(element2));
    element = new SpdzSInt(BigInteger.valueOf(25), null, BigInteger.valueOf(251));
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(BigInteger.valueOf(25), null, BigInteger.valueOf(251));
    assertTrue(element.equals(element2));

    element2 = new SpdzSInt(BigInteger.valueOf(25), null, null);
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(BigInteger.valueOf(25), null, BigInteger.valueOf(23));
    assertFalse(element.equals(element2));
    element = new SpdzSInt(BigInteger.valueOf(25), null, null);
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(BigInteger.valueOf(25), null, null);
    assertTrue(element.equals(element2));

    element = new SpdzSInt(null, BigInteger.valueOf(11), BigInteger.valueOf(13));
    element2 = new SpdzSInt(BigInteger.valueOf(25), BigInteger.valueOf(11), BigInteger.valueOf(13));
    assertFalse(element.equals(element2));
    element2 = new SpdzSInt(null, BigInteger.valueOf(11), BigInteger.valueOf(13));
    assertTrue(element.equals(element2));
    element = new SpdzSInt(BigInteger.valueOf(25), BigInteger.valueOf(11), BigInteger.valueOf(13));
    assertFalse(element.equals(element2));
  }

  @Test
  public void testHashCode() {
    SpdzSInt e1 = new SpdzSInt(BigInteger.valueOf(25), BigInteger.valueOf(15),
        BigInteger.valueOf(251));
    SpdzSInt e2 = new SpdzSInt(null, BigInteger.valueOf(15), BigInteger.valueOf(251));
    SpdzSInt e3 = new SpdzSInt(BigInteger.valueOf(25), null, BigInteger.valueOf(251));
    SpdzSInt e4 = new SpdzSInt(BigInteger.valueOf(25), BigInteger.valueOf(15), null);
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
