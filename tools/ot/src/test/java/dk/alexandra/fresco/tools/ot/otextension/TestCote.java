package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;

public class TestCote {

  /**** POSITIVE TESTS. ****/
  @Test
  public void testGetBit() {
    byte[] byteArray = new byte[] {(byte) 0x54, (byte) 0x04};
    assertEquals(true, CoteSender.getBit(byteArray, 13));
    byteArray = new byte[] {(byte) 0xFF, (byte) 0xFB};
    assertEquals(false, CoteSender.getBit(byteArray, 13));
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalInit() {
    Random rand = new Random();
    // fake network
    Network network = new Network() {
      @Override
      public void send(int partyId, byte[] data) {}

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }
    };
    CoteSender badSender;
    CoteReceiver badReceiver;
    boolean thrown = false;
    try {
      badSender = new CoteSender(0, 128, 80, null, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      badReceiver = new CoteReceiver(0, 128, 80, rand, null);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    thrown = false;
    try {
      badSender = new CoteSender(0, 0, 80, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      badReceiver = new CoteReceiver(0, 128, 0, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      badSender = new CoteSender(0, 127, 80, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Computational security parameter must be divisible by 8", e.getMessage());
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
      CoteShared.xor(arr1, arr2);
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
      CoteShared.xor(list1, list2);
    } catch (IllegalArgumentException e) {
      assertEquals("The vectors are not of equal length", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);

    thrown = false;
    list1.add(new byte[2]);
    try {
      CoteShared.xor(list1, list2);
    } catch (IllegalArgumentException e) {
      assertEquals("The byte arrays are not of equal length", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

}
