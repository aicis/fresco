package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import dk.alexandra.fresco.tools.ot.base.Ot;

public class FunctionalTestBristolOt {
  private TestRuntime testRuntime;
  private int kbitLength = 256;
  private int lambdaBitLength = 64;
  private int messageLength = 1024;

  /**
   * Initializes the test runtime and constructs a Cote Sender and a Cote
   * Receiver.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new TestRuntime();
  }

  /**
   * Shuts down the network and test runtime.
   * 
   * @throws IOException
   *           Thrown if the network fails to shut down
   */
  @After
  public void shutdown() throws IOException {
    testRuntime.shutdown();
  }

  private List<Pair<StrictBitVector, StrictBitVector>> bristolOtSend(
      int iterations,
      int batchSize) throws IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    Random rand = new Random(42);
    Ot otSender = new BristolOt(1, 2, kbitLength, lambdaBitLength, rand,
        network, batchSize);
    List<Pair<StrictBitVector, StrictBitVector>> messages = new ArrayList<>(
        iterations);
    for (int i = 0; i < iterations; i++) {
      StrictBitVector msgZero = new StrictBitVector(messageLength, rand);
      StrictBitVector msgOne = new StrictBitVector(messageLength, rand);
      otSender.send(msgZero, msgOne);
      Pair<StrictBitVector, StrictBitVector> currentPair = new Pair<StrictBitVector, StrictBitVector>(
          msgZero, msgOne);
      messages.add(currentPair);
    }
    ((Closeable) network).close();
    return messages;
  }

  private List<StrictBitVector> bristolOtReceive(StrictBitVector choices,
      int batchSize) throws IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Random rand = new Random(420);
    Ot otReceiver = new BristolOt(2, 1, kbitLength,
        lambdaBitLength, rand, network, batchSize);
    List<StrictBitVector> messages = new ArrayList<>(choices.getSize());
    for (int i = 0; i < choices.getSize(); i++) {
      StrictBitVector message = otReceiver.receive(choices.getBit(i, false));
      messages.add(message);
    }
    ((Closeable) network).close();
    return messages;
  }

  /**
   * Verify that we can execute the OT.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testBristolOt() {
    int batchSize = 1024 - kbitLength - lambdaBitLength;
    // We execute more OTs than the batchSize to ensure that an automatic
    // extension will take place once preprocessed OTs run out
    int iterations = 904;
    Random rand = new Random(540);
    StrictBitVector choices = new StrictBitVector(iterations, rand);
    Callable<List<?>> partyOneOt = () -> bristolOtSend(iterations, batchSize);
    Callable<List<?>> partyTwoOt = () -> bristolOtReceive(choices, batchSize);
    // run tasks and get ordered list of results
    List<List<?>> extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
    for (int i = 0; i < iterations; i++) {
      Pair<StrictBitVector, StrictBitVector> senderResult = (Pair<StrictBitVector, StrictBitVector>) extendResults
          .get(0).get(i);
      StrictBitVector receiverResult = (StrictBitVector) extendResults.get(1)
          .get(i);
      if (choices.getBit(i, false) == false) {
        assertTrue(senderResult.getFirst().equals(receiverResult));
      } else {
        assertTrue(senderResult.getSecond().equals(receiverResult));
      }
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
      int batchSize, int messageSize) throws IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    Random rand = new Random(42);
    BristolRotBatch rotBatchSender = new BristolRotBatch(1, 2, kbitLength,
        lambdaBitLength, rand, network);
    List<Pair<StrictBitVector, StrictBitVector>> messages = rotBatchSender
        .send(batchSize, messageSize);
    ((Closeable) network).close();
    return messages;
  }

  private List<StrictBitVector> bristolRotBatchReceive(StrictBitVector choices,
      int messageSize) throws IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Random rand = new Random(420);
    BristolRotBatch rotBatchReceiver = new BristolRotBatch(2, 1, kbitLength,
        lambdaBitLength, rand, network);
    List<StrictBitVector> messages = rotBatchReceiver.receive(choices,
        messageSize);
    ((Closeable) network).close();
    return messages;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBristolRot() {
    int extendSize = 1024;
    int messageSize = 2048;
    Callable<List<?>> partyOneExtend = () -> bristolRotBatchSend(extendSize,
        messageSize);
    StrictBitVector choices = new StrictBitVector(extendSize, new Random(540));
    Callable<List<?>> partyTwoExtend = () -> bristolRotBatchReceive(choices,
        messageSize);
    // run tasks and get ordered list of results
    List<List<?>> extendResults = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneExtend, partyTwoExtend));
    List<Pair<StrictBitVector, StrictBitVector>> senderResults = (List<Pair<StrictBitVector, StrictBitVector>>) extendResults
        .get(0);
    List<StrictBitVector> receiverResults = (List<StrictBitVector>) extendResults
        .get(1);
    for (int i = 0; i < choices.getSize(); i++) {
      Pair<StrictBitVector, StrictBitVector> currentSenderMessages = senderResults
          .get(i);
      if (choices.getBit(i, false) == false) {
        assertTrue(
            currentSenderMessages.getFirst().equals(receiverResults.get(i)));
      } else {
        assertTrue(
            currentSenderMessages.getSecond().equals(receiverResults.get(i)));
      }
    }
    // Do a sanity check of the values
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

  @Test
  public void testNoSuchAlgorithmBatch()
      throws IllegalAccessException,
      IllegalArgumentException, NoSuchMethodException, SecurityException,
      NoSuchFieldException {
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
    BristolRotBatch ot = new BristolRotBatch(1, 2, kbitLength, lambdaBitLength,
        new Random(42), network);
    Field algorithm = ot.getClass().getDeclaredField("prgAlgorithm");
    // Remove private
    algorithm.setAccessible(true);
    // Test receiver
    algorithm.set(ot, "something");
    Method method = ot.getClass().getDeclaredMethod(
        "computeRandomMessage", StrictBitVector.class, int.class);
    method.setAccessible(true);
    boolean thrown = false;
    try {
      method.invoke(ot, new StrictBitVector(8), 8);
    } catch (InvocationTargetException e) {
      assertEquals(
          "Something, non-malicious, went wrong during the sending/receiving of "
              + "the Bristol random OT extension.",
          e.getTargetException().getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testNoSuchAlgorithm()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException,
      IllegalAccessException, NoSuchMethodException {
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
    BristolOt ot = new BristolOt(1, 2, kbitLength, lambdaBitLength,
        new Random(42), network, 1024);
    Field algorithm = BristolOtShared.class.getDeclaredField("prgAlgorithm");
    // Remove private
    algorithm.setAccessible(true);
    // Test receiver
    algorithm.set(ot.receiver, "something");
    Method method = ot.receiver.getClass().getDeclaredMethod("adjustMessage",
        byte[].class);
    method.setAccessible(true);
    boolean thrown = false;
    try {
      method.invoke(ot.receiver, new byte[] { 0x42 });
    } catch (InvocationTargetException e) {
      assertEquals(
          "Something, non-malicious, went wrong when receiving a Bristol OT.",
          e.getTargetException().getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testMaliciousException()
      throws NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException {
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
    BristolOt ot = new BristolOt(1, 2, kbitLength, lambdaBitLength,
        new Random(42), network, 1024);
    Method method = ot.receiver.getClass().getDeclaredMethod("doActualReceive",
        byte[].class, byte[].class);
    method.setAccessible(true);
    boolean thrown = false;
    try {
      method.invoke(ot.receiver, new byte[] { 0x42 },
          new byte[] { 0x42, 0x43 });
    } catch (InvocationTargetException e) {
      assertEquals(
          "Sender gave adjustment messages of different length.",
          e.getTargetException().getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }
}
