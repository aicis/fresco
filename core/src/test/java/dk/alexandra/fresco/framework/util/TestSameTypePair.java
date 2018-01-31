package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSameTypePair {

  @Test
  public void testConstructor() {
    Integer left = 1;
    Integer right = 2;
    SameTypePair<Integer> pair = new SameTypePair<>(left, right);
    assertEquals(new Integer(1), pair.getFirst());
    assertEquals(new Integer(2), pair.getSecond());
  }
  
}
