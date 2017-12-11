package dk.alexandra.fresco.tools.ot.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.crypto.spec.DHParameterSpec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.otextension.CheatingNetwork;
import dk.alexandra.fresco.tools.ot.otextension.TestRuntime;

public class FunctionalTestNaorPinkas {
  private TestRuntime testRuntime;
  private int messageLength = 1024;
  private DHParameterSpec params;

  /**
   * Initializes the test runtime and constructs a Cote Sender and a Cote
   * Receiver.
   */
  @Before
  public void initializeRuntime() {
    this.testRuntime = new TestRuntime();
    params = new DHParameterSpec(TestNaorPinkasOt.DhPvalue,
        TestNaorPinkasOt.DhGvalue);
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

  private List<Pair<BigInteger, BigInteger>> otSend(int iterations)
      throws IOException, NoSuchAlgorithmException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    try {
      Random rand = new Random(42);
      Ot<BigInteger> otSender = new NaorPinkasOT<BigInteger>(1, 2, rand, network,
          params);
      List<Pair<BigInteger, BigInteger>> messages = new ArrayList<>(iterations);
      for (int i = 0; i < iterations; i++) {
        BigInteger msgZero = new BigInteger(messageLength, rand);
        BigInteger msgOne = new BigInteger(messageLength, rand);
        otSender.send(msgZero, msgOne);
        Pair<BigInteger, BigInteger> currentPair = new Pair<BigInteger, BigInteger>(
            msgZero, msgOne);
        messages.add(currentPair);
      }
      return messages;
    } finally {
      ((Closeable) network).close();
    }
  }

  private List<BigInteger> otReceive(StrictBitVector choices)
      throws IOException, NoSuchAlgorithmException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    try {
      Random rand = new Random(420);
      Ot<BigInteger> otReceiver = new NaorPinkasOT<BigInteger>(2, 1, rand,
          network, params);
      List<BigInteger> messages = new ArrayList<>(choices.getSize());
      for (int i = 0; i < choices.getSize(); i++) {
        BigInteger message = otReceiver.receive(choices.getBit(i, false));
        messages.add(message);
      }
      return messages;
    } finally {
      ((Closeable) network).close();
    }
  }

  /**
   * Verify that we can execute the OT.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testNaorPinkasOt() {
    // We execute more OTs than the batchSize to ensure that an automatic
    // extension will take place once preprocessed OTs run out
    int iterations = 160;
    Random rand = new Random(540);
    StrictBitVector choices = new StrictBitVector(iterations, rand);
    Callable<List<?>> partyOneOt = () -> otSend(iterations);
    Callable<List<?>> partyTwoOt = () -> otReceive(choices);
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
        assertNotEquals(extendResults.get(1).get(i - 1),
            extendResults.get(1).get(i));
      }
    }
    // Do more sanity checks
    // Check that choices are not the 0-string
    assertNotEquals(new StrictBitVector(choices.getSize()), choices);
    // Check the length the values
    assertEquals(iterations, extendResults.get(0).size());
    assertEquals(iterations, extendResults.get(1).size());
  }

  /***** NEGATIVE TESTS. *****/
  private List<BigInteger> otSendCheat()
      throws IOException, NoSuchAlgorithmException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    try {
      Random rand = new Random(42);
      Ot<BigInteger> otSender = new NaorPinkasOT<BigInteger>(1, 2, rand,
          network, params);
      BigInteger msgZero = new BigInteger(messageLength, rand);
      BigInteger msgOne = new BigInteger(messageLength, rand);
      // Send a wrong random value c, than what is actually used
      ((CheatingNetwork) network).cheatInNextMessage(0, 100);
      otSender.send(msgZero, msgOne);
      List<BigInteger> messages = new ArrayList<>(2);
      messages.add(msgZero);
      messages.add(msgOne);
      return messages;
    } finally {
      ((Closeable) network).close();
    }
  }

  private List<BigInteger> otReceiveCheat(boolean choice)
      throws IOException, NoSuchAlgorithmException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    try {
      Random rand = new Random(420);
      Ot<BigInteger> otReceiver = new NaorPinkasOT<BigInteger>(2, 1, rand,
          network, params);
      BigInteger message = otReceiver.receive(choice);

      List<BigInteger> messageList = new ArrayList<>(1);
      messageList.add(message);
      return messageList;
    } finally {
      ((Closeable) network).close();
    }
  }

  /**
   * Test that a receiver who flips a bit its message results in a malicious
   * exception being thrown. This is not meant to capture the best possible
   * cheating strategy, but more as a sanity checks that the proper checks are
   * in place.
   */
  @Test
  public void testCheatingInNaorPinkasOt() {
    boolean choice = true;
    Callable<List<BigInteger>> partyOneInit = () -> otSendCheat();
    Callable<List<BigInteger>> partyTwoInit = () -> otReceiveCheat(choice);
    // run tasks and get ordered list of results
    List<List<BigInteger>> results = testRuntime
        .runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
    assertTrue(results.get(0) instanceof List<?>);
    assertTrue(results.get(1) instanceof Exception);
    // TODO Finish once serialization has been moved to direct bytes and not
  }
}
