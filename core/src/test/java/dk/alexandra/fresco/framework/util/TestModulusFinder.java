package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;

public class TestModulusFinder {

  @Test
  public void testFindSuitableHit() {
    BigInteger expected = new BigInteger("65519");
    assertEquals(expected, ModulusFinder.findSuitableModulus(16));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFindSuitableMiss() {
    ModulusFinder.findSuitableModulus(1024);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindSuitableWrongBitLength() {
    ModulusFinder.findSuitableModulus(150);
  }

}
