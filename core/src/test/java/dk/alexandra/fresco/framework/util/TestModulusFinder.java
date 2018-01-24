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

  @Test
  public void testFindSuitableMiss() {
    BigInteger actual = ModulusFinder.findSuitableModulus(1024);
    assertEquals(new BigInteger(
            "179769313486231590772930519078902473361797697894230657273430081157732675805500963132708477322407536021120113879871393357658789768814416622492847430639474124377767893424865485276302219601246094119453082952085005768838150682342462881473913110540827237163350510684586298239947245938479716304835356329624224137037"),
        actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindSuitableWrongBitLength() {
    ModulusFinder.findSuitableModulus(150);
  }

}
