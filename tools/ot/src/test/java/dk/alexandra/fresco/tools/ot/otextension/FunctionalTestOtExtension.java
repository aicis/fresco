package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.Constants;
import dk.alexandra.fresco.tools.helper.TestRuntime;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FunctionalTestOtExtension {
  private TestRuntime testRuntime;
  private Cote coteSender;
  private Cote coteReceiver;
  private int kbitLength = 128;
  private int lambdaSecurityParam = 64;

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

  private Cote setupCoteSender() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength,
        lambdaSecurityParam);
    Cote cote = new Cote(ctx.getResources(), ctx.getNetwork(), ctx
        .getDummyOtInstance());
    return cote;
  }

  private Cote setupCoteReceiver() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength,
        lambdaSecurityParam);
    Cote cote = new Cote(ctx.getResources(), ctx.getNetwork(), ctx
        .getDummyOtInstance());
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
      StrictBitVector choices) throws IOException {
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
    Callable<Pair<List<StrictBitVector>, StrictBitVector>> partyOneExtend = 
        () -> extendCoteSender(extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize,
        new AesCtrDrbg(Constants.seedThree));
    Callable<Pair<List<StrictBitVector>, StrictBitVector>> partyTwoExtend = 
        () -> extendCoteReceiver(choices);
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
      // If the receiver choose the 1-message then XOR delta onto zeroMessage
      if (choices.getBit(i, false) == true) {
        zeroMessages.get(i).xor(delta);
      }
      // Now verify that the zero message (maybe with delta xor'ed into it) fits
      // the message the receiver got
      assertTrue(zeroMessages.get(i).equals(messages.get(i)));
    }
    // Do a sanity check of the values:
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
      RotSender rotSender, int size) {
    Pair<List<StrictBitVector>, List<StrictBitVector>> messages = rotSender
        .extend(size);
    return messages;
  }

  private Pair<List<StrictBitVector>, List<StrictBitVector>> extendRotReceiver(
      RotReceiver rotReceiver, StrictBitVector choices) throws IOException {
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
    int extendSize = 2048 - kbitLength - lambdaSecurityParam;
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
    Callable<Pair<List<StrictBitVector>, List<StrictBitVector>>> partyOneExtend = 
        () -> extendRotSender(rotSender, extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize,
        new AesCtrDrbg(Constants.seedThree));
    Callable<Pair<List<StrictBitVector>, List<StrictBitVector>>> partyTwoExtend = 
        () -> extendRotReceiver(rotReceiver, choices);
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
      // Verify that the receiver got the correct message in accordance with his
      // choicebit
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
    int extendSize = 2048 - kbitLength - lambdaSecurityParam;
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
    Callable<Pair<List<StrictBitVector>, List<StrictBitVector>>> partyOneExtend = 
        () -> extendRotSender(rotSender, extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize,
        new AesCtrDrbg(Constants.seedThree));
    // The next kbitLength messages sent are in correlated OT extension.
    // Flipping a bit makes the
    // correlation check fail with 0.5 probability, up to the random choices of
    // the sender. We have verifies that for the static randomness used by our
    // tests this happens for choice 1
    int corruptUVecPos = 1;
    ((CheatingNetwork) coteReceiver.getReceiver().getNetwork())
        .cheatInNextMessage(corruptUVecPos, 0);
    Callable<Pair<List<StrictBitVector>, List<StrictBitVector>>> partyTwoExtend = 
        () -> extendRotReceiver(rotReceiver, choices);
    // run tasks and get ordered list of results
    Object extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    // Cast the result as a list of Objects since they will have difference
    // types (an Exception and a Pair)
    @SuppressWarnings("unchecked")
    List<Object> castedRes = (List<Object>) extendResults;
    // Verify that the thrown exception is as expected
    assertTrue(castedRes.get(0) instanceof MaliciousException);
    assertEquals(
        ((MaliciousException) castedRes.get(0)).getMessage(),
        "Correlation check failed for the sender in the random OT extension");
    // Check that the receiver did not throw an exception
    assertTrue(!(castedRes.get(1) instanceof MaliciousException));
    // Check that the sender had choice bit 1 in position 2. This means that we
    // expect the correlation check to fail
    assertEquals(true,
        coteSender.getSender().getDelta().getBit(corruptUVecPos, false));
  }
}
