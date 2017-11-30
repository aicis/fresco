package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class FunctionalTestOtExtension {
  private TestRuntime testRuntime;
  private CoteSender coteSender;
  private CoteReceiver coteReceiver;

  /**
   * Initializes the test runtime and constructs a Cote Sender and a Cote
   * Receiver.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new TestRuntime();
    // define task each party will run
    Callable<Cote> partyOneTask = () -> setupCoteSender();
    Callable<Cote> partyTwoTask = () -> setupCoteReceiver();
    // run tasks and get ordered list of results
    List<Cote> results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    coteSender = results.get(0).getSender();
    coteReceiver = results.get(1).getReceiver();
  }

  /**
   * Shuts down the network and test runtime.
   * 
   * @throws IOException
   *           Thrown if the network fails to shut down
   */
  @After
  public void shutdown() throws IOException {
    ((Closeable) coteSender.getNetwork()).close();
    ((Closeable) coteReceiver.getNetwork()).close();
    testRuntime.shutdown();
  }

  /**
   * Returns a default network configuration.
   * 
   * @param myId
   *          The calling party's network ID
   * @param partyIds
   *          The IDs of all parties
   * @return The network configuration
   */
  public static NetworkConfiguration defaultNetworkConfiguration(Integer myId,
      List<Integer> partyIds) {
    Map<Integer, Party> parties = new HashMap<>();
    for (Integer partyId : partyIds) {
      parties.put(partyId, new Party(partyId, "localhost", 8000 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

  private Cote setupCoteSender()
      throws FailedOtExtensionException, IOException {
    Network network = new KryoNetNetwork(
        defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    Random rand = new Random(42);
    Cote cote = new Cote(1, 2, 128, 40, rand, network);
    return cote;
  }

  private Cote setupCoteReceiver() throws FailedOtExtensionException, IOException {
    Network network = new KryoNetNetwork(
        defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Random rand = new Random(420);
    Cote cote = new Cote(2, 1, 128, 40, rand, network);
    return cote;
  }

  private Exception initCoteSender() {
    try {
      coteSender.initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  private Exception initCoteReceiver() {
    try {
      coteReceiver.initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  private Pair<List<StrictBitVector>, StrictBitVector> extendCoteSender(int size) {
    List<StrictBitVector> zeroMessages = coteSender.extend(size);
    Pair<List<StrictBitVector>, StrictBitVector> resPair = new Pair<>(
        zeroMessages, coteSender.getDelta());
    return resPair;
  }

  private Pair<List<StrictBitVector>, StrictBitVector> extendCoteReceiver(
      StrictBitVector choices)
      throws FailedOtExtensionException, IOException {
    List<StrictBitVector> messages = coteReceiver.extend(choices);
    // The return type of the extend method for sender and receiver must be the
    // same for the test to work, which is why we return null for the second
    // element.
    Pair<List<StrictBitVector>, StrictBitVector> resPair = new Pair<>(messages,
        null);
    return resPair;
  }

  /**
   * Verify that we can initialize the parties in Cote.
   */
  @Test
  public void testCote() {
    int extendSize = 1024;
    Callable<Exception> partyOneInit = () -> initCoteSender();
    Callable<Exception> partyTwoInit = () -> initCoteReceiver();
    // run tasks and get ordered list of results
    List<Exception> initResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
    // Verify that no exception was thrown in init
    for (Exception current : initResults) {
      assertNull(current);
    }
    Callable<Pair<List<StrictBitVector>, StrictBitVector>> partyOneExtend = () -> extendCoteSender(
        extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize, new Random(540));
    Callable<Pair<List<StrictBitVector>, StrictBitVector>> partyTwoExtend = () -> extendCoteReceiver(
        choices);
    // run tasks and get ordered list of results
    List<Pair<List<StrictBitVector>, StrictBitVector>> extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    Pair<List<StrictBitVector>, StrictBitVector> senderResults = extendResults
        .get(0);
    List<StrictBitVector> zeroMessages = senderResults.getFirst();
    StrictBitVector delta = senderResults.getSecond();
    Pair<List<StrictBitVector>, StrictBitVector> receiverResults = extendResults
        .get(1);
    List<StrictBitVector> messages = receiverResults.getFirst();
    for (int i = 0; i < choices.getSize(); i++) {
      if (choices.getBit(i, false) == true) {
        zeroMessages.get(i).xor(delta);
      }
      assertTrue(zeroMessages.get(i).equals(messages.get(i)));
    }
    // Do a sanity check of the values
    StrictBitVector zeroVec = new StrictBitVector(coteSender.getkBitLength());
    // Check that delta is not a 0-string
    assertNotEquals(new StrictBitVector(coteSender.getkBitLength()), delta);
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(extendSize), choices);
    // Check the length the values
    assertEquals(coteSender.getkBitLength(), coteReceiver.getkBitLength());
    assertEquals(extendSize, zeroMessages.size());
    assertEquals(extendSize, messages.size());
    for (int i = 0; i < extendSize; i++) {
      // Check the messages are not 0-strings
      assertNotEquals(zeroVec, zeroMessages.get(i));
      assertNotEquals(zeroVec, messages.get(i));
    }
  }

  private Exception initRotSender(RotSender rotSender) {
    try {
      rotSender.initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  private Exception initRotReceiver(RotReceiver rotReceiver) {
    try {
      rotReceiver.initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  private Pair<List<StrictBitVector>, List<StrictBitVector>> extendRotSender(
      RotSender rotSender, int size)
      throws MaliciousOtExtensionException, FailedOtExtensionException {
    Pair<List<StrictBitVector>, List<StrictBitVector>> messages = rotSender
        .extend(size);
    return messages;
  }

  private Pair<List<StrictBitVector>, List<StrictBitVector>> extendRotReceiver(
      RotReceiver rotReceiver, StrictBitVector choices)
      throws FailedOtExtensionException, IOException {
    List<StrictBitVector> messages = rotReceiver.extend(choices);
    // The return type of the extend method for sender and receiver must be the
    // same for the test to work, which is why we return null for the second
    // element.
    Pair<List<StrictBitVector>, List<StrictBitVector>> resPair = new Pair<>(
        messages, null);
    return resPair;
  }

  /**
   * Verify that we can initialize the parties in Cote.
   */
  @Test
  public void testRot() {
    int extendSize = 1880; // = 2048 - 128 - 40, where computational security is
                          // 128 and statistical security is 40
    RotSender rotSender = new RotSender(coteSender);
    Callable<Exception> partyOneInit = () -> initRotSender(rotSender);
    RotReceiver rotReceiver = new RotReceiver(coteReceiver);
    Callable<Exception> partyTwoInit = () -> initRotReceiver(rotReceiver);
    // run tasks and get ordered list of results
    List<Exception> initResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
    // Verify that no exception was thrown in init
    for (Exception current : initResults) {
      assertNull(current);
    }
    Callable<Pair<List<StrictBitVector>, List<StrictBitVector>>> partyOneExtend = () -> extendRotSender(
        rotSender, extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize, new Random(540));
    Callable<Pair<List<StrictBitVector>, List<StrictBitVector>>> partyTwoExtend = () -> extendRotReceiver(
        rotReceiver, choices);
    // run tasks and get ordered list of results
    List<Pair<List<StrictBitVector>, List<StrictBitVector>>> extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    Pair<List<StrictBitVector>, List<StrictBitVector>> senderResults = extendResults
        .get(0);
    List<StrictBitVector> zeroMessages = senderResults.getFirst();
    List<StrictBitVector> oneMessages = senderResults.getSecond();
    Pair<List<StrictBitVector>, List<StrictBitVector>> receiverResults = extendResults
        .get(1);
    List<StrictBitVector> messages = receiverResults.getFirst();
    for (int i = 0; i < choices.getSize(); i++) {
      if (choices.getBit(i, false) == false) {
        assertTrue(zeroMessages.get(i).equals(messages.get(i)));
      } else {
        assertTrue(oneMessages.get(i).equals(messages.get(i)));
      }
    }
    // Do a sanity check of the values
    StrictBitVector zeroVec = new StrictBitVector(coteSender.getkBitLength());
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(extendSize), choices);
    // Check the length the values
    assertEquals(coteSender.getkBitLength(), coteReceiver.getkBitLength());
    assertEquals(extendSize, zeroMessages.size());
    assertEquals(extendSize, messages.size());
    for (int i = 0; i < extendSize; i++) {
      // Check the messages are not 0-strings
      assertNotEquals(zeroVec, zeroMessages.get(i));
      assertNotEquals(zeroVec, oneMessages.get(i));
      assertNotEquals(zeroVec, messages.get(i));
      // Check that the two messages are not the same
      assertNotEquals(zeroMessages.get(i), oneMessages.get(i));
    }
  }

  /**
   * Verify that initialization can only take place once.
   */
  @Test 
  public void testDoubleInitCote() {
    Callable<Exception> partyOneTask = () -> initCoteSender();
    Callable<Exception> partyTwoTask = () -> initCoteReceiver();
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
  public void testDoubleInitRot() {
    Callable<Exception> partyOneCoteInit = () -> initCoteSender();
    Callable<Exception> partyTwoCoteInit = () -> initCoteReceiver();
    // run tasks and get ordered list of results
    List<Exception> results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneCoteInit, partyTwoCoteInit));
    for (Exception current : results) {
      assertNull(current);
    }
    RotSender rotSender = new RotSender(coteSender);
    Callable<Exception> partyOneRotInit = () -> initRotSender(rotSender);
    RotReceiver rotReceiver = new RotReceiver(coteReceiver);
    Callable<Exception> partyTwoRotInit = () -> initRotReceiver(rotReceiver);
    results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneRotInit, partyTwoRotInit));
    // Rot still needs to be initialized, in particular the coin tossing
    for (Exception current : results) {
      assertNull(current);
    }
    // Call init on rot again
    results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneRotInit, partyTwoRotInit));
    for (Exception current : results) {
      assertEquals("Already initialized", current.getMessage());
    }
  }
}
