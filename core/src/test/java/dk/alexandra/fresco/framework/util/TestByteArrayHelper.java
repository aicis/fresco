package dk.alexandra.fresco.framework.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestByteArrayHelper {

  /**** POSITIVE TESTS. ****/
  @Test
  public void testGetBit() {
    byte[] byteArray = new byte[] { (byte) 0x54, (byte) 0x04 };
    assertEquals(true, ByteArrayHelper.getBit(byteArray, 13));
    byteArray = new byte[] { (byte) 0xFF, (byte) 0xFB };
    assertEquals(false, ByteArrayHelper.getBit(byteArray, 13));
  }

  @Test
  public void testSetBit() {
    // Set a true bit to false
    byte[] byteArray = new byte[] { (byte) 0x54, (byte) 0x04 };
    ByteArrayHelper.setBit(byteArray, 13, false);
    byte[] expected = new byte[] { (byte) 0x54, (byte) 0x00 };
    for (int i = 0; i < byteArray.length; i++) {
      assertEquals(expected[i], byteArray[i]);
    }
    // Set a false bit to true
    byteArray = new byte[] { (byte) 0x54, (byte) 0x04 };
    ByteArrayHelper.setBit(byteArray, 15, true);
    expected = new byte[] { (byte) 0x54, (byte) 0x05 };
    for (int i = 0; i < byteArray.length; i++) {
      assertEquals(expected[i], byteArray[i]);
    }
    // Set a false bit to false
    byteArray = new byte[] { (byte) 0x54, (byte) 0x04 };
    ByteArrayHelper.setBit(byteArray, 2, false);
    expected = new byte[] { (byte) 0x54, (byte) 0x04 };
    for (int i = 0; i < byteArray.length; i++) {
      assertEquals(expected[i], byteArray[i]);
    }
    // Set a true bit to true
    byteArray = new byte[] { (byte) 0x54, (byte) 0x04 };
    ByteArrayHelper.setBit(byteArray, 3, true);
    expected = new byte[] { (byte) 0x54, (byte) 0x04 };
    for (int i = 0; i < byteArray.length; i++) {
      assertEquals(expected[i], byteArray[i]);
    }
  }

  @Test
  public void testXor() {
    byte[] arr1 = { (byte) 0x00, (byte) 0x02, (byte) 0xFF };
    byte[] arr2 = { (byte) 0xF0, (byte) 0x02, (byte) 0xF0 };
    ByteArrayHelper.xor(arr1, arr2);
    assertEquals((byte) 0xF0, arr1[0]);
    assertEquals((byte) 0x00, arr1[1]);
    assertEquals((byte) 0x0F, arr1[2]);
  }

  @Test
  public void testXorList() {
    byte[] arr1 = { (byte) 0x00, (byte) 0x02, (byte) 0xFF };
    byte[] arr2 = { (byte) 0xF0, (byte) 0x02, (byte) 0xF0 };
    List<byte[]> list1 = new ArrayList<>(2);
    List<byte[]> list2 = new ArrayList<>(2);
    list1.add(arr1);
    list1.add(arr2);
    list2.add(arr2.clone());
    list2.add(arr1.clone());
    ByteArrayHelper.xor(list1, list2);
    assertEquals((byte) 0xF0, list1.get(0)[0]);
    assertEquals((byte) 0x00, list1.get(0)[1]);
    assertEquals((byte) 0x0F, list1.get(0)[2]);
    assertEquals((byte) 0xF0, list1.get(1)[0]);
    assertEquals((byte) 0x00, list1.get(1)[1]);
    assertEquals((byte) 0x0F, list1.get(1)[2]);
  }

  @Test
  public void testShiftArray() {
    byte[] input = new byte[] { (byte) 0x80, (byte) 0x01 };
    byte[] output = new byte[4];
    ByteArrayHelper.shiftArray(input, output, 1);
    byte[] expected = new byte[] { (byte) 0x40, (byte) 0x00, (byte) 0x80,
        (byte) 0x00 };
    for (int i = 0; i < 4; i++) {
      assertEquals(expected[i], output[i]);
    }
  }

  @Test
  public void testShiftArray2() {
    byte[] input = new byte[] { (byte) 0x80, (byte) 0x01 };
    byte[] output = new byte[4];
    ByteArrayHelper.shiftArray(input, output, 8);
    byte[] expected = new byte[] { (byte) 0x00, (byte) 0x80, (byte) 0x01,
        (byte) 0x00 };
    for (int i = 0; i < 4; i++) {
      assertEquals(expected[i], output[i]);
    }
  }

  @Test
  public void testShiftArray3() {
    byte[] input = new byte[] { (byte) 0x80, (byte) 0x01 };
    byte[] output = new byte[4];
    ByteArrayHelper.shiftArray(input, output, 15);
    byte[] expected = new byte[] { (byte) 0x00, (byte) 0x01, (byte) 0x00,
        (byte) 0x02 };
    for (int i = 0; i < 4; i++) {
      assertEquals(expected[i], output[i]);
    }
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalXor() {
    byte[] arr1 = new byte[34];
    byte[] arr2 = new byte[35];
    boolean thrown = false;
    try {
      ByteArrayHelper.xor(arr1, arr2);
    } catch (IllegalArgumentException e) {
      assertEquals("The byte arrays are not of equal length", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testIllegalXorList() {
    List<byte[]> list1 = new ArrayList<>();
    List<byte[]> list2 = new ArrayList<>();
    list1.add(new byte[12]);
    list1.add(new byte[15]);
    list2.add(new byte[12]);
    list2.add(new byte[15]);
    list2.add(new byte[1]);
    boolean thrown = false;
    try {
      ByteArrayHelper.xor(list1, list2);
    } catch (IllegalArgumentException e) {
      assertEquals("The vectors are not of equal length", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);

    thrown = false;
    list1.add(new byte[2]);
    try {
      ByteArrayHelper.xor(list1, list2);
    } catch (IllegalArgumentException e) {
      assertEquals("The byte arrays are not of equal length", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }
}
