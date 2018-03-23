package dk.alexandra.fresco.suite.spdz2k.datatypes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Test;

public class TestGenericCompUInt {

  private final BigInteger two = BigInteger.valueOf(2);
  private final BigInteger twoTo32 = BigInteger.ONE.shiftLeft(32);
  private final BigInteger twoTo64 = BigInteger.ONE.shiftLeft(64);
  private final BigInteger twoTo128 = BigInteger.ONE.shiftLeft(128);

  @Test
  public void testConstruct() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(BigInteger.ZERO, 128).toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompUInt(BigInteger.ONE, 128).toBigInteger()
    );
    assertEquals(
        new BigInteger("42"),
        new GenericCompUInt(new BigInteger("42"), 128).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new GenericCompUInt(twoTo32, 128).toBigInteger()
    );
    assertEquals(
        twoTo32.subtract(BigInteger.ONE),
        new GenericCompUInt(twoTo32.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new GenericCompUInt(twoTo32.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64.subtract(BigInteger.ONE),
        new GenericCompUInt(twoTo64.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new GenericCompUInt(twoTo64, 128).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new GenericCompUInt(twoTo64.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128).toBigInteger()
    );
  }

  @Test
  public void testAdd() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(0, 128).add(new GenericCompUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        two,
        new GenericCompUInt(1, 128).add(new GenericCompUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new GenericCompUInt(twoTo32, 128).add(new GenericCompUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new GenericCompUInt(twoTo32, 128).add(new GenericCompUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new GenericCompUInt(twoTo64, 128).add(new GenericCompUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new GenericCompUInt(twoTo64, 128).add(new GenericCompUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128).add(new GenericCompUInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128)
            .add(new GenericCompUInt(BigInteger.ONE, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(new BigInteger("10000000")).add(twoTo32.add(twoTo64)).mod(twoTo128),
        new GenericCompUInt(twoTo128.subtract(new BigInteger("10000000")), 128)
            .add(new GenericCompUInt(twoTo32.add(twoTo64), 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(twoTo64).mod(twoTo128),
        new GenericCompUInt(twoTo32, 128).add(new GenericCompUInt(twoTo64, 128)).toBigInteger()
    );
  }

  @Test
  public void testMultiply() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(0, 128).multiply(new GenericCompUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(1, 128).multiply(new GenericCompUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(0, 128).multiply(new GenericCompUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(1024, 128).multiply(new GenericCompUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128)
            .multiply(new GenericCompUInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompUInt(1, 128).multiply(new GenericCompUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompUInt(1, 128)
            .multiply(new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128)
            .multiply(new GenericCompUInt(1, 128))
            .toBigInteger()
    );
    // multiply no overflow
    assertEquals(
        new BigInteger("42").multiply(new BigInteger("7")),
        new GenericCompUInt(new BigInteger("42"), 128)
            .multiply(new GenericCompUInt(new BigInteger("7"), 128))
            .toBigInteger()
    );
    // multiply with overflow
    assertEquals(
        new BigInteger("42").multiply(twoTo128.subtract(BigInteger.ONE)).mod(twoTo128),
        new GenericCompUInt(new BigInteger("42"), 128)
            .multiply(new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo64.multiply(twoTo64.add(BigInteger.TEN)).mod(twoTo128),
        new GenericCompUInt(twoTo64, 128)
            .multiply(new GenericCompUInt(twoTo64.add(BigInteger.TEN), 128))
            .toBigInteger()
    );
  }

  @Test
  public void testNegate() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(BigInteger.ZERO, 128).negate().toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompUInt(twoTo128.subtract(BigInteger.ONE), 128).negate().toBigInteger()
    );
    assertEquals(
        two,
        new GenericCompUInt(twoTo128.subtract(two), 128).negate().toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(two),
        new GenericCompUInt(two, 128).negate().toBigInteger()
    );
  }

  @Test
  public void testSubtract() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(BigInteger.ZERO, 128)
            .subtract(new GenericCompUInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompUInt(BigInteger.ONE, 128).subtract(new GenericCompUInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompUInt(BigInteger.ONE, 128).subtract(new GenericCompUInt(BigInteger.ONE, 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompUInt(BigInteger.ONE, 128).subtract(new GenericCompUInt(two, 128))
            .toBigInteger()
    );
  }

  @Test
  public void testToByteArrayWithPadding() {
    byte[] bytes = new byte[]{0x42};
    UInt<GenericCompUInt> uint = new GenericCompUInt(bytes, 128);
    byte[] expected = new byte[16];
    expected[expected.length - 1] = 0x42;
    byte[] actual = uint.toByteArray();
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testToByteArray() {
    byte[] bytes = new byte[16];
    new Random(1).nextBytes(bytes);
    UInt<GenericCompUInt> uint = new GenericCompUInt(bytes, 128);
    byte[] actual = uint.toByteArray();
    assertArrayEquals(bytes, actual);
  }

  @Test
  public void testToByteArrayMore() {
    byte[] bytes = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, // high
        0x02, 0x02, 0x02, 0x02, // mid
        0x03, 0x03, 0x02, 0x03  // low
    };
    UInt<GenericCompUInt> uint = new GenericCompUInt(bytes, 128);
    byte[] actual = uint.toByteArray();
    assertArrayEquals(bytes, actual);
  }

  @Test
  public void testIsZero() {
    assertTrue(new GenericCompUInt(0, 128).isZero());
    assertFalse(new GenericCompUInt(1, 128).isZero());
  }

  @Test
  public void testGetSubRange() {
    byte[] bytes = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
        0x02, 0x02, 0x02, 0x02, 0x03, 0x03, 0x02, 0x03
    };
    CompUInt<GenericCompUInt, GenericCompUInt, GenericCompUInt> uint = new GenericCompUInt(bytes,
        128);
    GenericCompUInt subLow = uint.getLeastSignificant();
    byte[] expectedSubRangeBytesLow = new byte[]{
        0x02, 0x02, 0x02, 0x02,
        0x03, 0x03, 0x02, 0x03
    };
    assertArrayEquals(expectedSubRangeBytesLow, subLow.toByteArray());
    GenericCompUInt subHigh = uint.getMostSignificant();
    byte[] expectedSubRangeBytesHigh = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
    };
    assertArrayEquals(expectedSubRangeBytesHigh, subHigh.toByteArray());
  }

  @Test
  public void testGetBitLengths() {
    GenericCompUInt uint3232 = new GenericCompUInt(new int[]{1, 0});
    assertEquals(32, uint3232.getHighBitLength());
    assertEquals(32, uint3232.getLowBitLength());
    assertEquals(64, uint3232.getCompositeBitLength());
    GenericCompUInt uint6432 = new GenericCompUInt(new int[]{2, 1, 0}, 32);
    assertEquals(64, uint6432.getHighBitLength());
    assertEquals(32, uint6432.getLowBitLength());
    assertEquals(96, uint6432.getCompositeBitLength());
  }

  @Test
  public void testShiftLowIntoHigh() {
    GenericCompUInt uint3232 = new GenericCompUInt(new int[]{2, 1});
    assertEquals(new GenericCompUInt(new int[]{1, 0}).toBigInteger(),
        uint3232.shiftLowIntoHigh().toBigInteger());
    GenericCompUInt uint6432 = new GenericCompUInt(new int[]{3, 2, 1}, 32);
    assertEquals(new GenericCompUInt(new int[]{0, 1, 0}).toBigInteger(),
        uint6432.shiftLowIntoHigh().toBigInteger());
    GenericCompUInt uint3264 = new GenericCompUInt(new int[]{3, 2, 1}, 64);
    assertEquals(new GenericCompUInt(new int[]{1, 0, 0}).toBigInteger(),
        uint3264.shiftLowIntoHigh().toBigInteger());
  }

  @Test
  public void testToInt() {
    GenericCompUInt uint3232 = new GenericCompUInt(new int[]{1111});
    assertEquals(1111, uint3232.toInt());
  }

  @Test
  public void testToLong() {
    GenericCompUInt uint3232 = new GenericCompUInt(new int[]{122, 1111});
    assertEquals(523986011223L, uint3232.toLong());
  }

  @Test
  public void testToString() {
    GenericCompUInt uint3232 = new GenericCompUInt(new int[]{122, 1111});
    assertEquals("523986011223", uint3232.toString());
  }

}
