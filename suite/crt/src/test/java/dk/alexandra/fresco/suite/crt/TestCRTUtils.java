package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestCRTUtils {

  @Test
  public void testCRTConversion() {
    BigInteger p = BigInteger.valueOf(37);
    BigInteger q = BigInteger.valueOf(91);
    BigInteger x = BigInteger.valueOf(128);
    Pair<BigInteger, BigInteger> crt = Util.mapToCRT(x, p, q);
    BigInteger y = Util.mapToBigInteger(crt, p, q);
    Assert.assertEquals(x, y);
  }

  @Test
  public void testRandomSampling() {
    Random random = new Random(1234);
    BigInteger p = BigInteger.valueOf(10);

    for (int i = 0; i < 100; i++) {
      BigInteger x = Util.randomBigInteger(random, p);
      Assert.assertTrue(x.compareTo(p) < 0);
      Assert.assertTrue(x.compareTo(BigInteger.ZERO) >= 0);
    }
  }


}
