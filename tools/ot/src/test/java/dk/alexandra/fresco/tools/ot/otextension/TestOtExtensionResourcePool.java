package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.ot.base.DummyOt;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class TestOtExtensionResourcePool {
  private Drbg rand;
  private Network network;
  private RotList seedOts;
  private CoinTossing ct;

  /**
   * Setup a correlated OT functionality.
   */
  @Before
  public void setup() {
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
    seedOts = new RotList(rand, 128);
    ct = new CoinTossing(1, 2, rand);
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testConstantAmountOfParties() {
    OtExtensionResourcePool resources = new OtExtensionResourcePoolImpl(1, 2,
        128, 40, 1, rand, ct, seedOts);
    assertEquals(2, resources.getNoOfParties());
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalInit() {
    boolean thrown = false;
    try {
      new CoteSender(new OtExtensionResourcePoolImpl(1, 2, 0, 40, 1, rand, ct,
          seedOts), network);
    } catch (IllegalArgumentException e) {
      assertEquals("Security parameters must be at least 1 and divisible by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      new CoteReceiver(new OtExtensionResourcePoolImpl(1, 2, 128, 0, 1, rand,
          ct, seedOts), network);
    } catch (IllegalArgumentException e) {
      assertEquals("Security parameters must be at least 1 and divisible by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      new CoteSender(new OtExtensionResourcePoolImpl(1, 2, 127, 40, 1, rand, ct,
          seedOts), network);
    } catch (IllegalArgumentException e) {
      assertEquals("Security parameters must be at least 1 and divisible by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      new CoteReceiver(new OtExtensionResourcePoolImpl(1, 2, 128, 60, 1, rand,
          ct, seedOts), network);
    } catch (IllegalArgumentException e) {
      assertEquals("Security parameters must be at least 1 and divisible by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testIllegalRotListSend() throws NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {
    RotList ots = new RotList(new AesCtrDrbg(HelperForTests.seedOne), 128);
    boolean thrown = false;
    try {
      ots.getSentMessages();
    } catch (IllegalStateException e) {
      assertEquals("Seed OTs have not been sent yet.", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    Field sent = RotList.class.getDeclaredField("sent");
    sent.setAccessible(true);
    sent.set(ots, true);
    try {
      ots.send(new DummyOt(2, network));
    } catch (IllegalStateException e) {
      assertEquals("Seed OTs have already been sent.", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testIllegalRotListReceive() throws NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {
    RotList ots = new RotList(new AesCtrDrbg(HelperForTests.seedOne), 128);
    boolean thrown = false;
    try {
      ots.getLearnedMessages();
    } catch (IllegalStateException e) {
      assertEquals("Seed OTs have not been received yet.", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      ots.getChoices();
    } catch (IllegalStateException e) {
      assertEquals("Seed OTs have not been received yet.", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);

    Field receive = RotList.class.getDeclaredField("received");
    receive.setAccessible(true);
    receive.set(ots, true);
    thrown = false;
    try {
      ots.receive(new DummyOt(2, network));
    } catch (IllegalStateException e) {
      assertEquals("Seed OTs have already been received.", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }
}