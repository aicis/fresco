package dk.alexandra.fresco.suite.marlin.datatypes;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;

public class TestUInt {

  private final BigInteger two = BigInteger.valueOf(2);
  private final BigInteger twoTo32 = BigInteger.ONE.shiftLeft(32);
  private final BigInteger twoTo64 = BigInteger.ONE.shiftLeft(64);
  private final BigInteger twoTo128 = BigInteger.ONE.shiftLeft(128);

  @Test
  public void testConstruct() {
    assertEquals(
        BigInteger.ZERO,
        new UInt(BigInteger.ZERO, 128).toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new UInt(BigInteger.ONE, 128).toBigInteger()
    );
    assertEquals(
        new BigInteger("42"),
        new UInt(new BigInteger("42"), 128).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new UInt(twoTo32, 128).toBigInteger()
    );
    assertEquals(
        twoTo32.subtract(BigInteger.ONE),
        new UInt(twoTo32.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new UInt(twoTo32.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64.subtract(BigInteger.ONE),
        new UInt(twoTo64.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new UInt(twoTo64, 128).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new UInt(twoTo64.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new UInt(twoTo128.subtract(BigInteger.ONE), 128).toBigInteger()
    );
  }

}
