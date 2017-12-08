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
  private NaorPinkasOT sender;
  private NaorPinkasOT receiver;
  private int kbitLength = 256;
  private int lambdaBitLength = 64;
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

  private List<Pair<BigInteger, BigInteger>> otSend(int iterations,
      int batchSize) throws MaliciousOtException, FailedOtException,
      IOException, NoSuchAlgorithmException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(1, Arrays.asList(1, 2)));
    Random rand = new Random(42);
    Ot<BigInteger> otSender = new NaorPinkasOT<BigInteger>(1, 2, rand, network);
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

  private List<BigInteger> otReceive(StrictBitVector choices, int batchSize)
      throws MaliciousOtException, FailedOtException, IOException,
      NoSuchAlgorithmException {
    Network network = new CheatingNetwork(
        TestRuntime.defaultNetworkConfiguration(2, Arrays.asList(1, 2)));
    Random rand = new Random(420);
    Ot<BigInteger> otReceiver = new NaorPinkasOT<BigInteger>(2, 1, rand,
        network);
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
    int iterations = 5;
    Random rand = new Random(540);
    StrictBitVector choices = new StrictBitVector(iterations, rand);
    Callable<List<?>> partyOneOt = () -> otSend(iterations, batchSize);
    Callable<List<?>> partyTwoOt = () -> otReceive(choices, batchSize);
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
}
