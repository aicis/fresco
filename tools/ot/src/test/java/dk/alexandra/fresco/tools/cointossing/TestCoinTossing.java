package dk.alexandra.fresco.tools.cointossing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
import dk.alexandra.fresco.tools.ot.otextension.CheatingNetwork;

import java.io.Closeable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCoinTossing {
  private CoinTossing ctOne;
  private CoinTossing ctTwo;
  private RuntimeForTests testRuntime;

  private CoinTossing setupPartyOne() throws NoSuchAlgorithmException {
    Network network = new CheatingNetwork(
        RuntimeForTests.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    // To stress things we use HMAC for one party and AES for another
    Drbg rand = new HmacDrbg(HelperForTests.seedOne);
    CoinTossing ct = new CoinTossing(1, 2, rand, network);
    return ct;
  }

  private CoinTossing setupPartyTwo() {
    Network network = new CheatingNetwork(
        RuntimeForTests.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Drbg rand = new AesCtrDrbg(HelperForTests.seedTwo);
    CoinTossing ct = new CoinTossing(2, 1, rand, network);
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
   * Initializes the test runtime and constructs two Coin-tossing instances.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new RuntimeForTests();
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

  /****
   * NEGATIVE TESTS. Note that no cheating can occur in the coin tossing itself.
   * If cheating occurs it must be done in the commitments.
   *****/

  /**
   * Verify that initialization can only take place once.
   **/
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
