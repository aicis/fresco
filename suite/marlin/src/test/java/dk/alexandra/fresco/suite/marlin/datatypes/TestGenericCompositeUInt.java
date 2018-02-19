package dk.alexandra.fresco.suite.marlin.datatypes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Test;

public class TestGenericCompositeUInt {

  private final BigInteger two = BigInteger.valueOf(2);
  private final BigInteger twoTo32 = BigInteger.ONE.shiftLeft(32);
  private final BigInteger twoTo64 = BigInteger.ONE.shiftLeft(64);
  private final BigInteger twoTo128 = BigInteger.ONE.shiftLeft(128);

  @Test
  public void testConstruct() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(BigInteger.ZERO, 128).toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompositeUInt(BigInteger.ONE, 128).toBigInteger()
    );
    assertEquals(
        new BigInteger("42"),
        new GenericCompositeUInt(new BigInteger("42"), 128).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new GenericCompositeUInt(twoTo32, 128).toBigInteger()
    );
    assertEquals(
        twoTo32.subtract(BigInteger.ONE),
        new GenericCompositeUInt(twoTo32.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new GenericCompositeUInt(twoTo32.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64.subtract(BigInteger.ONE),
        new GenericCompositeUInt(twoTo64.subtract(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new GenericCompositeUInt(twoTo64, 128).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new GenericCompositeUInt(twoTo64.add(BigInteger.ONE), 128).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128).toBigInteger()
    );
  }

  @Test
  public void testAdd() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(0, 128).add(new GenericCompositeUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        two,
        new GenericCompositeUInt(1, 128).add(new GenericCompositeUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new GenericCompositeUInt(twoTo32, 128).add(new GenericCompositeUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new GenericCompositeUInt(twoTo32, 128).add(new GenericCompositeUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new GenericCompositeUInt(twoTo64, 128).add(new GenericCompositeUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new GenericCompositeUInt(twoTo64, 128).add(new GenericCompositeUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128).add(new GenericCompositeUInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128)
            .add(new GenericCompositeUInt(BigInteger.ONE, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(new BigInteger("10000000")).add(twoTo32.add(twoTo64)).mod(twoTo128),
        new GenericCompositeUInt(twoTo128.subtract(new BigInteger("10000000")), 128)
            .add(new GenericCompositeUInt(twoTo32.add(twoTo64), 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(twoTo64).mod(twoTo128),
        new GenericCompositeUInt(twoTo32, 128).add(new GenericCompositeUInt(twoTo64, 128)).toBigInteger()
    );
  }

  @Test
  public void testMultiply() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(0, 128).multiply(new GenericCompositeUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(1, 128).multiply(new GenericCompositeUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(0, 128).multiply(new GenericCompositeUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(1024, 128).multiply(new GenericCompositeUInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128).multiply(new GenericCompositeUInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompositeUInt(1, 128).multiply(new GenericCompositeUInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompositeUInt(1, 128)
            .multiply(new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128).multiply(new GenericCompositeUInt(1, 128))
            .toBigInteger()
    );
    // multiply no overflow
    assertEquals(
        new BigInteger("42").multiply(new BigInteger("7")),
        new GenericCompositeUInt(new BigInteger("42"), 128).multiply(new GenericCompositeUInt(new BigInteger("7"), 128))
            .toBigInteger()
    );
    // multiply with overflow
    assertEquals(
        new BigInteger("42").multiply(twoTo128.subtract(BigInteger.ONE)).mod(twoTo128),
        new GenericCompositeUInt(new BigInteger("42"), 128)
            .multiply(new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo64.multiply(twoTo64.add(BigInteger.TEN)).mod(twoTo128),
        new GenericCompositeUInt(twoTo64, 128)
            .multiply(new GenericCompositeUInt(twoTo64.add(BigInteger.TEN), 128))
            .toBigInteger()
    );
  }

  @Test
  public void testNegate() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(BigInteger.ZERO, 128).negate().toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompositeUInt(twoTo128.subtract(BigInteger.ONE), 128).negate().toBigInteger()
    );
    assertEquals(
        two,
        new GenericCompositeUInt(twoTo128.subtract(two), 128).negate().toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(two),
        new GenericCompositeUInt(two, 128).negate().toBigInteger()
    );
  }

  @Test
  public void testSubtract() {
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(BigInteger.ZERO, 128).subtract(new GenericCompositeUInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new GenericCompositeUInt(BigInteger.ONE, 128).subtract(new GenericCompositeUInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new GenericCompositeUInt(BigInteger.ONE, 128).subtract(new GenericCompositeUInt(BigInteger.ONE, 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new GenericCompositeUInt(BigInteger.ONE, 128).subtract(new GenericCompositeUInt(two, 128))
            .toBigInteger()
    );
  }

  @Test
  public void testToByteArrayWithPadding() {
    byte[] bytes = new byte[]{0x42};
    CompositeUInt<GenericCompositeUInt> uint = new GenericCompositeUInt(bytes, 128);
    byte[] expected = new byte[16];
    expected[expected.length - 1] = 0x42;
    byte[] actual = uint.toByteArray();
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testToByteArray() {
    byte[] bytes = new byte[16];
    new Random(1).nextBytes(bytes);
    CompositeUInt<GenericCompositeUInt> uint = new GenericCompositeUInt(bytes, 128);
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
    CompositeUInt<GenericCompositeUInt> uint = new GenericCompositeUInt(bytes, 128);
    byte[] actual = uint.toByteArray();
    assertArrayEquals(bytes, actual);
  }

  @Test
  public void testIsZero() {
    assertTrue(new GenericCompositeUInt(0, 128).isZero());
    assertFalse(new GenericCompositeUInt(1, 128).isZero());
  }

  @Test
  public void testGetSubRange() {
    byte[] bytes = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, // high
        0x02, 0x02, 0x02, 0x02, // mid
        0x03, 0x03, 0x02, 0x03  // low
    };
    CompositeUInt<GenericCompositeUInt> uint = new GenericCompositeUInt(bytes, 128);
    CompositeUInt<GenericCompositeUInt> subLow = uint.getSubRange(0, 2);
    byte[] expectedSubRangeBytesLow = new byte[]{
        0x02, 0x02, 0x02, 0x02,
        0x03, 0x03, 0x02, 0x03
    };
    assertArrayEquals(expectedSubRangeBytesLow, subLow.toByteArray());
    CompositeUInt<GenericCompositeUInt> subHigh = uint.getSubRange(2, 4);
    byte[] expectedSubRangeBytesHigh = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
    };
    assertArrayEquals(expectedSubRangeBytesHigh, subHigh.toByteArray());
  }
}
