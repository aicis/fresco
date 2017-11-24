package dk.alexandra.fresco.tools.cointossing;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;

public class TestCoinTossing {
  private CoinTossing ct;

  /**
   * Construct a dummy coin-tossing object for unit testing, i.e. without
   * network connection.
   */
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
    this.ct = new CoinTossing(1, 2, 128, rand, network);
  }

  /**** POSITIVE TESTS. ****/
  // TODO wait for integration test fixture

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalInit() {
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

    CoinTossing ct;
    boolean thrown = false;
    try {
      ct = new CoinTossing(0, 1, 128, null, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      ct = new CoinTossing(0, 1, 128, rand, null);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    thrown = false;
    try {
      ct = new CoinTossing(0, 1, 0, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      ct = new CoinTossing(0, 1, 67, rand, network);
    } catch (IllegalArgumentException e) {
      assertEquals("Computational security parameter must be divisible by 8",
          e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testNotInitialized() {
    boolean thrown = false;
    try {
      ct.toss(128);
    } catch (IllegalStateException e) {
      assertEquals("Not initialized", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testIncorrectSize() {
    boolean thrown = false;
    try {
      ct.toss(0);
    } catch (IllegalArgumentException e) {
      assertEquals("At least one coin must be tossed.", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}
