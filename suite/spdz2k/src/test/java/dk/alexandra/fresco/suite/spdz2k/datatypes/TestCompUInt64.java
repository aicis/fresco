package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class TestCompUInt64 {

  @Test
  public void clearAboveBitAt() {
    for (int i = 0; i < Long.SIZE - 2; i++) {
      BigInteger input = BigInteger.ONE.shiftLeft(i + 1).add(BigInteger.ONE);
      final BigInteger actual = new CompUInt64(input).clearAboveBitAt(i + 1).toBigInteger();
      Assert.assertEquals("Failed at " + i + " expected 1 but was " + actual,
          BigInteger.ONE,
          actual);
    }
  }

  @Test
  public void clearHighBits() {
    Assert.assertEquals(
        new CompUInt64(1234L).toBigInteger(),
        new CompUInt64((12L << 34) + 1234L).clearHighBits().toBigInteger());
  }

  @Test
  public void toBitRep() {
  }

  @Test
  public void testBit() {
    Assert.assertEquals(
        true, new CompUInt64(BigInteger.ONE.shiftLeft(30).longValue()).testBit(30));
    Assert.assertEquals(
        false, new CompUInt64(BigInteger.ONE.shiftLeft(30).longValue()).testBit(29));
    Assert.assertEquals(
        true, new CompUInt64(BigInteger.ONE.longValue()).testBit(0));
    Assert.assertEquals(
        false, new CompUInt64(BigInteger.ONE.longValue()).testBit(1));
  }

  @Test
  public void serializeLeastSignificant() {
    long input = 1231139128938112323L;
    Assert.assertArrayEquals(
        ByteAndBitConverter.toByteArray((int) input),
        new CompUInt64(input).serializeLeastSignificant());
  }

  @Test
  public void toByteArray() {
    long input = 1231139128938112323L;
    Assert.assertArrayEquals(
        ByteAndBitConverter.toByteArray(input),
        new CompUInt64(input).toByteArray());
  }
}
