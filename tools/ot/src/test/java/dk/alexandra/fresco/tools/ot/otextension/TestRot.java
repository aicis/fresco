package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class TestRot {
  private Rot rot;

  @Before
  public void setup() {
    Random rand = new Random();
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
    this.rot = new Rot(1, 2, 128, 40, rand, network);
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testMultiplyByZeroWithoutReduction() {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[] { (byte) 0x41, (byte) 0xFF };
    // Semantically equal to 1 as we read bits from left to right
    // 0 0 0 0 0 0 0 0
    byte[] bbyte = new byte[] { (byte) 0x00 };
    // 0 0 0 0 0 0 0 0, 0 0 0 0 0 0 0 0
    byte[] expectedByte = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    StrictBitVector res = RotShared.multiplyWithoutReduction(a, b);
    assertEquals(expected, res);
  }

  @Test
  public void testMultiplyByOneWithoutReduction() {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[] { (byte) 0x41, (byte) 0xFF };
    // Semantically equal to 1 as we read bits from left to right
    // 1 0 0 0 0 0 0 0
    byte[] bbyte = new byte[] { (byte) 0x80 };
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] expectedByte = new byte[] { (byte) 0x41, (byte) 0xFF, (byte) 0x00 };
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    StrictBitVector res = RotShared.multiplyWithoutReduction(a, b);
    assertEquals(expected, res);
  }

  @Test
  public void testMultiplyByFourWithoutReduction() {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[] { (byte) 0x41, (byte) 0xFF };
    // Semantically equal to 1 as we read bits from left to right
    // 0 0 1 0 0 0 0 0
    byte[] bbyte = new byte[] { (byte) 0x20 };
    // 0 0 0 1 0 0 0 0, 0 1 1 1 1 1 1 1, 1 1 0 0 0 0 0 0
    byte[] expectedByte = new byte[] { (byte) 0x10, (byte) 0x7F, (byte) 0xC0 };
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    StrictBitVector res = RotShared.multiplyWithoutReduction(a, b);
    assertEquals(expected, res);
  }

  @Test
  public void testMultiplyWithoutReduction() {
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1
    byte[] abyte = new byte[] { (byte) 0x41, (byte) 0xFF };
    // Semantically equal to 1 as we read bits from left to right
    // 1 0 1 0 0 0 0 0
    byte[] bbyte = new byte[] { (byte) 0xA0 };
    // 0 1 0 0 0 0 0 1, 1 1 1 1 1 1 1 1, 0 0 0 0 0 0 0 0 XOR
    // 0 0 0 1 0 0 0 0, 0 1 1 1 1 1 1 1, 1 1 0 0 0 0 0 0 =
    // 0 1 0 1 0 0 0 1, 1 0 0 0 0 0 0 0, 1 1 0 0 0 0 0 0
    byte[] expectedByte = new byte[] { (byte) 0x51, (byte) 0x80, (byte) 0xC0 };
    StrictBitVector a = new StrictBitVector(abyte, 16);
    StrictBitVector b = new StrictBitVector(bbyte, 8);
    StrictBitVector expected = new StrictBitVector(expectedByte, 24);
    StrictBitVector res = RotShared.multiplyWithoutReduction(a, b);
    assertEquals(expected, res);
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalExtend()
      throws MaliciousOtExtensionException, NoSuchAlgorithmException {
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
