package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class TestStrictBitVector {

  // Positive tests

  @Test
  public void testConstructCorrectSize() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits);
    assertArrayEquals(bits, bv.toByteArray());
  }

  @Test
  public void testConstructRandomCorrectSize() {
    Drbg rand = new PaddingAesCtrDrbg(new byte[] { 0x42 });
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
    StrictBitVector bvOne = new StrictBitVector(bitsOne);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02, (byte) 0x03 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo);

    byte[] expectedBits =
        { (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03 };
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatReverse() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02, (byte) 0x03 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo);

    StrictBitVector actual = StrictBitVector.concat(true, bvOne, bvTwo);

    byte[] expectedBits =
        { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatDifferentSizeBitVectors() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo);

    byte[] expectedBits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02 };
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatMultipleBitVectors() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo);

    byte[] bitsThree = { (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06 };
    StrictBitVector bvThree = new StrictBitVector(bitsThree);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo, bvThree);

    byte[] expectedBits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02,
        (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06 };
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testConcatSingleBitVector() {
    byte[] bitsOne = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bvOne = new StrictBitVector(bitsOne);

    StrictBitVector actual = StrictBitVector.concat(bvOne);

    assertEquals(bvOne, actual);
  }

  @Test
  public void testConcatEmptyBitVector() {
    byte[] bitsOne = {};
    StrictBitVector bvOne = new StrictBitVector(bitsOne);

    byte[] bitsTwo = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo);

    StrictBitVector actual = StrictBitVector.concat(bvOne, bvTwo);

    byte[] expectedBits = { (byte) 0x01, (byte) 0x02 };
    StrictBitVector expected = new StrictBitVector(expectedBits);
    assertEquals(expected, actual);
  }

  @Test
  public void testGetBitBigEndian() {
    int bitLen = 72;
    byte[] bits = new byte[bitLen / 8];
    for (int b = 0; b < bits.length; b++) {
      bits[b] = (byte) b;
    }
    StrictBitVector bv = new StrictBitVector(bits);
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
    StrictBitVector bvOne = new StrictBitVector(bitsOne);
    byte[] bitsTwo = { (byte) 0x03, (byte) 0x04, (byte) 0x05 };
    StrictBitVector bvTwo = new StrictBitVector(bitsTwo);
    bvOne.xor(bvTwo);
    byte[] expectedBytes = { (byte) 0x03, (byte) 0x05, (byte) 0x07 };
    StrictBitVector expected = new StrictBitVector(expectedBytes);
    assertEquals(expected, bvOne);
  }

  @Test
  public void testGetBitLittleEndian() {
    int bitLen = 72;
    byte[] bits = new byte[bitLen / 8];
    for (int b = 0; b < bits.length; b++) {
      bits[b] = (byte) b;
    }
    StrictBitVector bv = new StrictBitVector(bits);
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
    StrictBitVector bv = new StrictBitVector(bits);
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
    StrictBitVector bv = new StrictBitVector(bits);
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
    StrictBitVector bv = new StrictBitVector(bits);
    String expected = "000100001110000001100000101000000010000011000000010000001000000000000000";
    assertEquals(expected, bv.asBinaryString());
  }

  @Test
  public void testToString() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits);
    String expected = "StrictBitVector [bits=[-1, 1, 0]]";
    assertEquals(expected, bv.toString());
  }

  @Test
  public void testEquals() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits);
    byte[] otherBits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector otherBv = new StrictBitVector(otherBits);
    assertTrue(bv.equals(bv));
    assertTrue(bv.equals(otherBv));
    assertFalse(bv.equals(null));
    assertFalse(bv.equals("Not a bit vector"));
    assertFalse(bv.equals(new StrictBitVector(new byte[]{})));
  }

  @Test
  public void testHashCode() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits);
    byte[] otherBits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector otherBv = new StrictBitVector(otherBits);
    assertEquals(bv.hashCode(), otherBv.hashCode());
    assertNotEquals(bv.hashCode(), new StrictBitVector(new byte[]{0x42}));
  }

  // Negative tests

  @Test(expected = IndexOutOfBoundsException.class)
  public void testRangeCheck() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits);
    bv.getBit(2000);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testRangeCheckNegative() {
    byte[] bits = { (byte) 0xFF, (byte) 0x01, (byte) 0x00 };
    StrictBitVector bv = new StrictBitVector(bits);
    bv.getBit(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructRandomIncorrectSize() {
    new StrictBitVector(10, new Drbg() {
      @Override
      public void nextBytes(byte[] bytes) {}
    });
  }

}
