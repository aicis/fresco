package dk.alexandra.fresco.tools.cointossing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.otextension.CheatingNetwork;
import dk.alexandra.fresco.tools.ot.otextension.TestRuntime;

public class TestCoinTossing {
  private CoinTossing ctOne;
  private CoinTossing ctTwo;
  private TestRuntime testRuntime;

  private CoinTossing setupPartyOne() {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    Random rand = new Random(42);
    CoinTossing ct = new CoinTossing(1, 2, 128, rand, network);
    return ct;
  }

  private CoinTossing setupPartyTwo() {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Random rand = new Random(420);
    CoinTossing ct = new CoinTossing(2, 1, 128, rand, network);
    return ct;
  }

  private Exception initCtOne() {
    try {
      ctOne.initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  private Exception initCtTwo() {
    try {
      ctTwo.initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  /**
   * Initializes the test runtime and constructs a Cote Sender and a Cote
   * Receiver.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new TestRuntime();
    // define task each party will run
    Callable<CoinTossing> partyOneTask = () -> setupPartyOne();
    Callable<CoinTossing> partyTwoTask = () -> setupPartyTwo();
    // run tasks and get ordered list of results
    List<CoinTossing> results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    ctOne = results.get(0);
    ctTwo = results.get(1);
  }

  /**
   * Shuts down the network and test runtime.
   * 
   * @throws IOException
   *           Thrown if the network fails to shut down
   */
  @After
  public void shutdown() throws IOException {
    ((Closeable) ctOne.getNetwork()).close();
    ((Closeable) ctTwo.getNetwork()).close();
    testRuntime.shutdown();
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testCt() {
    int size = 10000;
    Callable<Exception> partyOneInit = () -> initCtOne();
    Callable<Exception> partyTwoInit = () -> initCtTwo();
    // run tasks and get ordered list of results
    List<Exception> initResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
    // Verify that no exception was thrown in init
    for (Exception current : initResults) {
      assertNull(current);
    }
    StrictBitVector tossOne = ctOne.toss(size);
    StrictBitVector tossTwo = ctTwo.toss(size);
    assertTrue(tossOne.equals(tossTwo));
    // Check that the toss is not a 0-string
    StrictBitVector zeroVec = new StrictBitVector(size);
    assertNotEquals(zeroVec, tossOne);
  }

  /**** NEGATIVE TESTS. ****/
  /****
   * Note that no cheating can occur in the coin tossing itself. If cheating
   * occurs it must be done in the commitments /** Verify that initialization
   * can only take place once.
   *****/
  @Test
  public void testDoubleInit() {
    Callable<Exception> partyOneTask = () -> initCtOne();
    Callable<Exception> partyTwoTask = () -> initCtTwo();
    // run tasks and get ordered list of results
    List<Exception> results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    for (Exception current : results) {
      assertNull(current);
    }
    // Call init twice
    results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    for (Exception current : results) {
      assertEquals("Already initialized", current.getMessage());
    }
  }

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
      ctOne.toss(128);
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
      ctOne.toss(0);
    } catch (IllegalArgumentException e) {
      assertEquals("At least one coin must be tossed.", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}