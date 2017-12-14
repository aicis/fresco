package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.math.BigInteger;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class TestFieldElement {

  private final BigInteger modulus = new BigInteger("251");
  private final int bitLength = 8;

  private <T> void setField(FieldElement element, String fieldName, T value) {
    ExceptionConverter.safe(() -> {
      Class<?> feClass = element.getClass();
      Field modulusField = feClass.getDeclaredField(fieldName);
      modulusField.setAccessible(true);
      modulusField.set(element, value);
      return null;
    }, "Reflection broke");
  }
  
  // Positive tests

  @Test
  public void testFieldElementConstructors() {
    FieldElement elOne = new FieldElement(new BigInteger("11"), modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement elThree = new FieldElement(elTwo);
    FieldElement elFour = new FieldElement("11", "251", bitLength);
    assertEquals(elOne, elTwo);
    assertEquals(elOne, elThree);
    assertEquals(elOne, elFour);
  }

  @Test
  public void testAdd() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement expected = new FieldElement(33, modulus, bitLength);
    assertEquals(expected, elOne.add(elTwo));
  }

  @Test
  public void testPow() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement expected = new FieldElement(233, modulus, bitLength);
    assertEquals(expected, elOne.pow(2));
  }

  @Test
  public void testSubtract() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement expected = new FieldElement(11, modulus, bitLength);
    assertEquals(expected, elOne.subtract(elTwo));
  }

  @Test
  public void testMultiply() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement elTwo = new FieldElement(11, modulus, bitLength);
    FieldElement expected = new FieldElement(242, modulus, bitLength);
    assertEquals(expected, elOne.multiply(elTwo));
  }

  @Test
  public void testNegate() {
    FieldElement elOne = new FieldElement(22, modulus, bitLength);
    FieldElement expected = new FieldElement(229, modulus, bitLength);
    assertEquals(expected, elOne.negate());
  }

  @Test
  public void testToBigInteger() {
    assertEquals(new BigInteger("22"), new FieldElement(22, modulus, bitLength).toBigInteger());
  }
  
  @Test
  public void testGetBit() {
    assertEquals(false, new FieldElement(22, modulus, bitLength).getBit(0));
    assertEquals(true, new FieldElement(22, modulus, bitLength).getBit(1));
  }
  
  @Test
  public void testSelect() {
    FieldElement el = new FieldElement(22, modulus, bitLength);
    assertEquals(el, el.select(true));
    assertEquals(new FieldElement(BigInteger.ZERO, modulus, bitLength), el.select(false));
  }

  @Test
  public void testConvertToBitVectorSingleByte() {
    FieldElement el = new FieldElement("11", "251", 8);
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesSmall() {
    FieldElement el = new FieldElement("11", "65521", 16);
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0x0, (byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesLarge() {
    FieldElement el = new FieldElement("65520", "65521", 16);
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xFF, (byte) 0xF0};
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorAndBack() {
    FieldElement el = new FieldElement("777", "65521", 16);
    StrictBitVector bv = el.toBitVector();
    FieldElement actual = new FieldElement(bv.toByteArray(), new BigInteger("65521"), 16);
    assertEquals(el, actual);
  }
  
  @Test
  public void testGetters() {
    FieldElement el = new FieldElement("777", "65521", 16);
    assertEquals(new BigInteger("65521"), el.getModulus());
    assertEquals(16, el.getBitLength());
  }
  
  @Test
  public void testToString() {
    FieldElement el = new FieldElement("777", "65521", 16);
    assertEquals("FieldElement [value=777, modulus=65521, bitLength=16]", el.toString());
  }

  @Test
  public void testHashCode() {
    FieldElement el = new FieldElement("777", "65521", 16);
    FieldElement same = new FieldElement("777", "65521", 16);
    FieldElement diff = new FieldElement("77", "65521", 16);
    assertEquals(el.hashCode(), same.hashCode());
    assertNotEquals(el.hashCode(), diff.hashCode());
    int hashOne = el.hashCode();
    setField(el, "modulus", null);
    int hashAfter = el.hashCode();
    assertNotEquals(hashOne, hashAfter);
    setField(el, "value", null);
    assertNotEquals(hashAfter, el.hashCode());
  }
  
  @Test
  public void testEquals() {
    FieldElement el = new FieldElement("777", "65521", 16);
    FieldElement same = new FieldElement("777", "65521", 16);
    FieldElement diff = new FieldElement("77", "65521", 16);
    FieldElement diffBitLen = new FieldElement("77", "251", 8);
    assertTrue(el.equals(el));
    assertTrue(el.equals(same));
    assertFalse(el.equals(diff));
    assertFalse(el.equals(diffBitLen));
    assertFalse(el.equals(null));
    assertFalse(el.equals(new Object()));
    setField(el, "modulus", null);
    assertFalse(el.equals(same));
    setField(same, "modulus", null);
    assertTrue(el.equals(same));
    setField(el, "modulus", new BigInteger("11111"));
    assertFalse(el.equals(new FieldElement("777", "65521", 16)));
    setField(el, "value", null);
    assertFalse(el.equals(new FieldElement("777", "65521", 16)));
    setField(same, "modulus", new BigInteger("11111"));
    setField(same, "value", null);
    assertTrue(el.equals(same));
    setField(same, "value", new BigInteger("65521"));
    assertFalse(el.equals(same));
  }
  
  // Negative tests

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckBitLength() {
    new FieldElement(111, modulus, 7);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegative() {
    new FieldElement(-111, modulus, 8);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegativeMod() {
    new FieldElement(111, BigInteger.valueOf(-251), 8);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckBitLengthMismatch() {
    new FieldElement(111, BigInteger.valueOf(1111), 8);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckValueTooLarge() {
    new FieldElement(252, BigInteger.valueOf(251), 8);
  }

}
