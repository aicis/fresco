package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import org.junit.Test;

public class TestBitVector {

  @Test
  public void testBitVectorBooleanArray() {
    boolean[] bs = new boolean[] { true, true, true, false, false, true, false, false };
    RegularBitVector vector = new RegularBitVector(bs);
    assertEquals(bs.length, vector.getSize());
    assertEquals(bs[0], vector.getBit(0));
    assertEquals(bs[0], vector.getBit(1));
    assertEquals(bs[2], vector.getBit(2));
    assertEquals(bs[3], vector.getBit(3));
    assertEquals(bs[4], vector.getBit(4));
    assertEquals(bs[5], vector.getBit(5));
    assertEquals(bs[6], vector.getBit(6));
    assertEquals(bs[7], vector.getBit(7));
  }

  @Test
  public void testBitVectorByteArray() {
    byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x04 };
    {
      BitSet bitSet = BitSet.valueOf(bytes);
      RegularBitVector vector = new RegularBitVector(bytes, bytes.length * 8);
      for (int i = 0; i < vector.getSize(); i++) {
        assertEquals(bitSet.get(i), vector.getBit(i));
      }
    }
    {
      BitSet bitSet = BitSet.valueOf(bytes);
      RegularBitVector vector = new RegularBitVector(bytes, 4);
      for (int i = 0; i < vector.getSize(); i++) {
        assertEquals(bitSet.get(i), vector.getBit(i));
      }
    }
    {
      boolean exception = false;
      try {
        new RegularBitVector(bytes, -4);
      } catch (IllegalArgumentException e) {
        exception = true;
      }
      assertTrue(exception);
    }
  }

  @Test
  public void testBitVectorInt() {
    RegularBitVector vector = new RegularBitVector(10);
    assertEquals(10, vector.getSize());
    for (int i = 0; i < vector.getSize(); i++) {
      assertFalse(vector.getBit(i));
    }
    boolean exception = false;
    try {
      new RegularBitVector(-1);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
  }

  @Test
  public void testGetSize() {
    RegularBitVector vector = new RegularBitVector(10);
    assertEquals(10, vector.getSize());
  }

  @Test
  public void testGetAndSet() {
    RegularBitVector vector = new RegularBitVector(10);
    assertEquals(10, vector.getSize());
    assertFalse(vector.getBit(2));
    vector.setBit(2, true);
    assertTrue(vector.getBit(2));
    boolean exception;
    exception = false;
    try {
      vector.setBit(-1, true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      vector.setBit(vector.getSize(), true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      vector.getBit(-1);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      vector.getBit(vector.getSize());
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
  }

  @Test
  public void testAsByteArr() {
    {
      byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x04 };
      RegularBitVector vector = new RegularBitVector(bytes, bytes.length * 8);
      byte[] vectorBytes = vector.toByteArray();
      assertArrayEquals(bytes, vectorBytes);
    }
    {
      // Test with last byte being zeroes
      byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x00 };
      RegularBitVector vector = new RegularBitVector(bytes, bytes.length * 8);
      byte[] vectorBytes = vector.toByteArray();
      // TODO: Find out if this is the intended behavior?
      assertNotEquals(vector.getSize(), vectorBytes.length * 8);
    }
  }

  @Test
  public void testXor() {
    byte[] bytes1 = new byte[] { 0x01, 0x02, 0x03, 0x04 };
    byte[] bytes2 = new byte[] { 0x01, 0x01, 0x01, 0x01 };
    RegularBitVector vector1 = new RegularBitVector(bytes1, bytes1.length * 8);
    RegularBitVector vector2 = new RegularBitVector(bytes2, bytes2.length * 8);
    vector1.xor(vector2);
    BitSet bitSet1 = BitSet.valueOf(bytes1);
    BitSet bitSet2 = BitSet.valueOf(bytes2);
    bitSet1.xor(bitSet2);
    for (int i = 0; i < vector1.getSize(); i++) {
      assertEquals(bitSet1.get(i), vector1.getBit(i));
    }
    byte[] bytes3 = new byte[] { 0x01, 0x01, 0x01, 0x01, 0x03 };
    RegularBitVector vector3 = new RegularBitVector(bytes3, bytes3.length);
    boolean exception = false;
    try {
      vector1.xor(vector3);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
  }

}
