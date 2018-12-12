package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.FieldElementBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import java.math.BigInteger;
import org.junit.Test;

public class TestMascotFieldElement {

  private final BigInteger modulus = new BigInteger("251");
  private final FieldDefinitionBigInteger definition = new FieldDefinitionBigInteger(
      new ModulusBigInteger(modulus));
  private final int bitLength = 8;

  // Positive tests

  @Test
  public void testAdd() {
    FieldElement elOne = definition.createElement(22);
    FieldElement elTwo = definition.createElement(11);
    FieldElement expected = definition.createElement(33);
    CustomAsserts.assertEquals(expected, elOne.add(elTwo));
  }

  @Test
  public void testPow() {
    FieldElement elOne = definition.createElement(22);
    FieldElement expected = definition.createElement(233);
    CustomAsserts.assertEquals(expected, elOne.pow(2));
  }

  @Test
  public void testSubtract() {
    FieldElement elOne = definition.createElement(22);
    FieldElement elTwo = definition.createElement(11);
    FieldElement expected = definition.createElement(11);
    CustomAsserts.assertEquals(expected, elOne.subtract(elTwo));
  }

  @Test
  public void testMultiply() {
    FieldElement elOne = definition.createElement(22);
    FieldElement elTwo = definition.createElement(11);
    FieldElement expected = definition.createElement(242);
    CustomAsserts.assertEquals(expected, elOne.multiply(elTwo));
  }

  @Test
  public void testNegate() {
    FieldElement elOne = definition.createElement(22);
    FieldElement expected = definition.createElement(229);
    CustomAsserts.assertEquals(expected, elOne.negate());
  }

  @Test
  public void testIsZero() {
    FieldElement zero = definition.createElement(0);
    FieldElement notZero = definition.createElement(1);
    assertTrue(zero.isZero());
    assertFalse(notZero.isZero());
  }

  @Test
  public void testGetBit() {
    assertEquals(false, definition.createElement(22).getBit(0));
    assertEquals(true, definition.createElement(22).getBit(1));
  }

  @Test
  public void testConvertToBitVectorSingleByte() {
    FieldElement el = new FieldElementBigInteger("11", new ModulusBigInteger("251"));
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesSmall() {
    FieldElement el = new FieldElementBigInteger("11", new ModulusBigInteger("65521"));
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0x0, (byte) 0xB};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorMultipleBytesLarge() {
    FieldElement el = new FieldElementBigInteger("65520", new ModulusBigInteger("65521"));
    StrictBitVector actual = el.toBitVector();
    byte[] expectedBits = {(byte) 0xFF, (byte) 0xF0};
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConvertToBitVectorAndBack() {
    FieldElement el = new FieldElementBigInteger("777", new ModulusBigInteger("65521"));
    StrictBitVector bv = el.toBitVector();
    FieldElement actual = new FieldElementBigInteger(bv.toByteArray(),
        new ModulusBigInteger("65521"));
    CustomAsserts.assertEquals(el, actual);
  }

  @Test
  public void testToString() {
    FieldElement el = new FieldElementBigInteger("777", new ModulusBigInteger("65521"));
    assertEquals("FieldElement [value=777, modulus=65521, bitLength=16]", el.toString());
  }

  @Test
  public void testModInverse() {
    BigInteger raw = new BigInteger("121");
    FieldElement el = definition.createElement(raw);
    FieldElement actual = el.modInverse();
    FieldElement expected = definition.createElement(raw.modInverse(modulus));
    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testSqrt() {
    FieldElement el = definition.createElement(123);
    FieldElement expected = definition.createElement(25);
    FieldElement actual = el.sqrt();
    CustomAsserts.assertEquals(expected, actual);
  }

  // Negative tests

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegative() {
    definition.createElement(-111);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckNegativeMod() {
    new FieldElementBigInteger(111, new ModulusBigInteger("-251"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckBitLengthMismatch() {
    new FieldElementBigInteger(111, new ModulusBigInteger("1111"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSanityCheckValueTooLarge() {
    new FieldElementBigInteger(252, new ModulusBigInteger("251"));
  }
}
