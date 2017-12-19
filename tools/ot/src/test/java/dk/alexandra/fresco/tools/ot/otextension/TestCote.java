package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.Constants;
import dk.alexandra.fresco.tools.ot.base.DummyOt;

import org.junit.Before;
import org.junit.Test;

public class TestCote {
  private Cote cote;
  private Drbg rand;
  private Network network;

  /**
   * Setup a correlated OT functionality.
   */
  @Before
  public void setup() {
    rand = new AesCtrDrbg(Constants.seedOne);
    // fake network
    network = new Network() {
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
    this.cote = new Cote(resources, network, new DummyOt(2, network));
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testNotInitialized() {
    boolean thrown = false;
    try {
      cote.getSender().extend(128);
    } catch (IllegalStateException e) {
      assertEquals("Not initialized", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      byte[] randomness = new byte[128 / 8];
      cote.getReceiver().extend(new StrictBitVector(randomness, 128));
    } catch (IllegalStateException e) {
      assertEquals("Not initialized", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testIllegalExtendSender() {
    boolean thrown = false;
    try {
      cote.getSender().extend(127);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer divisize by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      cote.getSender().extend(-1);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      cote.getSender().extend(0);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testIllegalExtendReceiver() {
    boolean thrown = false;
    try {
      StrictBitVector choices = new StrictBitVector(0);
      cote.getReceiver().extend(choices);
    } catch (IllegalArgumentException e) {
      assertEquals("The amount of OTs must be a positive integer",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}
