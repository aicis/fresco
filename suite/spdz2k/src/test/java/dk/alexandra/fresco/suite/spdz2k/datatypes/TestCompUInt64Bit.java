package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestCompUInt64Bit {

  private static final BigInteger twoTo64 = BigInteger.ONE.shiftLeft(64);

  private static int rand(int seed) {
    return new Random(seed).nextInt();
  }

  private static BigInteger bigInt64(BigInteger value33) {
    return value33.shiftLeft(31).mod(twoTo64);
  }

  private static BigInteger bigInt33(int high, int bit) {
    return BigInteger.valueOf(high).shiftLeft(1)
        .add(BigInteger.valueOf(bit))
        .mod(twoTo64);
  }

  @Test
  public void testConstruct() {
    Assert.assertEquals(BigInteger.valueOf(111).shiftLeft(32),
        new CompUInt64Bit(111, 0).toBigInteger());
    Assert.assertEquals(BigInteger.valueOf(rand(1)).shiftLeft(32).mod(twoTo64),
        new CompUInt64Bit(rand(1), 0).toBigInteger());
    Assert.assertEquals(
        BigInteger.valueOf(rand(2)).shiftLeft(32).add(BigInteger.valueOf(1L).shiftLeft(31))
            .mod(twoTo64),
        new CompUInt64Bit(rand(2), 1).toBigInteger());
  }

  @Test
  public void testBitValue() {
    Assert.assertEquals(
        0,
        new CompUInt64Bit(111, 0).bitValue());
    Assert.assertEquals(
        0,
        new CompUInt64Bit(rand(1), 0).bitValue());
    Assert.assertEquals(
        1,
        new CompUInt64Bit(rand(2), 1).bitValue());
    Assert.assertEquals(
        1,
        new CompUInt64Bit(0, 1).bitValue());
    Assert.assertEquals(
        0,
        new CompUInt64Bit(0, 0).bitValue());
  }

  @Test
  public void testMultiply() {
    Assert.assertEquals(
        bigInt64(bigInt33(rand(3), 0).multiply(bigInt33(rand(4), 0))),
        new CompUInt64Bit(rand(3), 0).multiply(new CompUInt64Bit(rand(4), 0)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt64(bigInt33(rand(3), 1).multiply(bigInt33(rand(4), 0))),
        new CompUInt64Bit(rand(3), 1).multiply(new CompUInt64Bit(rand(4), 0)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt64(bigInt33(1, 1).multiply(bigInt33(1, 1))),
        new CompUInt64Bit(1, 1).multiply(new CompUInt64Bit(1, 1)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt64(bigInt33(rand(3), 1).multiply(bigInt33(rand(4), 1))),
        new CompUInt64Bit(rand(3), 1).multiply(new CompUInt64Bit(rand(4), 1)).toBigInteger()
    );
  }

  @Test
  public void testAdd() {
    Assert.assertEquals(
        bigInt64(bigInt33(rand(3), 0).add(bigInt33(rand(4), 0))),
        new CompUInt64Bit(rand(3), 0).add(new CompUInt64Bit(rand(4), 0)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt64(bigInt33(rand(3), 1).add(bigInt33(rand(4), 0))).toString(2),
        new CompUInt64Bit(rand(3), 1).add(new CompUInt64Bit(rand(4), 0)).toBigInteger()
            .toString(2)
    );
    Assert.assertEquals(
        bigInt64(bigInt33(1, 1).add(bigInt33(1, 1))),
        new CompUInt64Bit(1, 1).add(new CompUInt64Bit(1, 1)).toBigInteger()
    );
    Assert.assertEquals(
        bigInt64(bigInt33(rand(3), 1).add(bigInt33(rand(4), 1))),
        new CompUInt64Bit(rand(3), 1).add(new CompUInt64Bit(rand(4), 1)).toBigInteger()
    );
  }

  @Test
  public void testSerializeLeastSignificant() {
    Assert.assertArrayEquals(
        new byte[]{0},
        new CompUInt64Bit(rand(42), 0).serializeLeastSignificant()
    );
    Assert.assertArrayEquals(
        new byte[]{1},
        new CompUInt64Bit(rand(42), 1).serializeLeastSignificant()
    );
  }

}
