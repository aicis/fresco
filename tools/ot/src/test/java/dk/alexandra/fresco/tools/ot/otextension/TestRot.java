package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.Constants;
import dk.alexandra.fresco.tools.ot.base.DummyOt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TestRot {

  private Rot rot;
  private Method multiplyWithoutReduction;

  /**
   * Setup a local Rot instance.
   */
  @Before
  public void setup() throws NoSuchMethodException {
    Drbg rand = new AesCtrDrbg(Constants.seedOne);
    // fake network
    Network network = new Network() {
      @Override
      public void send(int partyId, byte[] data) {
      }

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }
    };
    OtExtensionResourcePool resources = new OtExtensionResourcePoolImpl(1, 2,
        128, 40, rand);
    this.rot = new Rot(resources, network, new DummyOt(2, network));
    multiplyWithoutReduction = RotShared.class.getDeclaredMethod(
        "multiplyWithoutReduction", StrictBitVector.class,
        StrictBitVector.class);
    multiplyWithoutReduction.setAccessible(true);
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testMultiplyByZeroWithoutReduction() throws NoSuchMethodException,
      SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[]{(byte) 0x41, (byte) 0xFF};
    // Semantically equal to 1 as we read bits from left to right
    // 0 0 0 0 0 0 0 0
    byte[] bbyte = new byte[]{(byte) 0x00};
    // 0 0 0 0 0 0 0 0, 0 0 0 0 0 0 0 0
    byte[] expectedByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00};
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(rot
        .getReceiver(), a, b);
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
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(rot
        .getReceiver(), a, b);
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
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(rot
        .getReceiver(), a, b);
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
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 16);
    StrictBitVector expected = new StrictBitVector(expectedByte, 32);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(rot
        .getReceiver(), a, b);
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
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    List<StrictBitVector> firstList = new ArrayList<StrictBitVector>(2);
    List<StrictBitVector> secondList = new ArrayList<StrictBitVector>(2);
    firstList.add(a);
    firstList.add(b);
    secondList.add(b);
    secondList.add(a);
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1, 0 0 0 0 0 0 0 0 XOR
    // 0 0 0 1 0 0 0 0, 0 1 1 1 1 1 1 1, 1 1 0 0 0 0 0 0 =
    // 0 1 0 1 0 0 0 1, 1 0 0 0 0 0 0 0, 1 1 0 0 0 0 0 0
    byte[] expectedByte = new byte[]{(byte) 0x51, (byte) 0x80, (byte) 0xC0};
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    // TODO[TOR] This is not used?
    List<StrictBitVector> expectedList = new ArrayList<>(2);
    expectedList.add(expected);
    expectedList.add(expected);
    StrictBitVector res = (StrictBitVector) multiplyWithoutReduction.invoke(rot
        .getReceiver(), a, b);
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
    StrictBitVector aone = new StrictBitVector(abyteOne, 8);
    StrictBitVector atwo = new StrictBitVector(abyteTwo, 8);
    List<StrictBitVector> alist = new ArrayList<>(
        Arrays.asList(aone, zero, zero, zero, zero, zero, zero, atwo));
    // Semantically equal to 1 as we read bits from left to right
    // 1 0 0 0 0 0 0 1
    byte[] bbyte = new byte[]{(byte) 0x89};
    StrictBitVector b = new StrictBitVector(bbyte, 8);
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
    StrictBitVector expected = new StrictBitVector(expectedByte, 8);
    Method computeBitLinearCombination = RotReceiver.class.getDeclaredMethod(
        "computeBitLinearCombination", StrictBitVector.class,
        List.class);
    computeBitLinearCombination.setAccessible(true);
    StrictBitVector res = (StrictBitVector) computeBitLinearCombination.invoke(
        rot.getReceiver(), b, alist);
    assertEquals(true, expected.equals(res));
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalExtend() {
    boolean thrown = false;
    try {
      rot.getSender().extend(88);
    } catch (IllegalStateException e) {
      assertEquals("Not initialized",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);

    thrown = false;
    try {
      StrictBitVector choices = new StrictBitVector(88);
      rot.getReceiver().extend(choices);
    } catch (IllegalStateException e) {
      assertEquals("Not initialized", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}
