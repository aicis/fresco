package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import org.junit.Test;

public class TestStrictBitVector {

  // Positive tests

  @Test
  public void testConstructCorrectSize() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits, bits.length * 8);
    assertArrayEquals(bits, bv.toByteArray());
  }

  @Test
  public void testConstructRandomCorrectSize() {
    Drbg rand = new PaddingAesCtrDrbg(new byte[] { 0x42 }, 32 * 8);
    StrictBitVector bv = new StrictBitVector(4 * 8, rand);
    assertEquals(4 * 8, bv.getSize());
    assertEquals(4, bv.toByteArray().length);
    // Sanity check
    byte[] zeroArray = new byte[4 * 8];
    assertFalse(Arrays.equals(zeroArray, bv.toByteArray()));
  }

  @Test
  public void testConcatSameSizeBitVectors() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne, bitsOne.length * 8);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02, (byte) 0x03 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo, bitsTwo.length * 8);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo);

    byte[] expectedBits =
        { (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03 };
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatReverse() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne, bitsOne.length * 8);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02, (byte) 0x03 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo, bitsTwo.length * 8);

    StrictBitVector actual = StrictBitVector.concat(true, bvOne, bvTwo);

    byte[] expectedBits =
        { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatDifferentSizeBitVectors() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne, bitsOne.length * 8);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo, bitsTwo.length * 8);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo);

    byte[] expectedBits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02 };
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatMultipleBitVectors() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne, bitsOne.length * 8);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo, bitsTwo.length * 8);

    byte[] bitsThree = { (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06 };
    StrictBitVector bvThree = new StrictBitVector(bitsThree, bitsThree.length * 8);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo, bvThree);

    byte[] expectedBits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02,
        (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06 };
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatSingleBitVector() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne, bitsOne.length * 8);

    StrictBitVector actual = StrictBitVector.concat(bvOne);

    assertEquals(bvOne, actual);
  }

  @Test
  public void testConcatEmptyBitVector() {
    byte[] bitsOne = {};
    StrictBitVector bvOne = new StrictBitVector(bitsOne, bitsOne.length * 8);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo, bitsTwo.length * 8);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo);

    byte[] expectedBits = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector expected = new StrictBitVector(expectedBits, expectedBits.length * 8);
    assertEquals(expected, actual);
  }

  @Test
  public void testGetBitBigEndian() {
    int bitLen = 72;
    byte[] bits = new byte[bitLen / 8];
    for (int b = 0; b < bits.length; b++) {
      bits[b] = (byte) b;
    }
    StrictBitVector bv = new StrictBitVector(bits, bitLen);
    // string for readability
    String actual = "";
    for (int b = 0; b < bitLen; b++) {
      actual += bv.getBit(b) ? "1" : "0";
    }
    String expected = "000100001110000001100000101000000010000011000000010000001000000000000000";
    assertEquals(expected, actual);
  }

  @Test
  public void testXor() {
    byte[] bitsOne = { (byte) 0x00, (byte) 0x01, (byte) 0x02 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne, 24);
    byte[] bitsTwo = { (byte) 0x03, (byte) 0x04, (byte) 0x05 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo, 24);
    bvOne.xor(bvTwo);
    byte[] expectedBytes = { (byte) 0x03, (byte) 0x05, (byte) 0x07 };
    StrictBitVector expected = new StrictBitVector(expectedBytes, 24);
    assertEquals(expected, bvOne);
  }

  @Test
  public void testGetBitLittleEndian() {
    int bitLen = 72;
    byte[] bits = new byte[bitLen / 8];
    for (int b = 0; b < bits.length; b++) {
      bits[b] = (byte) b;
    }
    StrictBitVector bv = new StrictBitVector(bits, bitLen);
    // string for readability
    String actual = "";
    for (int b = 0; b < bitLen; b++) {
      actual += bv.getBit(b, false) ? "1" : "0";
    }
    String expected = "000000000000000100000010000000110000010000000101000001100000011100001000";
    assertEquals(expected, actual);
  }

  @Test
  public void testSetBitBigEndian() {
    int bitLen = 16;
    byte[] bits = new byte[bitLen / 8];
    StrictBitVector bv = new StrictBitVector(bits, bitLen);
    bv.setBit(2, true);
    bv.setBit(11, true);
    byte[] actual = bv.toByteArray();
    // 15 14 13 12 11 ... 2 1 0
    // 0 0 0 0 1 0 0 0 0 0 0 0 0 1 0 0
    byte[] expected = new byte[] { (byte) 0x08, (byte) 0x04 };
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testSetBitLittleEndian() {
    int bitLen = 16;
    byte[] bits = new byte[bitLen / 8];
    StrictBitVector bv = new StrictBitVector(bits, bitLen);
    bv.setBit(2, true, false);
    bv.setBit(11, true, false);
    byte[] actual = bv.toByteArray();
    // 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0
    // 0 0 1 0 0 0 0 0 0 0 0 1 0 0 0 0
    byte[] expected = new byte[] { (byte) 0x20, (byte) 0x10 };
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testAsBinaryString() {
    int bitLen = 72;
    byte[] bits = new byte[bitLen / 8];
    for (int b = 0; b < bits.length; b++) {
      bits[b] = (byte) b;
    }
    StrictBitVector bv = new StrictBitVector(bits, bitLen);
    String expected = "000100001110000001100000101000000010000011000000010000001000000000000000";
    assertEquals(expected, bv.asBinaryString());
  }

  @Test
  public void testToString() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits, bits.length * 8);
    String expected = "StrictBitVector [bits=[-1, 1, 0]]";
    assertEquals(expected, bv.toString());
  }

  // Negative tests

  @Test(expected = IndexOutOfBoundsException.class)
  public void testRangeCheck() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits, bits.length * 8);
    bv.getBit(2000);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testRangeCheckNegative() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits, bits.length * 8);
    bv.getBit(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructIncorrectSize() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    new StrictBitVector(bits, 11);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructInconsistentSize() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    new StrictBitVector(bits, 4 * 8);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructRandomIncorrectSize() {
    new StrictBitVector(10, new Drbg() {
      @Override
      public void nextBytes(byte[] bytes) {}
    });
  }

}
