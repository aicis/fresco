package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.FailedOtException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;
import dk.alexandra.fresco.tools.ot.base.Ot;

public class FunctionalTestOtExtension {
  private TestRuntime testRuntime;
  private Cote coteSender;
  private Cote coteReceiver;

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
    coteSender = results.get(0);
    coteReceiver = results.get(1);
  }

  /**
   * Shuts down the network and test runtime.
   * 
   * @throws IOException
   *           Thrown if the network fails to shut down
   */
  @After
  public void shutdown() throws IOException {
    ((Closeable) coteSender.getSender().getNetwork()).close();
    ((Closeable) coteReceiver.getReceiver().getNetwork()).close();
    testRuntime.shutdown();
  }

  private Cote setupCoteSender()
      throws FailedOtExtensionException, IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    Random rand = new Random(42);
    Cote cote = new Cote(1, 2, 128, 40, rand, network);
    return cote;
  }

  private Cote setupCoteReceiver() throws FailedOtExtensionException, IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Random rand = new Random(420);
    Cote cote = new Cote(2, 1, 128, 40, rand, network);
    return cote;
  }

  private Exception initCoteSender() {
    try {
      coteSender.getSender().initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  private Exception initCoteReceiver() {
    try {
      coteReceiver.getReceiver().initialize();
    } catch (Exception e) {
      return e;
    }
    return null;
  }

  private Pair<List<StrictBitVector>, StrictBitVector> extendCoteSender(int size) {
    List<StrictBitVector> zeroMessages = coteSender.getSender().extend(size);
    Pair<List<StrictBitVector>, StrictBitVector> resPair = new Pair<>(
        zeroMessages, coteSender.getSender().getDelta());
    return resPair;
  }

  private Pair<List<StrictBitVector>, StrictBitVector> extendCoteReceiver(
      StrictBitVector choices)
      throws FailedOtExtensionException, IOException {
    List<StrictBitVector> messages = coteReceiver.getReceiver().extend(choices);
    // The return type of the extend method for sender and receiver must be the
    // same for the test to work, which is why we return null for the second
    // element.
    Pair<List<StrictBitVector>, StrictBitVector> resPair = new Pair<>(messages,
        null);
    return resPair;
  }
  
  /***** POSITIVE TESTS. *****/

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
    StrictBitVector zeroVec = new StrictBitVector(
        coteSender.getSender().getkBitLength());
    // Check that delta is not a 0-string
    assertNotEquals(new StrictBitVector(coteSender.getSender().getkBitLength()),
        delta);
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(extendSize), choices);
    // Check the length the values
    assertEquals(coteSender.getSender().getkBitLength(),
        coteReceiver.getReceiver().getkBitLength());
    assertEquals(extendSize, zeroMessages.size());
    assertEquals(extendSize, messages.size());
    for (int i = 0; i < extendSize; i++) {
      // Check the messages are not 0-strings
      assertNotEquals(zeroVec, zeroMessages.get(i));
      assertNotEquals(zeroVec, messages.get(i));
      // Check that they are not all equal
      if (i > 0) {
        assertNotEquals(zeroMessages.get(i - 1), zeroMessages.get(i));
        assertNotEquals(messages.get(i - 1), messages.get(i));
      }
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
   * Verify that we can initialize the parties in Rot.
   */
  @Test
  public void testRot() {
    int extendSize = 1880; // = 2048 - 128 - 40, where computational security is
    // 128 and statistical security is 40
    RotSender rotSender = new RotSender(coteSender.getSender());
    Callable<Exception> partyOneInit = () -> initRotSender(rotSender);
    RotReceiver rotReceiver = new RotReceiver(coteReceiver.getReceiver());
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
    StrictBitVector zeroVec = new StrictBitVector(
        coteSender.getSender().getkBitLength());
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(extendSize), choices);
    // Check the length the values
    assertEquals(coteSender.getSender().getkBitLength(),
        coteReceiver.getReceiver().getkBitLength());
    assertEquals(extendSize, zeroMessages.size());
    assertEquals(extendSize, messages.size());
    for (int i = 0; i < extendSize; i++) {
      // Check the messages are not 0-strings
      assertNotEquals(zeroVec, zeroMessages.get(i));
      assertNotEquals(zeroVec, oneMessages.get(i));
      assertNotEquals(zeroVec, messages.get(i));
      // Check that the two messages are not the same
      assertNotEquals(zeroMessages.get(i), oneMessages.get(i));
      // Check that they are not all equal
      if (i > 0) {
        assertNotEquals(zeroMessages.get(i - 1), zeroMessages.get(i));
        assertNotEquals(oneMessages.get(i - 1), oneMessages.get(i));
        assertNotEquals(messages.get(i - 1), messages.get(i));
      }
    }
  }

  private BigInteger bristolOtSend(Ot<BigInteger> otSender,
      BigInteger msgZero, BigInteger msgOne)
      throws MaliciousOtException, FailedOtException {
    otSender.send(msgZero, msgOne);
    // Return null since we need to return a BigInteger to get the test
    // framework to work
    return null;
  }

  private BigInteger bristolOtReceive(Ot<BigInteger> otReceiver, boolean choice)
      throws MaliciousOtException, FailedOtException {
    BigInteger message = otReceiver.receive(choice);
    return message;
  }

  /**
   * Verify that we can initialize the parties in OT.
   */
  @Test
  public void testBristolOt() {
    int batchSize = 856; // = 1024 - 128 - 40, where computational security is
    // 128 and statistical security is 40
    // We execute more OTs than the batchSize to ensure that an automatic
    // extension will take place once preprocessed OTs run out
    int iterations = 900;
    // Construct a BristolOT based on the coteSender and coteReceiver
    Rot rotSender = new Rot(coteSender);
    Rot rotReceiver = new Rot(coteReceiver);
    Ot<BigInteger> bristolOtSender = new BristolOt<BigInteger>(rotSender,
        batchSize);
    Ot<BigInteger> bristolOtReceiver = new BristolOt<BigInteger>(rotReceiver,
        batchSize);
    Random rand = new Random(540);
    for (int i = 0; i < iterations; i++) {
      BigInteger msgZero = new BigInteger(1024, rand);
      BigInteger msgOne = new BigInteger(1024, rand);
      boolean choice = rand.nextBoolean();
      Callable<BigInteger> partyOneOt = () -> bristolOtSend(bristolOtSender,
          msgZero, msgOne);
      Callable<BigInteger> partyTwoOt = () -> bristolOtReceive(
          bristolOtReceiver, choice);
      // run tasks and get ordered list of results
      List<BigInteger> extendResults = testRuntime
          .runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
      BigInteger receiverResult = extendResults.get(1);
      if (choice == false) {
        assertTrue(msgZero.equals(receiverResult));
      } else {
        assertTrue(msgOne.equals(receiverResult));
      }
    }
  }
  
  /***** NEGATIVE TESTS. *****/

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
    RotSender rotSender = new RotSender(coteSender.getSender());
    Callable<Exception> partyOneRotInit = () -> initRotSender(rotSender);
    RotReceiver rotReceiver = new RotReceiver(coteReceiver.getReceiver());
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

  /**
   * Test that a receiver who flips a bit its message from correlated OT with
   * errors results in a failure of the correlation test. This is not meant to
   * capture the best possible cheating strategy, but more as a sanity checks
   * that the proper checks are in place.
   */
  @Test
  public void testCheatingInRot() {
    int extendSize = 1880; // = 2048 - 128 - 40, where computational security is
    // 128 and statistical security is 40
    RotSender rotSender = new RotSender(coteSender.getSender());
    Callable<Exception> partyOneInit = () -> initRotSender(rotSender);
    RotReceiver rotReceiver = new RotReceiver(coteReceiver.getReceiver());
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
    // The next kbitLength messages sent are in correlated OT extension, this
    // should make the correlation check fail. Flipping a bit makes the
    // correlation check fail with 0.5 probability, up to the random choices of
    // the sender
    int corruptUVecPos = 2;
    ((CheatingNetwork) coteReceiver.getReceiver().getNetwork())
        .cheatInNextMessage(corruptUVecPos);
    Callable<Pair<List<StrictBitVector>, List<StrictBitVector>>> partyTwoExtend = () -> extendRotReceiver(
        rotReceiver, choices);
    // run tasks and get ordered list of results
    Object extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    // Cast the result as a list of Objects since they will have difference
    // types (an Exception and a Pair)
    @SuppressWarnings("unchecked")
    List<Object> castedRes = (List<Object>) extendResults;
    // Verify that the thrown exception is as expected
    assertTrue(castedRes.get(0) instanceof MaliciousOtExtensionException);
    assertEquals(
        ((MaliciousOtExtensionException) castedRes.get(0)).getMessage(),
        "Correlation check failed");
    // Check that the receiver did not throw an exception
    assertTrue(!(castedRes.get(1) instanceof MaliciousOtExtensionException));
    // Check that the sender had choice bit 1 in position 2. This means that we
    // expect the correlation check to fail
    assertEquals(true,
        coteSender.getSender().getDelta().getBit(corruptUVecPos, false));
  }
}
