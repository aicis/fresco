package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.helper.HelperForTests;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;

public class TestCote {
  private final int kbitSecurity = 128;
  private CoteFactory cote;
  private Drbg rand;
  private Network network;

  /**
   * Setup a correlated OT functionality.
   */
  @Before
  public void setup() throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {
    rand = new AesCtrDrbg(HelperForTests.seedOne);
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
    RotList seedOts = new RotList(rand, kbitSecurity);
    Field sent = RotList.class.getDeclaredField("sent");
    sent.setAccessible(true);
    sent.set(seedOts, true);
    Field received = RotList.class.getDeclaredField("received");
    received.setAccessible(true);
    received.set(seedOts, true);
    CoinTossing ct = new CoinTossing(1, 2, rand);
    OtExtensionResourcePool resources = new OtExtensionResourcePoolImpl(1, 2,
        128, 40, 1, rand, ct, seedOts);
    this.cote = new CoteFactory(resources, network);
  }

  /**** NEGATIVE TESTS. ****/
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
