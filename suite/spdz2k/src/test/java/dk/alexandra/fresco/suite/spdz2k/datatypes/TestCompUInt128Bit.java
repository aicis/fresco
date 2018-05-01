package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestCompUInt128Bit {

  private final BigInteger twoTo128 = BigInteger.ONE.shiftLeft(128);

  private static long rand(int seed) {
    return new Random(seed).nextLong();
  }

  @Test
  public void testConstruct() {
    Assert.assertEquals(BigInteger.valueOf(111).shiftLeft(64),
        new CompUInt128Bit(111, 0).toBigInteger());
    Assert.assertEquals(BigInteger.valueOf(rand(1)).shiftLeft(64).mod(twoTo128),
        new CompUInt128Bit(rand(1), 0).toBigInteger());
    Assert.assertEquals(
        BigInteger.valueOf(rand(2)).shiftLeft(64).add(BigInteger.valueOf(1L).shiftLeft(63))
            .mod(twoTo128),
        new CompUInt128Bit(rand(2), 1).toBigInteger());
  }

  @Test
  public void testValueBit() {
    Assert.assertEquals(
        false,
        new CompUInt128Bit(111, 0).getValueBit());
    Assert.assertEquals(
        false,
        new CompUInt128Bit(rand(1), 0).getValueBit());
    Assert.assertEquals(
        true,
        new CompUInt128Bit(rand(2), 1).getValueBit());
    Assert.assertEquals(
        true,
        new CompUInt128Bit(0, 1).getValueBit());
    Assert.assertEquals(
        false,
        new CompUInt128Bit(0, 0).getValueBit());
  }

}
