package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFunctionalOtExtension {
  private RuntimeForTests testRuntime;
  private Cote coteSender;
  private OtExtensionResourcePool senderResources;
  private Cote coteReceiver;
  private OtExtensionResourcePool receiverResources;
  private int kbitLength = 128;
  private int lambdaSecurityParam = 64;

  /**
   * Initializes the test runtime and constructs a Cote Sender and a Cote
   * Receiver.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new RuntimeForTests();
    // define task each party will run
    Callable<Pair<Cote, OtExtensionResourcePool>> partyOneTask = this::setupCoteSender;
    Callable<Pair<Cote, OtExtensionResourcePool>> partyTwoTask = this::setupCoteReceiver;
    // run tasks and get ordered list of results
    List<Pair<Cote, OtExtensionResourcePool>> results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    coteSender = results.get(0).getFirst();
    senderResources = results.get(0).getSecond();
    coteReceiver = results.get(1).getFirst();
    receiverResources = results.get(1).getSecond();
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

  private Pair<Cote, OtExtensionResourcePool> setupCoteSender() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength,
        lambdaSecurityParam);
    OtExtensionResourcePool resources = ctx.createResources(1);
    Cote cote = new Cote(resources, ctx.getNetwork());
    return new Pair<>(cote, resources);
  }

  private Pair<Cote, OtExtensionResourcePool> setupCoteReceiver() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength,
        lambdaSecurityParam);
    OtExtensionResourcePool resources = ctx.createResources(1);
    Cote cote = new Cote(resources, ctx.getNetwork());
    return new Pair<>(cote, resources);
  }

  /***** POSITIVE TESTS. *****/

  private List<Pair<StrictBitVector, StrictBitVector>> extendCoteSender(
      int size) {
    List<StrictBitVector> zeroMessages = coteSender.getSender().extend(size);
    StrictBitVector delta = coteSender.getSender().getDelta();
    List<Pair<StrictBitVector, StrictBitVector>> res = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      StrictBitVector oneMessage = new StrictBitVector(zeroMessages.get(i)
          .toByteArray());
      oneMessage.xor(delta);
      Pair<StrictBitVector, StrictBitVector> current = new Pair<>(zeroMessages
          .get(i), oneMessage);
      res.add(current);
    }
    return res;
  }

  private List<StrictBitVector> extendCoteReceiver(StrictBitVector choices) {
    return coteReceiver.getReceiver().extend(choices);
  }

  /**
   * Verify that we can initialize the parties in Cote.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testCote() {
    int extendSize = 1024;
    Callable<List<?>> partyOneExtend = () -> extendCoteSender(extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize,
        new AesCtrDrbg(HelperForTests.seedThree));
    Callable<List<?>> partyTwoExtend = () -> extendCoteReceiver(choices);
    // run tasks and get ordered list of results
    List<List<?>> extendResults = testRuntime.runPerPartyTasks(Arrays.asList(
        partyOneExtend, partyTwoExtend));
    List<Pair<StrictBitVector, StrictBitVector>> senderResults =
        (List<Pair<StrictBitVector, StrictBitVector>>) extendResults.get(0);
    List<StrictBitVector> receiverResults = (List<StrictBitVector>) extendResults
        .get(1);
    HelperForTests.verifyOts(senderResults, receiverResults, choices);
  }

  private List<Pair<StrictBitVector, StrictBitVector>> extendRotSender(
      int size) {
    RotSender rotSender = new RotSenderImpl(coteSender.getSender(),
        senderResources.getCoinTossing());
    Pair<List<StrictBitVector>, List<StrictBitVector>> messages = rotSender
        .extend(size);
    List<Pair<StrictBitVector, StrictBitVector>> res = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Pair<StrictBitVector, StrictBitVector> current = new Pair<>(messages
          .getFirst().get(i), messages.getSecond().get(i));
      res.add(current);
    }
    return res;
  }

  private List<StrictBitVector> extendRotReceiver(StrictBitVector choices) {
    RotReceiver rotReceiver = new RotReceiverImpl(coteReceiver.getReceiver(),
        receiverResources.getCoinTossing());
    return rotReceiver.extend(choices);
  }

  /**
   * Verify that we can initialize the parties in Rot.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testRot() {
    int extendSize = 2048 - kbitLength - lambdaSecurityParam;
    Callable<List<?>> partyOneExtend = () -> extendRotSender(extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize,
        new AesCtrDrbg(HelperForTests.seedThree));
    Callable<List<?>> partyTwoExtend = () -> extendRotReceiver(choices);
    // run tasks and get ordered list of results
    List<List<?>> extendResults = testRuntime.runPerPartyTasks(Arrays.asList(
        partyOneExtend, partyTwoExtend));
    List<Pair<StrictBitVector, StrictBitVector>> senderResults =
        (List<Pair<StrictBitVector, StrictBitVector>>) extendResults.get(0);
    List<StrictBitVector> receiverResults = (List<StrictBitVector>) extendResults
        .get(1);
    HelperForTests.verifyOts(senderResults, receiverResults, choices);
  }

  /***** NEGATIVE TESTS. *****/

  /**
   * Test that a receiver who flips a bit its message from correlated OT with
   * errors results in a failure of the correlation test. This is not meant to
   * capture the best possible cheating strategy, but more as a sanity checks
   * that the proper checks are in place.
   */
  @Test
  public void testCheatingInRot() {
    int extendSize = 2048 - kbitLength - lambdaSecurityParam;
    Callable<List<?>> partyOneExtend =
        () -> extendRotSender(extendSize);
    StrictBitVector choices = new StrictBitVector(extendSize,
        new AesCtrDrbg(HelperForTests.seedThree));
    // The next kbitLength messages sent are in correlated OT extension.
    // Flipping a bit makes the correlation check fail with 0.5 probability,
    // up to the random choices of the sender. We have verifies that for the static
    // randomness used by our tests this happens for choice 1
    int corruptUVecPos = 1;
    ((CheatingNetwork) coteReceiver.getReceiver().getNetwork())
        .cheatInNextMessage(corruptUVecPos, 0);
    Callable<List<?>> partyTwoExtend = () -> extendRotReceiver(choices);
    // run tasks and get ordered list of results
    List<?> extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    // Verify that the thrown exception is as expected
    assertTrue(extendResults.get(0) instanceof MaliciousException);
    assertEquals(
        "Correlation check failed for the sender in the random OT extension",
        ((MaliciousException) extendResults.get(0)).getMessage());
    // Check that the receiver did not throw an exception
    assertTrue(!(extendResults.get(1) instanceof MaliciousException));
    // Check that the sender had choice bit 1 in position 2. This means that we
    // expect the correlation check to fail
    assertEquals(true,
        coteSender.getSender().getDelta().getBit(corruptUVecPos, false));
  }
}
