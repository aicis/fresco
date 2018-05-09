package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestCompUInt128Bit {

  private static final BigInteger twoTo128 = BigInteger.ONE.shiftLeft(128);

  private static long rand(int seed) {
    return new Random(seed).nextLong();
  }

  private static BigInteger bigInt128(BigInteger value65) {
    return value65.shiftLeft(63).mod(twoTo128);
  }

  private static BigInteger bigInt65(long high, int bit) {
    return BigInteger.valueOf(high).shiftLeft(1)
        .add(BigInteger.valueOf(bit))
        .mod(twoTo128);
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
  public void testBitValue() {
    Assert.assertEquals(
        0,
        new CompUInt128Bit(111, 0).bitValue());
    Assert.assertEquals(
        0,
        new CompUInt128Bit(rand(1), 0).bitValue());
    Assert.assertEquals(
        1,
        new CompUInt128Bit(rand(2), 1).bitValue());
    Assert.assertEquals(
        1,
        new CompUInt128Bit(0, 1).bitValue());
    Assert.assertEquals(
        0,
        new CompUInt128Bit(0, 0).bitValue());
  }

  @Test
  public void testMultiply() {
    Assert.assertEquals(
        bigInt128(bigInt65(rand(3), 0).multiply(bigInt65(rand(4), 0))),
        new CompUInt128Bit(rand(3), 0).multiply(new CompUInt128Bit(rand(4), 0)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt128(bigInt65(rand(3), 1).multiply(bigInt65(rand(4), 0))),
        new CompUInt128Bit(rand(3), 1).multiply(new CompUInt128Bit(rand(4), 0)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt128(bigInt65(1, 1).multiply(bigInt65(1, 1))),
        new CompUInt128Bit(1, 1).multiply(new CompUInt128Bit(1, 1)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt128(bigInt65(rand(3), 1).multiply(bigInt65(rand(4), 1))),
        new CompUInt128Bit(rand(3), 1).multiply(new CompUInt128Bit(rand(4), 1)).toBigInteger()
    );
  }

  @Test
  public void testAdd() {
    Assert.assertEquals(
        bigInt128(bigInt65(rand(3), 0).add(bigInt65(rand(4), 0))),
        new CompUInt128Bit(rand(3), 0).add(new CompUInt128Bit(rand(4), 0)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt128(bigInt65(rand(3), 1).add(bigInt65(rand(4), 0))).toString(2),
        new CompUInt128Bit(rand(3), 1).add(new CompUInt128Bit(rand(4), 0)).toBigInteger()
            .toString(2)
    );
    Assert.assertEquals(
        bigInt128(bigInt65(1, 1).add(bigInt65(1, 1))),
        new CompUInt128Bit(1, 1).add(new CompUInt128Bit(1, 1)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt128(bigInt65(rand(3), 1).add(bigInt65(rand(4), 1))),
        new CompUInt128Bit(rand(3), 1).add(new CompUInt128Bit(rand(4), 1)).toBigInteger()
    );
  }

  @Test
  public void testSerializeLeastSignificant() {
    Assert.assertArrayEquals(
        new byte[]{0},
        new CompUInt128Bit(rand(42), 0).serializeLeastSignificant()
    );
    Assert.assertArrayEquals(
        new byte[]{1},
        new CompUInt128Bit(rand(42), 1).serializeLeastSignificant()
    );
  }

}
