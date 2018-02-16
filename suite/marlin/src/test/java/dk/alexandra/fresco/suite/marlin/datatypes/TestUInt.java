package dk.alexandra.fresco.suite.marlin.datatypes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;
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

  @Test
  public void testAdd() {
    assertEquals(
        BigInteger.ZERO,
        new UInt(0, 128).add(new UInt(0, 128)).toBigInteger()
    );
    assertEquals(
        two,
        new UInt(1, 128).add(new UInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new UInt(twoTo32, 128).add(new UInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new UInt(twoTo32, 128).add(new UInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new UInt(twoTo64, 128).add(new UInt(0, 128)).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new UInt(twoTo64, 128).add(new UInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new UInt(twoTo128.subtract(BigInteger.ONE), 128).add(new UInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new UInt(twoTo128.subtract(BigInteger.ONE), 128)
            .add(new UInt(BigInteger.ONE, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(new BigInteger("10000000")).add(twoTo32.add(twoTo64)).mod(twoTo128),
        new UInt(twoTo128.subtract(new BigInteger("10000000")), 128)
            .add(new UInt(twoTo32.add(twoTo64), 128)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(twoTo64).mod(twoTo128),
        new UInt(twoTo32, 128).add(new UInt(twoTo64, 128)).toBigInteger()
    );
  }

  @Test
  public void testMultiply() {
    assertEquals(
        BigInteger.ZERO,
        new UInt(0, 128).multiply(new UInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new UInt(1, 128).multiply(new UInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new UInt(0, 128).multiply(new UInt(1, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new UInt(1024, 128).multiply(new UInt(0, 128)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new UInt(twoTo128.subtract(BigInteger.ONE), 128).multiply(new UInt(0, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new UInt(1, 128).multiply(new UInt(1, 128)).toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new UInt(1, 128)
            .multiply(new UInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new UInt(twoTo128.subtract(BigInteger.ONE), 128).multiply(new UInt(1, 128))
            .toBigInteger()
    );
    // multiply no overflow
    assertEquals(
        new BigInteger("42").multiply(new BigInteger("7")),
        new UInt(new BigInteger("42"), 128).multiply(new UInt(new BigInteger("7"), 128))
            .toBigInteger()
    );
    // multiply with overflow
    assertEquals(
        new BigInteger("42").multiply(twoTo128.subtract(BigInteger.ONE)).mod(twoTo128),
        new UInt(new BigInteger("42"), 128)
            .multiply(new UInt(twoTo128.subtract(BigInteger.ONE), 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo64.multiply(twoTo64.add(BigInteger.TEN)).mod(twoTo128),
        new UInt(twoTo64, 128)
            .multiply(new UInt(twoTo64.add(BigInteger.TEN), 128))
            .toBigInteger()
    );
  }

  @Test
  public void testNegate() {
    assertEquals(
        BigInteger.ZERO,
        new UInt(BigInteger.ZERO, 128).negate().toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new UInt(twoTo128.subtract(BigInteger.ONE), 128).negate().toBigInteger()
    );
    assertEquals(
        two,
        new UInt(twoTo128.subtract(two), 128).negate().toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(two),
        new UInt(two, 128).negate().toBigInteger()
    );
  }

  @Test
  public void testSubtract() {
    assertEquals(
        BigInteger.ZERO,
        new UInt(BigInteger.ZERO, 128).subtract(new UInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new UInt(BigInteger.ONE, 128).subtract(new UInt(BigInteger.ZERO, 128))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new UInt(BigInteger.ONE, 128).subtract(new UInt(BigInteger.ONE, 128))
            .toBigInteger()
    );
    assertEquals(
        twoTo128.subtract(BigInteger.ONE),
        new UInt(BigInteger.ONE, 128).subtract(new UInt(two, 128))
            .toBigInteger()
    );
  }

  @Test
  public void testToByteArrayWithPadding() {
    byte[] bytes = new byte[]{0x42};
    BigUInt<UInt> uint = new UInt(bytes, 128);
    byte[] expected = new byte[16];
    expected[expected.length - 1] = 0x42;
    byte[] actual = uint.toByteArray();
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testToByteArray() {
    byte[] bytes = new byte[16];
    new Random(1).nextBytes(bytes);
    BigUInt<UInt> uint = new UInt(bytes, 128);
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
    BigUInt<UInt> uint = new UInt(bytes, 128);
    byte[] actual = uint.toByteArray();
    assertArrayEquals(bytes, actual);
  }

}
