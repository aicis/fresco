package dk.alexandra.fresco.tools.ot.otextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
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
import dk.alexandra.fresco.tools.ot.base.FailedOtException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;
import dk.alexandra.fresco.tools.ot.base.Ot;

public class FunctionalTestBristolRotBatch {
  private TestRuntime testRuntime;
  private int kbitLength = 256;
  private int lambdaBitLength = 64;

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

  private List<Pair<BigInteger, BigInteger>> bristolOtSend(int iterations,
      int batchSize)
      throws MaliciousOtException, FailedOtException, IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    Random rand = new Random(42);
    Ot<BigInteger> otSender = new BristolOt<BigInteger>(1, 2, kbitLength,
        lambdaBitLength, rand, network, batchSize);
    List<Pair<BigInteger, BigInteger>> messages = new ArrayList<>(iterations);
    for (int i = 0; i < iterations; i++) {
      BigInteger msgZero = new BigInteger(1024, rand);
      BigInteger msgOne = new BigInteger(1024, rand);
      otSender.send(msgZero, msgOne);
      Pair<BigInteger, BigInteger> currentPair = new Pair<BigInteger, BigInteger>(
          msgZero, msgOne);
      messages.add(currentPair);
    }
    ((Closeable) network).close();
    return messages;
  }

  private List<BigInteger> bristolOtReceive(StrictBitVector choices,
      int batchSize)
      throws MaliciousOtException, FailedOtException, IOException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Random rand = new Random(420);
    Ot<BigInteger> otReceiver = new BristolOt<BigInteger>(2, 1, kbitLength,
        lambdaBitLength, rand, network, batchSize);
    List<BigInteger> messages = new ArrayList<>(choices.getSize());
    for (int i = 0; i < choices.getSize(); i++) {
      BigInteger message = otReceiver.receive(choices.getBit(i, false));
      messages.add(message);
    }
    ((Closeable) network).close();
    return messages;
  }

  /**
   * Verify that we can initialize the parties in OT.
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
      Pair<BigInteger, BigInteger> senderResult = (Pair<BigInteger, BigInteger>) extendResults
          .get(0).get(i);
      BigInteger receiverResult = (BigInteger) extendResults.get(1).get(i);
      if (choices.getBit(i, false) == false) {
        assertTrue(senderResult.getFirst().equals(receiverResult));
      } else {
        assertTrue(senderResult.getSecond().equals(receiverResult));
      }
      // Check the messages are not 0-strings
      BigInteger zeroInt = new BigInteger("0");
      assertNotEquals(zeroInt, senderResult.getFirst());
      assertNotEquals(zeroInt, senderResult.getSecond());
      assertNotEquals(zeroInt, receiverResult);
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
      int batchSize, int messageSize)
      throws MaliciousOtException, FailedOtException, IOException {
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
      int messageSize)
      throws MaliciousOtException, FailedOtException, IOException {
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
}
