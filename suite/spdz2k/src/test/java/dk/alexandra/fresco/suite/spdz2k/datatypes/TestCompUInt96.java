package dk.alexandra.fresco.suite.spdz2k.datatypes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Test;

public class TestCompUInt96 {

  private final BigInteger two = BigInteger.valueOf(2);
  private final BigInteger twoTo32 = BigInteger.ONE.shiftLeft(32);
  private final BigInteger twoTo64 = BigInteger.ONE.shiftLeft(64);
  private final BigInteger twoTo96 = BigInteger.ONE.shiftLeft(96);

  @Test
  public void testConstruct() {
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(BigInteger.ZERO).toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new CompUInt96(BigInteger.ONE).toBigInteger()
    );
    assertEquals(
        new BigInteger("42"),
        new CompUInt96(new BigInteger("42")).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new CompUInt96(twoTo32).toBigInteger()
    );
    assertEquals(
        twoTo32.subtract(BigInteger.ONE),
        new CompUInt96(twoTo32.subtract(BigInteger.ONE)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new CompUInt96(twoTo32.add(BigInteger.ONE)).toBigInteger()
    );
    assertEquals(
        twoTo64.subtract(BigInteger.ONE),
        new CompUInt96(twoTo64.subtract(BigInteger.ONE)).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new CompUInt96(twoTo64).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new CompUInt96(twoTo64.add(BigInteger.ONE)).toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(BigInteger.ONE),
        new CompUInt96(twoTo96.subtract(BigInteger.ONE)).toBigInteger()
    );
    assertEquals(
        BigInteger.valueOf(-1).mod(twoTo96),
        new CompUInt96(BigInteger.valueOf(-1)).toBigInteger()
    );
    assertEquals(
        BigInteger.valueOf(-2).mod(twoTo96),
        new CompUInt96(BigInteger.valueOf(-2)).toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(BigInteger.ONE).negate().mod(twoTo96),
        new CompUInt96(twoTo96.subtract(BigInteger.ONE).negate()).toBigInteger()
    );
  }

  @Test
  public void testAdd() {
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(0).add(new CompUInt96(0)).toBigInteger()
    );
    assertEquals(
        two,
        new CompUInt96(1).add(new CompUInt96(1)).toBigInteger()
    );
    assertEquals(
        twoTo32,
        new CompUInt96(twoTo32).add(new CompUInt96(0)).toBigInteger()
    );
    assertEquals(
        twoTo32.add(BigInteger.ONE),
        new CompUInt96(twoTo32).add(new CompUInt96(1)).toBigInteger()
    );
    assertEquals(
        twoTo64,
        new CompUInt96(twoTo64).add(new CompUInt96(0)).toBigInteger()
    );
    assertEquals(
        twoTo64.add(BigInteger.ONE),
        new CompUInt96(twoTo64).add(new CompUInt96(1)).toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(BigInteger.ONE),
        new CompUInt96(twoTo96.subtract(BigInteger.ONE)).add(new CompUInt96(0))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(twoTo96.subtract(BigInteger.ONE))
            .add(new CompUInt96(BigInteger.ONE)).toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(new BigInteger("10000000")).add(twoTo32.add(twoTo64)).mod(twoTo96),
        new CompUInt96(twoTo96.subtract(new BigInteger("10000000")))
            .add(new CompUInt96(twoTo32.add(twoTo64))).toBigInteger()
    );
    assertEquals(
        twoTo32.add(twoTo64).mod(twoTo96),
        new CompUInt96(twoTo32).add(new CompUInt96(twoTo64)).toBigInteger()
    );
  }

  @Test
  public void testMultiply() {
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(0).multiply(new CompUInt96(0)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(1).multiply(new CompUInt96(0)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(0).multiply(new CompUInt96(1)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(1024).multiply(new CompUInt96(0)).toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(twoTo96.subtract(BigInteger.ONE)).multiply(new CompUInt96(0))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new CompUInt96(1).multiply(new CompUInt96(1)).toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(BigInteger.ONE),
        new CompUInt96(new CompUInt96(1))
            .multiply(new CompUInt96(twoTo96.subtract(BigInteger.ONE)))
            .toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(BigInteger.ONE),
        new CompUInt96(twoTo96.subtract(BigInteger.ONE)).multiply(new CompUInt96(1))
            .toBigInteger()
    );
    assertEquals(
        new BigInteger("4294967306").multiply(new BigInteger("7415541639366192187")).mod(twoTo96),
        new CompUInt96(new BigInteger("4294967306"))
            .multiply(new CompUInt96(new BigInteger("7415541639366192187")))
            .toBigInteger()
    );
    // multiply no overflow
    assertEquals(
        new BigInteger("42").multiply(new BigInteger("7")),
        new CompUInt96(new BigInteger("42")).multiply(new CompUInt96(new BigInteger("7")))
            .toBigInteger()
    );
    // multiply with overflow
    assertEquals(
        new BigInteger("42").multiply(twoTo96.subtract(BigInteger.ONE)).mod(twoTo96),
        new CompUInt96(new BigInteger("42"))
            .multiply(new CompUInt96(twoTo96.subtract(BigInteger.ONE)))
            .toBigInteger()
    );
    assertEquals(
        twoTo64.multiply(twoTo64.add(BigInteger.TEN)).mod(twoTo96),
        new CompUInt96(twoTo64)
            .multiply(new CompUInt96(twoTo64.add(BigInteger.TEN)))
            .toBigInteger()
    );
  }

  @Test
  public void testNegate() {
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(BigInteger.ZERO).negate().toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new CompUInt96(twoTo96.subtract(BigInteger.ONE)).negate().toBigInteger()
    );
    assertEquals(
        two,
        new CompUInt96(twoTo96.subtract(two)).negate().toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(two),
        new CompUInt96(two).negate().toBigInteger()
    );
  }

  @Test
  public void testSubtract() {
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(BigInteger.ZERO).subtract(new CompUInt96(BigInteger.ZERO))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ONE,
        new CompUInt96(BigInteger.ONE).subtract(new CompUInt96(BigInteger.ZERO))
            .toBigInteger()
    );
    assertEquals(
        BigInteger.ZERO,
        new CompUInt96(BigInteger.ONE).subtract(new CompUInt96(BigInteger.ONE))
            .toBigInteger()
    );
    assertEquals(
        twoTo96.subtract(BigInteger.ONE),
        new CompUInt96(BigInteger.ONE).subtract(new CompUInt96(two))
            .toBigInteger()
    );
  }

  @Test
  public void testToByteArrayWithPadding() {
    byte[] bytes = new byte[]{0x42};
    UInt<CompUInt96> uint = new CompUInt96(bytes);
    byte[] expected = new byte[12];
    expected[expected.length - 1] = 0x42;
    byte[] actual = uint.toByteArray();
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testToByteArray() {
    byte[] bytes = new byte[12];
    new Random(1).nextBytes(bytes);
    UInt<CompUInt96> uint = new CompUInt96(bytes);
    byte[] actual = uint.toByteArray();
    assertArrayEquals(bytes, actual);
  }

  @Test
  public void testToByteArrayMore() {
    byte[] bytes = new byte[]{
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, // high
        0x02, 0x02, 0x02, 0x02, // low
    };
    UInt<CompUInt96> uint = new CompUInt96(bytes);
    byte[] actual = uint.toByteArray();
    assertArrayEquals(bytes, actual);
  }

  @Test
  public void testToLong() {
    assertEquals(Long.MAX_VALUE, new CompUInt96(Long.MAX_VALUE).toLong());
    assertEquals(0, new CompUInt96(0).toLong());
    assertEquals(1, new CompUInt96(1).toLong());
    assertEquals(twoTo64.longValue(), new CompUInt96(twoTo64).toLong());
  }

  @Test
  public void testGetBitLength() {
    CompUInt96 uint = new CompUInt96(1);
    assertEquals(96, uint.getBitLength());
    assertEquals(64, uint.getHighBitLength());
    assertEquals(32, uint.getLowBitLength());
  }

  @Test
  public void testToInt() {
    UInt<CompUInt96> uint = new CompUInt96(1);
    assertEquals(1, uint.toInt());
  }

  @Test
  public void testToString() {
    UInt<CompUInt96> uint = new CompUInt96(12135);
    assertEquals("12135", uint.toString());
  }

  @Test
  public void testIsZero() {
    assertTrue(new CompUInt96(0, 0,0).isZero());
    assertFalse(new CompUInt96(0, 0,1).isZero());
    assertFalse(new CompUInt96(0, 1,0).isZero());
    assertFalse(new CompUInt96(1, 0,0).isZero());
  }

}
