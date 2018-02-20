package dk.alexandra.fresco.suite.marlin.datatypes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Test;

public class TestGenericUInt {

  private final BigInteger two = BigInteger.valueOf(2);
  private final BigInteger twoTo32 = BigInteger.ONE.shiftLeft(32);
  private final BigInteger twoTo64 = BigInteger.ONE.shiftLeft(64);
  private final BigInteger twoTo128 = BigInteger.ONE.shiftLeft(128);

  @Test
  public void testConstruct() {
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(BigInteger.ZERO, 128).toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericUInt(BigInteger.ONE, 128).toBigInteger()
    );
    assertEquals(
        new BigInteger("42"),
        new GenericUInt(new BigInteger("42"), 128).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new GenericUInt(twoTo32, 128).toBigInteger()
    );
    assertEquals(
        twoTo32.subtract(BigInteger.ONE),
        new GenericUInt(twoTo32.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new GenericUInt(twoTo32.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64.subtract(BigInteger.ONE),
        new GenericUInt(twoTo64.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new GenericUInt(twoTo64, 128).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new GenericUInt(twoTo64.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128).toBigInteger()
    );
  }

  @Test
  public void testAdd() {
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(0, 128).add(new GenericUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        two,
        new GenericUInt(1, 128).add(new GenericUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new GenericUInt(twoTo32, 128).add(new GenericUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new GenericUInt(twoTo32, 128).add(new GenericUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new GenericUInt(twoTo64, 128).add(new GenericUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new GenericUInt(twoTo64, 128).add(new GenericUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128).add(new GenericUInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128)
            .add(new GenericUInt(BigInteger.ONE, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(new BigInteger("10000000")).add(twoTo32.add(twoTo64)).mod(twoTo128),
        new GenericUInt(twoTo128.subtract(new BigInteger("10000000")), 128)
            .add(new GenericUInt(twoTo32.add(twoTo64), 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(twoTo64).mod(twoTo128),
        new GenericUInt(twoTo32, 128).add(new GenericUInt(twoTo64, 128)).toBigInteger()
    );
  }

  @Test
  public void testMultiply() {
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(0, 128).multiply(new GenericUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(1, 128).multiply(new GenericUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(0, 128).multiply(new GenericUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(1024, 128).multiply(new GenericUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128).multiply(new GenericUInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericUInt(1, 128).multiply(new GenericUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericUInt(1, 128)
            .multiply(new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128).multiply(new GenericUInt(1, 128))
            .toBigInteger()
    );
    // multiply no overflow
    assertEquals(
        new BigInteger("42").multiply(new BigInteger("7")),
        new GenericUInt(new BigInteger("42"), 128)
            .multiply(new GenericUInt(new BigInteger("7"), 128))
            .toBigInteger()
    );
    // multiply with overflow
    assertEquals(
        new BigInteger("42").multiply(twoTo128.subtract(BigInteger.ONE)).mod(twoTo128),
        new GenericUInt(new BigInteger("42"), 128)
            .multiply(new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo64.multiply(twoTo64.add(BigInteger.TEN)).mod(twoTo128),
        new GenericUInt(twoTo64, 128)
            .multiply(new GenericUInt(twoTo64.add(BigInteger.TEN), 128))
            .toBigInteger()
    );
  }

  @Test
  public void testNegate() {
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(BigInteger.ZERO, 128).negate().toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericUInt(twoTo128.subtract(BigInteger.ONE), 128).negate().toBigInteger()
    );
    assertEquals(
        two,
        new GenericUInt(twoTo128.subtract(two), 128).negate().toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(two),
        new GenericUInt(two, 128).negate().toBigInteger()
    );
  }

  @Test
  public void testSubtract() {
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(BigInteger.ZERO, 128).subtract(new GenericUInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericUInt(BigInteger.ONE, 128).subtract(new GenericUInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericUInt(BigInteger.ONE, 128).subtract(new GenericUInt(BigInteger.ONE, 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericUInt(BigInteger.ONE, 128).subtract(new GenericUInt(two, 128))
            .toBigInteger()
    );
  }

  @Test
  public void testToByteArrayWithPadding() {
    byte[] bytes = new byte[]{0x42};
    UInt<GenericUInt> uint = new GenericUInt(bytes, 128);
    byte[] expected = new byte[16];
    expected[expected.length - 1] = 0x42;
    byte[] actual = uint.toByteArray();
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testToByteArray() {
    byte[] bytes = new byte[16];
    new Random(1).nextBytes(bytes);
    UInt<GenericUInt> uint = new GenericUInt(bytes, 128);
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
    UInt<GenericUInt> uint = new GenericUInt(bytes, 128);
    byte[] actual = uint.toByteArray();
    assertArrayEquals(bytes, actual);
  }

  @Test
  public void testIsZero() {
    assertTrue(new GenericUInt(0, 128).isZero());
    assertFalse(new GenericUInt(1, 128).isZero());
  }

  @Test
  public void testGetSubRange() {
    byte[] bytes = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, // high
        0x02, 0x02, 0x02, 0x02, // mid
        0x03, 0x03, 0x02, 0x03  // low
    };
    CompUInt<GenericUInt, GenericUInt, GenericUInt> uint = new GenericUInt(bytes, 128);
    GenericUInt subLow = uint.getLow();
    byte[] expectedSubRangeBytesLow = new byte[]{
        0x02, 0x02, 0x02, 0x02,
        0x03, 0x03, 0x02, 0x03
    };
    assertArrayEquals(expectedSubRangeBytesLow, subLow.toByteArray());
    GenericUInt subHigh = uint.getHigh();
    byte[] expectedSubRangeBytesHigh = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
    };
    assertArrayEquals(expectedSubRangeBytesHigh, subHigh.toByteArray());
  }
}
