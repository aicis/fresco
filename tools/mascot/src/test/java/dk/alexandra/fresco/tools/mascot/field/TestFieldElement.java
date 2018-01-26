package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;

import org.junit.Test;

public class TestFieldElement {

  private final BigInteger modulus = new BigInteger("251");
  private final int bitLength = 8;

  // Positive tests

  @Test
  public void testFieldElementConstructors() {
    FieldElement elOne = new FieldElement(new BigInteger("11"), modulus);
    FieldElement elTwo = new FieldElement(11, modulus);
    FieldElement elThree = new FieldElement(elTwo);
    FieldElement elFour = new FieldElement("11", "251");
    CustomAsserts.assertEquals(elOne, elTwo);
    CustomAsserts.assertEquals(elOne, elThree);
    CustomAsserts.assertEquals(elOne, elFour);
  }

  @Test
  public void testAdd() {
    FieldElement elOne = new FieldElement(22, modulus);
    FieldElement elTwo = new FieldElement(11, modulus);
    FieldElement expected = new FieldElement(33, modulus);
    CustomAsserts.assertEquals(expected, elOne.add(elTwo));
  }

  @Test
  public void testPow() {
    FieldElement elOne = new FieldElement(22, modulus);
    FieldElement expected = new FieldElement(233, modulus);
    CustomAsserts.assertEquals(expected, elOne.pow(2));
  }

  @Test
  public void testSubtract() {
    FieldElement elOne = new FieldElement(22, modulus);
    FieldElement elTwo = new FieldElement(11, modulus);
    FieldElement expected = new FieldElement(11, modulus);
    CustomAsserts.assertEquals(expected, elOne.subtract(elTwo));
  }

  @Test
  public void testMultiply() {
    FieldElement elOne = new FieldElement(22, modulus);
    FieldElement elTwo = new FieldElement(11, modulus);
    FieldElement expected = new FieldElement(242, modulus);
    CustomAsserts.assertEquals(expected, elOne.multiply(elTwo));
  }

  @Test
  public void testNegate() {
    FieldElement elOne = new FieldElement(22, modulus);
    FieldElement expected = new FieldElement(229, modulus);
    CustomAsserts.assertEquals(expected, elOne.negate());
  }

  @Test
  public void testIsZero() {
    FieldElement zero = new FieldElement(0, modulus);
    FieldElement notZero = new FieldElement(1, modulus);
    assertTrue(zero.isZero());
    assertFalse(notZero.isZero());
  }

  @Test
  public void testToBigInteger() {
    assertEquals(new BigInteger("22"), new FieldElement(22, modulus).toBigInteger());
  }

  @Test
  public void testGetBit() {
    assertEquals(false, new FieldElement(22, modulus).getBit(0));
    assertEquals(true, new FieldElement(22, modulus).getBit(1));
  }

  @Test
  public void testSelect() {
    FieldElement el = new FieldElement(22, modulus);
    CustomAsserts.assertEquals(el, el.select(true));
    CustomAsserts.assertEquals(new FieldElement(BigInteger.ZERO, modulus),
        el.select(false));
  }

  @Test
  public void testConvertToBitVectorSingleByte() {
    FieldElement el = new FieldElement("11", "251");
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesSmall() {
    FieldElement el = new FieldElement("11", "65521");
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0x0, (byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesLarge() {
    FieldElement el = new FieldElement("65520", "65521");
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xFF, (byte) 0xF0};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorAndBack() {
    FieldElement el = new FieldElement("777", "65521");
    StrictBitVector bv = el.toBitVector();
    FieldElement actual = new FieldElement(bv.toByteArray(), new BigInteger("65521"));
    CustomAsserts.assertEquals(el, actual);
  }

  @Test
  public void testGetters() {
    FieldElement el = new FieldElement("777", "65521");
    assertEquals(new BigInteger("65521"), el.getModulus());
    assertEquals(16, el.getBitLength());
  }

  @Test
  public void testToString() {
    FieldElement el = new FieldElement("777", "65521");
    assertEquals("FieldElement [value=777, modulus=65521, bitLength=16]", el.toString());
  }

  @Test
  public void testModInverse() {
    BigInteger raw = new BigInteger("121");
    FieldElement el = new FieldElement(raw, modulus);
    FieldElement actual = el.modInverse();
    FieldElement expected = new FieldElement(raw.modInverse(modulus), modulus);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testSqrt() {
    FieldElement el = new FieldElement(123, modulus);
    FieldElement expected = new FieldElement(25, modulus);
    FieldElement actual = el.sqrt();
    CustomAsserts.assertEquals(expected, actual);
  }

  // Negative tests

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegative() {
    new FieldElement(-111, modulus);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegativeMod() {
    new FieldElement(111, BigInteger.valueOf(-251));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckBitLengthMismatch() {
    new FieldElement(111, BigInteger.valueOf(1111));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckValueTooLarge() {
    new FieldElement(252, BigInteger.valueOf(251));
  }

}
