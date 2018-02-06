package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestPair {

  @Test
  public void testPair() {
    Pair<String, Integer> pair = new Pair<>("Test", 5);
    assertEquals("Test", pair.getFirst());
    assertEquals(5, pair.getSecond().intValue());
  }

  @Test
  public void testLazy() {
    Pair<String, Integer> pair = Pair.lazy("Test", 5).out();
    assertEquals("Test", pair.getFirst());
    assertEquals(5, pair.getSecond().intValue());
  }

  @Test
  public void testEqualsAndHashCode() {
    Pair<String, Integer> pair1 = new Pair<>("Foo", 5);
    assertTrue(pair1.equals(pair1));
    assertTrue(pair1.hashCode() == pair1.hashCode());
    Pair<String, Integer> pair2 = new Pair<>("Foo", 5);
    assertTrue(pair1.equals(pair2) && pair2.equals(pair1));
    assertTrue(pair1.hashCode() == pair2.hashCode());
    Pair<String, Integer> pair3 = new Pair<>("Foo", 3);
    assertFalse(pair1.equals(pair3) || pair3.equals(pair1));
    Pair<String, Integer> pair4 = new Pair<>("Bar", 5);
    assertFalse(pair1.equals(pair4) || pair4.equals(pair1));
    assertFalse(pair1.equals(null));
    assertFalse(pair1.equals("Baz"));
  }

  @Test
  public void testToString() {
    Pair<String, Integer> pair1 = new Pair<>("Foo", 5);
    assertEquals("<Foo, 5>", pair1.toString());
  }

}
