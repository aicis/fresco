package dk.alexandra.fresco.framework;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class TestDRes {

  @Test
  public void testCreator() {
    BigInteger value = BigInteger.valueOf(123);
    DRes<BigInteger> deferred = DRes.of(value);
    Assert.assertEquals(deferred.out(), value);
  }

}
