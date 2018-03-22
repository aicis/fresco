package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
import dk.alexandra.fresco.tools.ot.base.Ot;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFunctionalBristolOt {
  private final int kbitLength = 128;
  private final int lambdaSecurityParam = 56;
  private final int messageLength = 1024;
  private OtExtensionTestContext senderContext;
  private OtExtensionTestContext receiverContext;
  private RuntimeForTests testRuntime;

  /**
   * Initializes the test runtime by setting up OT extension contexts for both sender and receiver.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new RuntimeForTests();
    Callable<OtExtensionTestContext> partyOneInit = this::bristolInitSender;
    Callable<OtExtensionTestContext> partyTwoInit = this::bristolInitReceiver;
    // run tasks and get ordered list of results
    List<OtExtensionTestContext> initResults =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
    senderContext = initResults.get(0);
    receiverContext = initResults.get(1);
  }

  /**
   * Shuts down the network and test runtime.
   *
   * @throws IOException Thrown if the network cannot shut down
   */
  @After
  public void shutdown() throws IOException {
    testRuntime.shutdown();
    ((Closeable) senderContext.getNetwork()).close();
    ((Closeable) receiverContext.getNetwork()).close();
  }

  private OtExtensionTestContext bristolInitSender() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength, lambdaSecurityParam);
    return ctx;
  }

  private OtExtensionTestContext bristolInitReceiver() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength, lambdaSecurityParam);
    return ctx;
  }

  private List<Pair<StrictBitVector, StrictBitVector>> bristolOtSend(OtExtensionTestContext ctx,
      int iterations, int batchSize) {
    OtExtensionResourcePool resources = ctx.createResources(1);
    Ot otSender = new BristolOtFactory(new RotFactory(resources, ctx.getNetwork()), resources,
        ctx.getNetwork(), batchSize);
    List<Pair<StrictBitVector, StrictBitVector>> messages = new ArrayList<>(iterations);
    Drbg rand = ctx.createRand(1);
    byte[] msgBytes = new byte[messageLength / 8];
    for (int i = 0; i < iterations; i++) {
      rand.nextBytes(msgBytes);
      StrictBitVector msgZero = new StrictBitVector(msgBytes);
      rand.nextBytes(msgBytes);
      StrictBitVector msgOne = new StrictBitVector(msgBytes);
      otSender.send(msgZero, msgOne);
      Pair<StrictBitVector, StrictBitVector> currentPair = new Pair<>(msgZero, msgOne);
      messages.add(currentPair);
    }
    return messages;
  }

  private List<StrictBitVector> bristolOtReceive(OtExtensionTestContext ctx,
      StrictBitVector choices, int batchSize) {
    OtExtensionResourcePool resources = ctx.createResources(1);
    Ot otReceiver = new BristolOtFactory(new RotFactory(resources, ctx.getNetwork()), resources,
        ctx.getNetwork(), batchSize);
    List<StrictBitVector> messages = new ArrayList<>(choices.getSize());
    for (int i = 0; i < choices.getSize(); i++) {
      StrictBitVector message = otReceiver.receive(choices.getBit(i, false));
      messages.add(message);
    }
    return messages;
  }

  /**
   * Verify that we can execute the OT.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testBristolOt() {
    // The batchsize of the underlying implementation must be a two power minus
    // kbitLength and lambdaBitLength
    int batchSize = 1024 - kbitLength - lambdaSecurityParam;
    // We execute more OTs than the batchSize to ensure that an automatic
    // extension will take place once preprocessed OTs run out
    int iterations = 1032;
    Drbg rand = new AesCtrDrbg(HelperForTests.seedThree);
    StrictBitVector choices = new StrictBitVector(iterations, rand);
    Callable<List<?>> partyOneOt = () -> bristolOtSend(senderContext, iterations, batchSize);
    Callable<List<?>> partyTwoOt = () -> bristolOtReceive(receiverContext, choices, batchSize);
    // run tasks and get ordered list of results
    List<List<?>> extendResults =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
    HelperForTests.verifyOts((List<Pair<StrictBitVector, StrictBitVector>>) extendResults.get(0),
        (List<StrictBitVector>) extendResults.get(1), choices);
  }

  private List<Pair<StrictBitVector, StrictBitVector>> bristolRotBatchSend(
      OtExtensionTestContext ctx, int batchSize, int id) {
    OtExtensionResourcePool resources = ctx.createResources(id);
    BristolRotBatch rotBatchSender =
        new BristolRotBatch(new RotFactory(resources, ctx.getNetwork()),
            resources.getComputationalSecurityParameter(), resources.getLambdaSecurityParam());
    List<Pair<StrictBitVector, StrictBitVector>> messages =
        rotBatchSender.send(batchSize, messageLength);
    return messages;
  }

  private List<Pair<StrictBitVector, StrictBitVector>> bristolRotBatchSendTwice(
      OtExtensionTestContext ctx, int batchSize, int id) {
    OtExtensionResourcePool resources = ctx.createResources(id);
    BristolRotBatch rotBatchSender =
        new BristolRotBatch(new RotFactory(resources, ctx.getNetwork()),
            resources.getComputationalSecurityParameter(), resources.getLambdaSecurityParam());
    List<Pair<StrictBitVector, StrictBitVector>> messages =
        rotBatchSender.send(batchSize, messageLength);
    messages.addAll(rotBatchSender.send(batchSize, messageLength));
    return messages;
  }

  private List<StrictBitVector> bristolRotBatchReceive(OtExtensionTestContext ctx,
      StrictBitVector choices, int id) {
    OtExtensionResourcePool resources = ctx.createResources(id);
    BristolRotBatch rotBatchReceiver =
        new BristolRotBatch(new RotFactory(resources, ctx.getNetwork()),
            resources.getComputationalSecurityParameter(), resources.getLambdaSecurityParam());
    List<StrictBitVector> messages = rotBatchReceiver.receive(choices, messageLength);
    return messages;
  }

  private List<StrictBitVector> bristolRotBatchReceiveTwice(OtExtensionTestContext ctx,
      StrictBitVector choices, int id) {
    OtExtensionResourcePool resources = ctx.createResources(id);
    BristolRotBatch rotBatchReceiver =
        new BristolRotBatch(new RotFactory(resources, ctx.getNetwork()),
            resources.getComputationalSecurityParameter(), resources.getLambdaSecurityParam());
    List<StrictBitVector> messages = rotBatchReceiver.receive(choices, messageLength);
    messages.addAll(rotBatchReceiver.receive(choices, messageLength));
    return messages;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBristolRotTwice() {
    int extendSize = 128;
    Callable<List<?>> partyOneExtend = () -> bristolRotBatchSendTwice(senderContext, extendSize, 1);
    // Pick some random choice bits
    Drbg rand = new AesCtrDrbg(HelperForTests.seedThree);
    StrictBitVector choices = new StrictBitVector(extendSize, rand);
    Callable<List<?>> partyTwoExtend = () ->
        bristolRotBatchReceiveTwice(receiverContext, choices, 1);
    // run tasks and get ordered list of results
    List<List<?>> extendResults =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    HelperForTests.verifyOts((List<Pair<StrictBitVector, StrictBitVector>>) extendResults.get(0),
        (List<StrictBitVector>) extendResults.get(1), StrictBitVector.concat(choices, choices));
  }



  @SuppressWarnings("unchecked")
  @Test
  public void testBristolRot() {
    int extendSize = 1024;
    Callable<List<?>> partyOneExtend = () -> bristolRotBatchSend(senderContext, extendSize, 1);
    // Pick some random choice bits
    Drbg rand = new AesCtrDrbg(HelperForTests.seedThree);
    StrictBitVector choices = new StrictBitVector(extendSize, rand);
    Callable<List<?>> partyTwoExtend = () -> bristolRotBatchReceive(receiverContext, choices, 1);
    // run tasks and get ordered list of results
    List<List<?>> extendResults =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    HelperForTests.verifyOts((List<Pair<StrictBitVector, StrictBitVector>>) extendResults.get(0),
        (List<StrictBitVector>) extendResults.get(1), choices);
  }



  @SuppressWarnings("unchecked")
  @Test
  public void testBristolRotReuseSeeds() {
    // // Verify that the BristolRot can do auto initialization
    int extendSize = 1024;
    int iterations = 15;
    StrictBitVector previousChoices = null;
    List<Pair<StrictBitVector, StrictBitVector>> previousMessages = null;
    for (int i = 0; i < iterations; i++) {
      final int sessionId = i;
      Callable<List<?>> partyOneExtend =
          () -> bristolRotBatchSend(senderContext, extendSize, sessionId);
      byte[] seed = HelperForTests.seedThree;
      // Make sure the seed used is unique for each thread
      seed[0] ^= (byte) i;
      Drbg rand = new AesCtrDrbg(seed);
      StrictBitVector choices = new StrictBitVector(extendSize, rand);
      Callable<List<?>> partyTwoExtend =
          () -> bristolRotBatchReceive(receiverContext, choices, sessionId);
      // run tasks and get ordered list of results
      List<List<?>> extendResults =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
      List<Pair<StrictBitVector, StrictBitVector>> senderResults =
          (List<Pair<StrictBitVector, StrictBitVector>>) extendResults.get(0);
      List<StrictBitVector> receiverResults = (List<StrictBitVector>) extendResults.get(1);
      HelperForTests.verifyOts(senderResults, receiverResults, choices);
      // Sanity check:
      // Ensure that we don't get the same values in each parallel execution
      if (previousChoices != null) {
        assertNotEquals(previousChoices, choices);
      }
      for (int j = 0; j < choices.getSize(); j++) {
        // Ensure that we don't get the same messages in each execution
        if (previousMessages != null) {
          assertNotEquals(previousMessages.get(j), senderResults.get(j));
          // The following check is needed to ensure that the senders messages are
          // actually randomized and that not only the receiver's choices are
          // randomized, as the receiver's choices affect the order of each pair of
          // messages.
          assertNotEquals(previousMessages.get(j).getFirst(), senderResults.get(j).getSecond());
        }
      }
      previousChoices = choices;
      previousMessages = senderResults;
    }
  }

  private Exception bristolOtMaliciousSend(OtExtensionTestContext ctx, int iterations,
      int batchSize) {
    OtExtensionResourcePool resources = ctx.createResources(1);
    BristolOtFactory otSender = new BristolOtFactory(new RotFactory(resources, ctx.getNetwork()),
        resources, ctx.getNetwork(), batchSize);
    Drbg rand = ctx.createRand(1);
    byte[] msgBytes = new byte[messageLength / 8];
    rand.nextBytes(msgBytes);
    StrictBitVector msgZero = new StrictBitVector(msgBytes);
    rand.nextBytes(msgBytes);
    StrictBitVector msgOne = new StrictBitVector(msgBytes);
    otSender.send(msgZero, msgOne);
    return null;
  }

  private Exception bristolOtMaliciousReceive(OtExtensionTestContext ctx, StrictBitVector choices,
      int batchSize) throws NoSuchMethodException, SecurityException, IllegalArgumentException,
      IllegalAccessException, NoSuchFieldException {
    OtExtensionResourcePool resources = ctx.createResources(1);
    BristolOtFactory otReceiver = new BristolOtFactory(new RotFactory(resources, ctx.getNetwork()),
        resources, ctx.getNetwork(), batchSize);
    otReceiver.receive(choices.getBit(0, false));
    Field receiver = BristolOtFactory.class.getDeclaredField("receiver");
    receiver.setAccessible(true);
    Method method = receiver.get(otReceiver).getClass().getDeclaredMethod("doActualReceive",
        byte[].class, byte[].class);
    method.setAccessible(true);
    Exception exception = null;
    try {
      method.invoke(receiver.get(otReceiver), new byte[] { 0x42 }, new byte[] { 0x42, 0x43 });
    } catch (Exception e) {
      exception = e;
    }
    return exception;
  }

  @Test
  public void testMaliciousException() throws SecurityException, IllegalArgumentException {
    int batchSize = 1024 - kbitLength - lambdaSecurityParam;
    StrictBitVector choices = new StrictBitVector(8);
    Callable<Exception> partyOneOt = () -> bristolOtMaliciousSend(senderContext, 8, batchSize);
    Callable<Exception> partyTwoOt =
        () -> bristolOtMaliciousReceive(receiverContext, choices, batchSize);
    // run tasks and get ordered list of results
    List<Exception> extendResults =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
    assertEquals(MaliciousException.class, extendResults.get(1).getCause().getClass());
    assertEquals("Sender gave adjustment messages of different length.",
        extendResults.get(1).getCause().getMessage());
  }

  /**** UNIT TESTS. ****/
  @Test
  public void testComputeExtensionSize() {
    int res;
    res = BristolRotBatch.computeExtensionSize(88, 128, 40);
    assertEquals(88, res);

    res = BristolRotBatch.computeExtensionSize(88, 64, 16);
    assertEquals(176, res);

    res = BristolRotBatch.computeExtensionSize(2048, 16, 16);
    assertEquals(4064, res);

    res = BristolRotBatch.computeExtensionSize(2000, 32, 16);
    assertEquals(2000, res);

    res = BristolRotBatch.computeExtensionSize(2000, 32, 8);
    assertEquals(2008, res);

    res = BristolRotBatch.computeExtensionSize(14, 192, 80);
    assertEquals(240, res);

    res = BristolRotBatch.computeExtensionSize(2, 8, 8);
    assertEquals(16, res);
  }
}
