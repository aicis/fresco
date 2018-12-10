package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;
import org.junit.Test;

public class TestMascotFieldElement {

  private final ModulusBigInteger modulus = new ModulusBigInteger("251");
  private final int bitLength = 8;

  // Positive tests

  @Test
  public void testFieldElementConstructors() {
    MascotFieldElement elOne = new MascotFieldElement(new BigInteger("11"), modulus);
    MascotFieldElement elTwo = new MascotFieldElement(11, modulus);
    MascotFieldElement elThree = new MascotFieldElement(elTwo);
    MascotFieldElement elFour = new MascotFieldElement("11", "251");
    CustomAsserts.assertEquals(elOne, elTwo);
    CustomAsserts.assertEquals(elOne, elThree);
    CustomAsserts.assertEquals(elOne, elFour);
  }

  @Test
  public void testAdd() {
    MascotFieldElement elOne = new MascotFieldElement(22, modulus);
    MascotFieldElement elTwo = new MascotFieldElement(11, modulus);
    MascotFieldElement expected = new MascotFieldElement(33, modulus);
    CustomAsserts.assertEquals(expected, elOne.add(elTwo));
  }

  @Test
  public void testPow() {
    MascotFieldElement elOne = new MascotFieldElement(22, modulus);
    MascotFieldElement expected = new MascotFieldElement(233, modulus);
    CustomAsserts.assertEquals(expected, elOne.pow(2));
  }

  @Test
  public void testSubtract() {
    MascotFieldElement elOne = new MascotFieldElement(22, modulus);
    MascotFieldElement elTwo = new MascotFieldElement(11, modulus);
    MascotFieldElement expected = new MascotFieldElement(11, modulus);
    CustomAsserts.assertEquals(expected, elOne.subtract(elTwo));
  }

  @Test
  public void testMultiply() {
    MascotFieldElement elOne = new MascotFieldElement(22, modulus);
    MascotFieldElement elTwo = new MascotFieldElement(11, modulus);
    MascotFieldElement expected = new MascotFieldElement(242, modulus);
    CustomAsserts.assertEquals(expected, elOne.multiply(elTwo));
  }

  @Test
  public void testNegate() {
    MascotFieldElement elOne = new MascotFieldElement(22, modulus);
    MascotFieldElement expected = new MascotFieldElement(229, modulus);
    CustomAsserts.assertEquals(expected, elOne.negate());
  }

  @Test
  public void testIsZero() {
    MascotFieldElement zero = new MascotFieldElement(0, modulus);
    MascotFieldElement notZero = new MascotFieldElement(1, modulus);
    assertTrue(zero.isZero());
    assertFalse(notZero.isZero());
  }

  @Test
  public void testToBigInteger() {
    assertEquals(new BigInteger("22"), new MascotFieldElement(22, modulus).toBigInteger());
  }

  @Test
  public void testGetBit() {
    assertEquals(false, new MascotFieldElement(22, modulus).getBit(0));
    assertEquals(true, new MascotFieldElement(22, modulus).getBit(1));
  }

  @Test
  public void testSelect() {
    MascotFieldElement el = new MascotFieldElement(22, modulus);
    CustomAsserts.assertEquals(el, el.select(true));
    CustomAsserts.assertEquals(new MascotFieldElement(BigInteger.ZERO, modulus),
        el.select(false));
  }

  @Test
  public void testConvertToBitVectorSingleByte() {
    MascotFieldElement el = new MascotFieldElement("11", "251");
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesSmall() {
    MascotFieldElement el = new MascotFieldElement("11", "65521");
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0x0, (byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesLarge() {
    MascotFieldElement el = new MascotFieldElement("65520", "65521");
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xFF, (byte) 0xF0};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorAndBack() {
    MascotFieldElement el = new MascotFieldElement("777", "65521");
    StrictBitVector bv = el.toBitVector();
    MascotFieldElement actual = new MascotFieldElement(bv.toByteArray(),
        new ModulusBigInteger("65521"));
    CustomAsserts.assertEquals(el, actual);
  }

  @Test
  public void testGetters() {
    MascotFieldElement el = new MascotFieldElement("777", "65521");
    assertEquals(new ModulusBigInteger("65521"), el.getModulus());
    assertEquals(16, el.getBitLength());
  }

  @Test
  public void testToString() {
    MascotFieldElement el = new MascotFieldElement("777", "65521");
    assertEquals("MascotFieldElement [value=777, modulus=65521, bitLength=16]", el.toString());
  }

  @Test
  public void testModInverse() {
    BigInteger raw = new BigInteger("121");
    MascotFieldElement el = new MascotFieldElement(raw, modulus);
    MascotFieldElement actual = el.modInverse();
    MascotFieldElement expected = new MascotFieldElement(raw.modInverse(modulus.getBigInteger()), modulus);
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testSqrt() {
    MascotFieldElement el = new MascotFieldElement(123, modulus);
    MascotFieldElement expected = new MascotFieldElement(25, modulus);
    MascotFieldElement actual = el.sqrt();
    CustomAsserts.assertEquals(expected, actual);
  }

  // Negative tests

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegative() {
    new MascotFieldElement(-111, modulus);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegativeMod() {
    new MascotFieldElement(111, new ModulusBigInteger(-251));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckBitLengthMismatch() {
    new MascotFieldElement(111, new ModulusBigInteger(1111));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckValueTooLarge() {
    new MascotFieldElement(252, new ModulusBigInteger(251));
  }

}
