package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.Constants;
import dk.alexandra.fresco.tools.helper.TestRuntime;
import dk.alexandra.fresco.tools.ot.base.Ot;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FunctionalTestBristolOt {
  private TestRuntime testRuntime;
  private final int kbitLength = 128;
  private final int lambdaSecurityParam = 56;
  private final int messageLength = 1024;

  /**
   * Initializes the test runtime.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new TestRuntime();
  }

  /**
   * Shuts down the network and test runtime.
   */
  @After
  public void shutdown() {
    testRuntime.shutdown();
  }

  private OtExtensionTestContext bristolInitSender() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength,
        lambdaSecurityParam);
    // Remember that the sender/receiver roles are inverted for seed OTs
    // ctx.getDummyOtInstance().receive();
    return ctx;
  }

  private OtExtensionTestContext bristolInitReceiver() {
    OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength,
        lambdaSecurityParam);
    // Remember that the sender/receiver roles are inverted for seed OTs
    // ctx.getDummyOtInstance().send();
    return ctx;
  }

  private List<Pair<StrictBitVector, StrictBitVector>> bristolOtSend(
      OtExtensionTestContext ctx, int iterations, int batchSize)
      throws IOException {
    Ot otSender = new BristolOt(ctx.createResources(1), ctx.getNetwork(),
        batchSize);
    List<Pair<StrictBitVector, StrictBitVector>> messages = new ArrayList<>(
        iterations);
    Drbg rand = ctx.createRand(1);
    byte[] msgBytes = new byte[messageLength / 8];
    for (int i = 0; i < iterations; i++) {
      rand.nextBytes(msgBytes);
      StrictBitVector msgZero = new StrictBitVector(msgBytes, messageLength);
      rand.nextBytes(msgBytes);
      StrictBitVector msgOne = new StrictBitVector(msgBytes, messageLength);
      otSender.send(msgZero, msgOne);
      Pair<StrictBitVector, StrictBitVector> currentPair =
          new Pair<StrictBitVector, StrictBitVector>(msgZero, msgOne);
      messages.add(currentPair);
    }
    ((Closeable) ctx.getNetwork()).close();
    return messages;
  }

  private List<StrictBitVector> bristolOtReceive(OtExtensionTestContext ctx,
      StrictBitVector choices, int batchSize) throws IOException {
    Ot otReceiver = new BristolOt(ctx.createResources(1), ctx.getNetwork(),
        batchSize);
    List<StrictBitVector> messages = new ArrayList<>(choices.getSize());
    for (int i = 0; i < choices.getSize(); i++) {
      StrictBitVector message = otReceiver.receive(choices.getBit(i, false));
      messages.add(message);
    }
    ((Closeable) ctx.getNetwork()).close();
    return messages;
  }

  /**
   * Verify that we can execute the OT.
   *
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testBristolOt() throws IOException {
    // The batchsize of the underlying implementation must be a two power minus
    // kbitLength and lambdaBitLength
    int batchSize = 1024 - kbitLength - lambdaSecurityParam;
    // We execute more OTs than the batchSize to ensure that an automatic
    // extension will take place once preprocessed OTs run out
    int iterations = 1032;
    Drbg rand = new AesCtrDrbg(Constants.seedThree);
    Callable<OtExtensionTestContext> partyOneInit = () -> bristolInitSender();
    Callable<OtExtensionTestContext> partyTwoInit = () -> bristolInitReceiver();
    // run tasks and get ordered list of results
    List<OtExtensionTestContext> initResults = testRuntime.runPerPartyTasks(
        Arrays.asList(partyOneInit, partyTwoInit));
    StrictBitVector choices = new StrictBitVector(iterations, rand);
    Callable<List<?>> partyOneOt = () -> bristolOtSend(initResults.get(0),
        iterations, batchSize);
    Callable<List<?>> partyTwoOt = () -> bristolOtReceive(initResults.get(1),
        choices, batchSize);
    // run tasks and get ordered list of results
    List<List<?>> extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
    for (int i = 0; i < iterations; i++) {
      Pair<StrictBitVector, StrictBitVector> senderResult =
          (Pair<StrictBitVector, StrictBitVector>) extendResults.get(0).get(i);
      StrictBitVector receiverResult = (StrictBitVector) extendResults.get(1)
          .get(i);
      // Verify the receiver's result
      if (choices.getBit(i, false) == false) {
        assertTrue(senderResult.getFirst().equals(receiverResult));
      } else {
        assertTrue(senderResult.getSecond().equals(receiverResult));
      }
      // Sanity checks:
      // Check the messages are not 0-strings
      StrictBitVector zeroVec = new StrictBitVector(messageLength);
      assertEquals(zeroVec.getSize(), senderResult.getFirst().getSize());
      assertEquals(zeroVec.getSize(), senderResult.getSecond().getSize());
      assertNotEquals(zeroVec, senderResult.getFirst());
      assertNotEquals(zeroVec, senderResult.getSecond());
      assertEquals(zeroVec.getSize(), receiverResult.getSize());
      assertNotEquals(zeroVec, receiverResult);
      // Check that the two messages are not the same
      assertNotEquals(senderResult.getFirst(), senderResult.getSecond());
      // Check that they are not all equal
      if (i > 0) {
        assertNotEquals(extendResults.get(0).get(i - 1),
            extendResults.get(0).get(i));
        assertNotEquals(extendResults.get(1).get(i - 1), extendResults.get(1).get(i));
      }
    }
    // Do more sanity checks
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(choices.getSize()), choices);
    // Check the length the values
    assertEquals(iterations, extendResults.get(0).size());
    assertEquals(iterations, extendResults.get(1).size());
  }

  private List<Pair<StrictBitVector, StrictBitVector>> bristolRotBatchSend(
      OtExtensionTestContext ctx, int batchSize, int messageSize, int id)
      throws IOException {
    // OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength,
    // lambdaSecurityParam);
    BristolRotBatch rotBatchSender = new BristolRotBatch(ctx.createResources(
        id), ctx.getNetwork());
    // if (autoInit == false) {
    // rotBatchSender.initSender();
    // }
    List<Pair<StrictBitVector, StrictBitVector>> messages = rotBatchSender
        .send(batchSize, messageSize);
    return messages;
  }

  private List<StrictBitVector> bristolRotBatchReceive(
      OtExtensionTestContext ctx, StrictBitVector choices, int messageSize,
      int id) throws IOException {
    // OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength,
    // lambdaSecurityParam);
    BristolRotBatch rotBatchReceiver = new BristolRotBatch(ctx.createResources(
        id), ctx.getNetwork());
    // if (autoInit == false) {
    // rotBatchReceiver.initReceiver();
    // }
    List<StrictBitVector> messages = rotBatchReceiver.receive(choices,
        messageSize);
    return messages;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBristolRot() throws IOException {
    // boolean autoInit = false;
    int extendSize = 1024;
    int messageSize = 2048;
    Callable<OtExtensionTestContext> partyOneInit = () -> bristolInitSender();
    Callable<OtExtensionTestContext> partyTwoInit = () -> bristolInitReceiver();
    // run tasks and get ordered list of results
    List<OtExtensionTestContext> initResults = testRuntime.runPerPartyTasks(
        Arrays.asList(partyOneInit, partyTwoInit));
    Callable<List<?>> partyOneExtend = () -> bristolRotBatchSend(initResults
        .get(0), extendSize, messageSize, 1);
    // Pick some random choice bits
    Drbg rand = new AesCtrDrbg(Constants.seedThree);
    StrictBitVector choices = new StrictBitVector(extendSize, rand);
    Callable<List<?>> partyTwoExtend = () -> bristolRotBatchReceive(initResults
        .get(1), choices, messageSize, 1);
    // run tasks and get ordered list of results
    List<List<?>> extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    List<Pair<StrictBitVector, StrictBitVector>> senderResults =
        (List<Pair<StrictBitVector, StrictBitVector>>) extendResults.get(0);
    List<StrictBitVector> receiverResults = (List<StrictBitVector>) extendResults
        .get(1);
    for (int i = 0; i < choices.getSize(); i++) {
      Pair<StrictBitVector, StrictBitVector> currentSenderMessages = senderResults
          .get(i);
      // Check the receiver got the right messages according to his choicebits
      if (choices.getBit(i, false) == false) {
        assertTrue(
            currentSenderMessages.getFirst().equals(receiverResults.get(i)));
      } else {
        assertTrue(
            currentSenderMessages.getSecond().equals(receiverResults.get(i)));
      }
    }
    // Do a sanity check of the values:
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(choices.getSize()), choices);
    // Check the length the values
    assertEquals(extendSize, senderResults.size());
    assertEquals(extendSize, receiverResults.size());
    StrictBitVector zeroVec = new StrictBitVector(messageSize);
    for (int i = 0; i < extendSize; i++) {
      // Check the messages are not 0-strings
      assertNotEquals(zeroVec, senderResults.get(i).getFirst());
      assertNotEquals(zeroVec, senderResults.get(i).getSecond());
      assertNotEquals(zeroVec, receiverResults.get(i));
      // Check that the two messages are not the same
      assertNotEquals(senderResults.get(i).getFirst(),
          senderResults.get(i).getSecond());
      // Check that they are not all equal
      if (i > 0) {
        assertNotEquals(senderResults.get(i - 1).getFirst(),
            senderResults.get(i).getFirst());
        assertNotEquals(senderResults.get(i - 1).getSecond(),
            senderResults.get(i).getSecond());
        assertNotEquals(receiverResults.get(i - 1), receiverResults.get(i));
      }
    }
    ((Closeable) initResults.get(0).getNetwork()).close();
    ((Closeable) initResults.get(1).getNetwork()).close();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBristolRotReuseSeeds() throws IOException {
    // // Verify that the BristolRot can do auto initialization
    // boolean autoInit = true;
    int extendSize = 1024;
    int messageSize = 2048;
    int iterations = 2;
    Callable<OtExtensionTestContext> partyOneInit = () -> bristolInitSender();
    Callable<OtExtensionTestContext> partyTwoInit = () -> bristolInitReceiver();
    // run tasks and get ordered list of results
    List<OtExtensionTestContext> initResults = testRuntime.runPerPartyTasks(
        Arrays.asList(partyOneInit, partyTwoInit));
    StrictBitVector previousChoices = null;
    List<Pair<StrictBitVector, StrictBitVector>> previousMessages = null;
    for (int i = 0; i < iterations; i++) {
      final int sessionId = i;
      Callable<List<?>> partyOneExtend = () -> bristolRotBatchSend(initResults
          .get(0), extendSize, messageSize, sessionId);
      byte[] seed = Constants.seedThree;
      // Make sure the seed used is unique for each thread
      seed[0] ^= (byte) i;
      Drbg rand = new AesCtrDrbg(seed);
      StrictBitVector choices = new StrictBitVector(extendSize, rand);
      Callable<List<?>> partyTwoExtend = () -> bristolRotBatchReceive(
          initResults.get(1), choices, messageSize, sessionId);
      // run tasks and get ordered list of results
      List<List<?>> extendResults = testRuntime.runPerPartyTasks(Arrays.asList(
          partyOneExtend, partyTwoExtend));
      List<Pair<StrictBitVector, StrictBitVector>> senderResults = (List<Pair<StrictBitVector, StrictBitVector>>) extendResults
          .get(0);
      List<StrictBitVector> receiverResults = (List<StrictBitVector>) extendResults
          .get(1);
      // Sanity check:
      // Ensure that we don't get the same values in each parallel execution
      if (previousChoices != null) {
        assertNotEquals(previousChoices, choices);
      }
      for (int j = 0; j < choices.getSize(); j++) {
        Pair<StrictBitVector, StrictBitVector> currentSenderMessages = senderResults
            .get(j);
        if (choices.getBit(j, false) == false) {
          assertTrue(currentSenderMessages.getFirst().equals(receiverResults
              .get(j)));
        } else {
          assertTrue(currentSenderMessages.getSecond().equals(receiverResults
              .get(j)));
        }
        // Ensure that we don't get the same messages in each parallel execution
        if (previousMessages != null) {
          assertNotEquals(previousMessages.get(j), currentSenderMessages);
          // The following check is needed to ensure that the senders messages are actually randomized and
          // that not only the receiver's choices are randomized, as the receiver's choices affect the order
          // of each pair of messages
          assertNotEquals(previousMessages.get(j).getFirst(),
              currentSenderMessages.getSecond());
        }
      }
      // Do a sanity check of the values
      // Check that choices are not the 0-string
      assertNotEquals(new StrictBitVector(choices.getSize()), choices);
      // Check the length the values
      assertEquals(extendSize, senderResults.size());
      assertEquals(extendSize, receiverResults.size());
      StrictBitVector zeroVec = new StrictBitVector(messageSize);
      for (int j = 0; j < extendSize; j++) {
        // Check the messages are not 0-strings
        assertNotEquals(zeroVec, senderResults.get(j).getFirst());
        assertNotEquals(zeroVec, senderResults.get(j).getSecond());
        assertNotEquals(zeroVec, receiverResults.get(j));
        // Check that the two messages are not the same
        assertNotEquals(senderResults.get(j).getFirst(), senderResults.get(j)
            .getSecond());
        // Check that they are not all equal
        if (j > 0) {
          assertNotEquals(senderResults.get(j - 1).getFirst(), senderResults
              .get(j).getFirst());
          assertNotEquals(senderResults.get(j - 1).getSecond(), senderResults
              .get(j).getSecond());
          assertNotEquals(receiverResults.get(j - 1), receiverResults.get(j));
        }
      }
      previousChoices = choices;
      previousMessages = senderResults;
    }
    ((Closeable) initResults.get(0).getNetwork()).close();
    ((Closeable) initResults.get(1).getNetwork()).close();
  }

  private Exception bristolOtMaliciousSend(
      OtExtensionTestContext ctx, int iterations, int batchSize)
      throws IOException {
    BristolOt otSender = new BristolOt(ctx.createResources(1), ctx.getNetwork(),
        batchSize);
    Drbg rand = ctx.createRand(1);
    byte[] msgBytes = new byte[messageLength / 8];
    rand.nextBytes(msgBytes);
    StrictBitVector msgZero = new StrictBitVector(msgBytes, messageLength);
    rand.nextBytes(msgBytes);
    StrictBitVector msgOne = new StrictBitVector(msgBytes, messageLength);
    otSender.send(msgZero, msgOne);
    ((Closeable) ctx.getNetwork()).close();
    return null;
  }

  private Exception bristolOtMaliciousReceive(
      OtExtensionTestContext ctx, StrictBitVector choices, int batchSize)
      throws IOException, NoSuchMethodException, SecurityException,
      IllegalArgumentException, IllegalAccessException,
      InvocationTargetException, NoSuchFieldException {
    BristolOt otReceiver = new BristolOt(ctx.createResources(1), ctx
        .getNetwork(), batchSize);
    otReceiver.receive(choices.getBit(0, false));
    Field receiver = BristolOt.class.getDeclaredField("receiver");
    receiver.setAccessible(true);
    Method method = receiver.get(otReceiver).getClass().getDeclaredMethod(
        "doActualReceive", byte[].class, byte[].class);
    method.setAccessible(true);
    Exception exception = null;
    try {
      method.invoke(receiver.get(otReceiver), new byte[] { 0x42 }, new byte[] {
        0x42, 0x43 });
    } catch (Exception e) {
      exception = e;
    } finally {
      ((Closeable) ctx.getNetwork()).close();
    }
    return exception;
  }

  @Test
  public void testMaliciousException()
      throws NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException, NoSuchFieldException {
    // Network network = new Network() {
    // @Override
    // public void send(int partyId, byte[] data) {
    // }
    //
    // @Override
    // public byte[] receive(int partyId) {
    // return null;
    // }
    //
    // @Override
    // public int getNoOfParties() {
    // return 0;
    // }
    // };
    // Drbg rand = new AesCtrDrbg(Constants.seedOne);
    // BristolSeedOts seedOts = new BristolSeedOts(rand, kbitLength, new DummyOt(2,
    // network));
    // BristolOt ot = new BristolOt(new OtExtensionResourcePoolImpl(1, 2,
    // kbitLength, lambdaSecurityParam, rand), network, seedOts, 1024, 1);
    // Field receiver = BristolOt.class.getDeclaredField("receiver");
    // receiver.setAccessible(true);
    // Method method = receiver.get(ot).getClass().getDeclaredMethod(
    // "doActualReceive", byte[].class, byte[].class);
    // method.setAccessible(true);

    // Rot rot = new Rot(new OtExtensionResourcePoolImpl(1, 2, kbitLength,
    // lambdaSecurityParam, rand), network, seedOts, 1);
    // RotReceiver rotReceiver = rot.getReceiver();
    // BristolOtReceiver botRec = new BristolOtReceiver(rotReceiver, 1024);
    int batchSize = 1024 - kbitLength - lambdaSecurityParam;
    Callable<OtExtensionTestContext> partyOneInit = () -> bristolInitSender();
    Callable<OtExtensionTestContext> partyTwoInit = () -> bristolInitReceiver();
    // run tasks and get ordered list of results
    List<OtExtensionTestContext> initResults = testRuntime.runPerPartyTasks(
        Arrays.asList(partyOneInit, partyTwoInit));
    StrictBitVector choices = new StrictBitVector(8);
    Callable<Exception> partyOneOt = () -> bristolOtMaliciousSend(initResults
        .get(0), 8, batchSize);
    Callable<Exception> partyTwoOt = () -> bristolOtMaliciousReceive(initResults
        .get(1), choices, batchSize);
    // run tasks and get ordered list of results
    List<Exception> extendResults = testRuntime.runPerPartyTasks(Arrays.asList(
        partyOneOt, partyTwoOt));
    assertEquals(MaliciousException.class, extendResults.get(1).getCause()
        .getClass());
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
