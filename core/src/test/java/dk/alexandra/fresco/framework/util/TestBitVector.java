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
    BitVector vector = new BitVector(bs);
    assertEquals(bs.length, vector.getSize());
    assertEquals(bs[0], vector.get(0));
    assertEquals(bs[0], vector.get(1));
    assertEquals(bs[2], vector.get(2));
    assertEquals(bs[3], vector.get(3));
    assertEquals(bs[4], vector.get(4));
    assertEquals(bs[5], vector.get(5));
    assertEquals(bs[6], vector.get(6));
    assertEquals(bs[7], vector.get(7));
  }

  @Test
  public void testBitVectorByteArray() {
    byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x04 };
    {
      BitSet bitSet = BitSet.valueOf(bytes);
      BitVector vector = new BitVector(bytes, bytes.length * 8);
      for (int i = 0; i < vector.getSize(); i++) {
        assertEquals(bitSet.get(i), vector.get(i));
      }
    }
    {
      BitSet bitSet = BitSet.valueOf(bytes);
      BitVector vector = new BitVector(bytes, 4);
      for (int i = 0; i < vector.getSize(); i++) {
        assertEquals(bitSet.get(i), vector.get(i));
      }
    }
    {
      boolean exception = false;
      try {
        new BitVector(bytes, -4);
      } catch (IllegalArgumentException e) {
        exception = true;
      }
      assertTrue(exception);
    }
  }

  @Test
  public void testBitVectorInt() {
    BitVector vector = new BitVector(10);
    assertEquals(10, vector.getSize());
    for (int i = 0; i < vector.getSize(); i++) {
      assertFalse(vector.get(i));
    }
    boolean exception = false;
    try {
      new BitVector(-1);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
  }

  @Test
  public void testGetSize() {
    BitVector vector = new BitVector(10);
    assertEquals(10, vector.getSize());
  }

  @Test
  public void testGetAndSet() {
    BitVector vector = new BitVector(10);
    assertEquals(10, vector.getSize());
    assertFalse(vector.get(2));
    vector.set(2, true);
    assertTrue(vector.get(2));
    boolean exception;
    exception = false;
    try {
      vector.set(-1, true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      vector.set(vector.getSize(), true);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      vector.get(-1);
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
    exception = false;
    try {
      vector.get(vector.getSize());
    } catch (IndexOutOfBoundsException e) {
      exception = true;
    }
    assertTrue(exception);
  }

  @Test
  public void testAsByteArr() {
    {
      byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x04 };
      BitVector vector = new BitVector(bytes, bytes.length * 8);
      byte[] vectorBytes = vector.asByteArr();
      assertArrayEquals(bytes, vectorBytes);
    }
    {
      // Test with last byte being zeroes
      byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x00 };
      BitVector vector = new BitVector(bytes, bytes.length * 8);
      byte[] vectorBytes = vector.asByteArr();
      // TODO: Find out if this is the intended behavior?
      assertNotEquals(vector.getSize(), vectorBytes.length * 8);
    }
  }

  @Test
  public void testAsBooleans() {
    boolean[] bs1 = new boolean[] { true, true, true, false, false, true, false, false };
    BitVector vector = new BitVector(bs1);
    boolean[] bs2 = vector.asBooleans();
    assertArrayEquals(bs1, bs2);
  }

  @Test
  public void testXor() {
    byte[] bytes1 = new byte[] { 0x01, 0x02, 0x03, 0x04 };
    byte[] bytes2 = new byte[] { 0x01, 0x01, 0x01, 0x01 };
    BitVector vector1 = new BitVector(bytes1, bytes1.length * 8);
    BitVector vector2 = new BitVector(bytes2, bytes2.length * 8);
    vector1.xor(vector2);
    BitSet bitSet1 = BitSet.valueOf(bytes1);
    BitSet bitSet2 = BitSet.valueOf(bytes2);
    bitSet1.xor(bitSet2);
    for (int i = 0; i < vector1.getSize(); i++) {
      assertEquals(bitSet1.get(i), vector1.get(i));
    }
    byte[] bytes3 = new byte[] { 0x01, 0x01, 0x01, 0x01, 0x03 };
    BitVector vector3 = new BitVector(bytes3, bytes3.length);
    boolean exception = false;
    try {
      vector1.xor(vector3);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
  }

}
