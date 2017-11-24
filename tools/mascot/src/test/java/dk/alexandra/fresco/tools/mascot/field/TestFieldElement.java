package dk.alexandra.fresco.tools.mascot.field;

import static org.junit.Assert.*;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.StrictBitVector;

public class TestFieldElement {

  // Positive tests

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
    FieldElement actual = new FieldElement(bv.toByteArray(), "65521", 16);
    assertEquals(el, actual);
  }
  
  // Negative tests

}
