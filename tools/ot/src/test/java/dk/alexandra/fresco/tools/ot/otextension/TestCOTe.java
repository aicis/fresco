package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;

public class TestCOTe {

  private COTeSender dummySender;
  private COTeReceiver dummyReceiver;

  @Before
  public void init() {
    Random rand = new Random();
    Network network = new KryoNetNetwork();
    dummySender = new COTeSender(0, 128, 80, rand, network);
    dummyReceiver = new COTeReceiver(1, 128, 80, rand, network);
  }

  /**** POSITIVE TESTS ****/
  @Test
  public void testMakeMonoVal() {
    byte[] zeroArray = dummyReceiver.makeMonoVal(false, 5);
    for (int i = 0; i < 5; i++) {
      assertEquals(0, zeroArray[i]);
    }
    byte[] oneArray = dummyReceiver.makeMonoVal(true, 5);
    for (int i = 0; i < 5; i++) {
      // Test that each bit in the byte is 1
      for (int j = 0; j < 8; j++) {
        assertEquals(1, (oneArray[i] >> j) & 0x01);
      }
    }
  }

  @Test
  public void testXor() {
    byte[] arr1 = {(byte) 0x00, (byte) 0x02, (byte) 0xFF};
    byte[] arr2 = {(byte) 0xF0, (byte) 0x02, (byte) 0xF0};
    byte[] res = COTeShared.xor(arr1, arr2);
    assertEquals(res[0], (byte) 0xF0);
    assertEquals(res[1], (byte) 0x00);
    assertEquals(res[2], (byte) 0x0F);
  }

  @Test
  public void testXorList() {
    byte[] arr1 = { (byte) 0x00, (byte) 0x02, (byte) 0xFF };
    byte[] arr2 = { (byte) 0xF0, (byte) 0x02, (byte) 0xF0 };
    List<byte[]> list1 = new ArrayList<>(2);
    List<byte[]> list2 = new ArrayList<>(2);
    list1.add(arr1);
    list1.add(arr2);
    list2.add(arr2);
    list2.add(arr1);
    List<byte[]> res = COTeShared.xor(list1, list2);
    assertEquals(res.get(0)[0], (byte) 0xF0);
    assertEquals(res.get(0)[1], (byte) 0x00);
    assertEquals(res.get(0)[2], (byte) 0x0F);
    assertEquals(res.get(1)[0], (byte) 0xF0);
    assertEquals(res.get(1)[1], (byte) 0x00);
    assertEquals(res.get(1)[2], (byte) 0x0F);
  }

  /**** NEGATIVE TESTS ****/
  @Test
  public void testIllegalInit() {
    Random rand = new Random();
    Network network = new KryoNetNetwork();
    boolean thrown = false;
    try {
      COTeSender badSender = new COTeSender(0, 0, 80, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      COTeReceiver badReceiver = new COTeReceiver(0, 128, 0, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      COTeSender badSender = new COTeSender(0, 128, 80, null, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      COTeReceiver badReceiver = new COTeReceiver(0, 128, 80, rand, null);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testIllegalXor() {
    byte[] arr1 = new byte[34];
    byte[] arr2 = new byte[35];
    boolean thrown = false;
    try {
      COTeShared.xor(arr1, arr2);
    } catch (IllegalArgumentException e) {
      assertEquals("The byte arrays are not of equal lengh", e.getMessage());
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
      COTeShared.xor(list1, list2);
    } catch (IllegalArgumentException e) {
      assertEquals("The vectors are not of equal length", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);

    thrown = false;
    list1.add(new byte[2]);
    try {
      COTeShared.xor(list1, list2);
    } catch (IllegalArgumentException e) {
      assertEquals("The byte arrays are not of equal lengh", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }
}
