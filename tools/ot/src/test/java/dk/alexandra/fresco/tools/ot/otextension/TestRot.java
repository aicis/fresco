package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TestRot {
  private Method multiplyWithoutReduction;

  /**
   * Setup a local Rot instance.
   */
  @Before
  public void setup() throws NoSuchMethodException,
      SecurityException, IllegalArgumentException {
    multiplyWithoutReduction = RotSharedImpl.class.getDeclaredMethod(
        "multiplyWithoutReduction", StrictBitVector.class,
        StrictBitVector.class);
    multiplyWithoutReduction.setAccessible(true);
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testMultiplyByZeroWithoutReduction() throws
      SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[]{(byte) 0x41, (byte) 0xFF};
    // Semantically equal to 1 as we read bits from left to right
    // 0 0 0 0 0 0 0 0
    byte[] bbyte = new byte[]{(byte) 0x00};
    // 0 0 0 0 0 0 0 0, 0 0 0 0 0 0 0 0
    byte[] expectedByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00};
    StrictBitVector a = new StrictBitVector(abyte);
    StrictBitVector b = new StrictBitVector(bbyte);
    StrictBitVector expected = new StrictBitVector(expectedByte);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(
        RotReceiverImpl.class, a, b);
    assertEquals(expected, res);
  }

  @Test
  public void testMultiplyByOneWithoutReduction() throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[]{(byte) 0x41, (byte) 0xFF};
    // Semantically equal to 1 as we read bits from left to right
    // 1 0 0 0 0 0 0 0
    byte[] bbyte = new byte[]{(byte) 0x80};
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] expectedByte = new byte[]{(byte) 0x41, (byte) 0xFF, (byte) 0x00};
    StrictBitVector a = new StrictBitVector(abyte);
    StrictBitVector b = new StrictBitVector(bbyte);
    StrictBitVector expected = new StrictBitVector(expectedByte);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(
        RotReceiverImpl.class, a, b);
    assertEquals(expected, res);
  }

  @Test
  public void testMultiplyByFourWithoutReduction()
      throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[]{(byte) 0x41, (byte) 0xFF};
    // Semantically equal to 1 as we read bits from left to right
    // 0 0 1 0 0 0 0 0
    byte[] bbyte = new byte[]{(byte) 0x20};
    // 0 0 0 1 0 0 0 0, 0 1 1 1 1 1 1 1, 1 1 0 0 0 0 0 0
    byte[] expectedByte = new byte[]{(byte) 0x10, (byte) 0x7F, (byte) 0xC0};
    StrictBitVector a = new StrictBitVector(abyte);
    StrictBitVector b = new StrictBitVector(bbyte);
    StrictBitVector expected = new StrictBitVector(expectedByte);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(
        RotReceiverImpl.class, a, b);
    assertEquals(expected, res);
  }

  @Test
  public void testMultiplyWithoutReduction() throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[]{(byte) 0x41, (byte) 0xFF};
    // Semantically equal to 1 as we read bits from left to right
    // 1 0 1 0 0 0 0 0, 1 0 0 0 0 0 0 0
    byte[] bbyte = new byte[]{(byte) 0xA0, (byte) 0x80};
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1, 0 0 0 0 0 0 0 0, 0 0 0 0 0 0 0 0 XOR
    // 0 0 0 1 0 0 0 0, 0 1 1 1 1 1 1 1, 1 1 0 0 0 0 0 0, 0 0 0 0 0 0 0 0
    // 0 0 0 0 0 0 0 0, 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1, 0 0 0 0 0 0 0 0 =
    // 0 1 0 1 0 0 0 1, 1 1 0 0 0 0 0 1, 0 0 1 1 1 1 1 1, 0 0 0 0 0 0 0 0
    byte[] expectedByte = new byte[]{(byte) 0x51, (byte) 0xC1, (byte) 0x3F,
        (byte) 0x00};
    StrictBitVector a = new StrictBitVector(abyte);
    StrictBitVector b = new StrictBitVector(bbyte);
    StrictBitVector expected = new StrictBitVector(expectedByte);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(
        RotReceiverImpl.class, a, b);
    assertEquals(expected, res);
  }

  @Test
  public void testComputePolyLinearCombination() throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[]{(byte) 0x41, (byte) 0xFF};
    // Semantically equal to 1 as we read bits from left to right
    // 1 0 1 0 0 0 0 0
    byte[] bbyte = new byte[]{(byte) 0xA0};
    StrictBitVector a = new StrictBitVector(abyte);
    StrictBitVector b = new StrictBitVector(bbyte);
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1, 0 0 0 0 0 0 0 0 XOR
    // 0 0 0 1 0 0 0 0, 0 1 1 1 1 1 1 1, 1 1 0 0 0 0 0 0 =
    // 0 1 0 1 0 0 0 1, 1 0 0 0 0 0 0 0, 1 1 0 0 0 0 0 0
    byte[] expectedByte = new byte[]{(byte) 0x51, (byte) 0x80, (byte) 0xC0};
    StrictBitVector expected = new StrictBitVector(expectedByte);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(
        RotReceiverImpl.class, a, b);
    assertEquals(true, expected.equals(res));
  }

  @Test
  public void testComputeBitLinearCombination() throws NoSuchMethodException,
      SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    // 0 1 0 0 0 0 0 1
    byte[] abyteOne = new byte[]{(byte) 0x41};
    // 0 1 1 1 1 1 1 1
    byte[] abyteTwo = new byte[]{(byte) 0x7F};
    StrictBitVector zero = new StrictBitVector(8);
    StrictBitVector aone = new StrictBitVector(abyteOne);
    StrictBitVector atwo = new StrictBitVector(abyteTwo);
    List<StrictBitVector> alist = new ArrayList<>(
        Arrays.asList(aone, zero, zero, zero, zero, zero, zero, atwo));
    // Semantically equal to 1 as we read bits from left to right
    // 1 0 0 0 0 0 0 1
    byte[] bbyte = new byte[]{(byte) 0x89};
    StrictBitVector b = new StrictBitVector(bbyte);
    // 1 * (0 1 0 0 0 0 0 1) XOR
    // 0 * (0 0 0 0 0 0 0 0) XOR
    // 0 * (0 0 0 0 0 0 0 0) XOR
    // 0 * (0 0 0 0 0 0 0 0) XOR
    // 1 * (0 0 0 0 0 0 0 0) XOR
    // 0 * (0 0 0 0 0 0 0 0) XOR
    // 0 * (0 0 0 0 0 0 0 0) XOR
    // 1 * (0 1 1 1 1 1 1 1 =
    //      0 0 1 1 1 1 1 0
    byte[] expectedByte = new byte[]{(byte) 0x3E};
    StrictBitVector expected = new StrictBitVector(expectedByte);
    Method computeBitLinearCombination = RotReceiverImpl.class.getDeclaredMethod(
        "computeBitLinearCombination", StrictBitVector.class,
        List.class);
    computeBitLinearCombination.setAccessible(true);
    StrictBitVector res = (StrictBitVector) computeBitLinearCombination.invoke(
        RotReceiverImpl.class, b, alist);
    assertEquals(true, expected.equals(res));
  }
}