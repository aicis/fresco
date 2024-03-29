package dk.alexandra.fresco.tools.ot.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
import dk.alexandra.fresco.tools.ot.otextension.CheatingNetworkDecorator;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import javax.crypto.spec.DHParameterSpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for AbstractNaorPinkasOT and AbstractChouOrlandiOT
 */
@RunWith(Parameterized.class)
public class TestFunctionalOT {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {BigIntNaorPinkas.class},
            {ECNaorPinkasOt.class},
            {ECChouOrlandi.class},
            {BigIntChouOrlandi.class}
    });
  }

  private RuntimeForTests testRuntime;
  private int messageLength = 1024;
  private DHParameterSpec staticParams;
  private Class testClass;

  public TestFunctionalOT(Class testClass) {
    this.testClass = testClass;
  }


  /**
   * Initializes the test runtime and constructs loads pre-constructed Diffie-Hellman parameters.
   */
  @Before
  public void initializeRuntime() throws Exception {
    this.testRuntime = new RuntimeForTests();
    staticParams = DhParameters.getStaticDhParams();
  }

  /**
   * Shuts down the test runtime.
   */
  @After
  public void shutdown() {
    testRuntime.shutdown();
  }

  private List<Pair<StrictBitVector, StrictBitVector>> otSend(int iterations) throws Exception {
    Network network =
        new CheatingNetworkDecorator(
            new SocketNetwork(RuntimeForTests.defaultNetworkConfiguration(1, Arrays.asList(1, 2))));
    try {
      Drbg rand = new AesCtrDrbg(HelperForTests.seedOne);
      Class clazz = this.testClass;
      Constructor[] constructors = clazz.getConstructors();
      Ot otSender = (Ot) constructors[0]
          .newInstance(2, rand, network);
      List<Pair<StrictBitVector, StrictBitVector>> messages = new ArrayList<>(iterations);
      for (int i = 0; i < iterations; i++) {
        StrictBitVector msgZero = new StrictBitVector(messageLength, rand);
        StrictBitVector msgOne = new StrictBitVector(messageLength, rand);
        otSender.send(msgZero, msgOne);
        Pair<StrictBitVector, StrictBitVector> currentPair = new Pair<>(msgZero, msgOne);
        messages.add(currentPair);
      }
      return messages;
    } finally {
      ((Closeable) network).close();
    }
  }

  private List<StrictBitVector> otReceive(StrictBitVector choices) throws Exception {
    Network network =
        new CheatingNetworkDecorator(
            new SocketNetwork(RuntimeForTests.defaultNetworkConfiguration(2, Arrays.asList(1, 2))));
    try {
      Drbg rand = new AesCtrDrbg(HelperForTests.seedTwo);
      Class clazz = this.testClass;
      Constructor[] constructors = clazz.getConstructors();
      Ot otReceiver = (Ot) constructors[0]
          .newInstance(1, rand, network);
      List<StrictBitVector> messages = new ArrayList<>(choices.getSize());
      for (int i = 0; i < choices.getSize(); i++) {
        StrictBitVector message = otReceiver.receive(choices.getBit(i, false));
        messages.add(message);
      }
      return messages;
    } finally {
      ((Closeable) network).close();
    }
  }

  /**
   * Verify that we can execute the OT.
   *
   * @throws Exception Thrown if the Diffie-Hellman parameter size field could not be changed
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testOt() throws Exception {
    // We execute 24 OTs
    int iterations = 24;
    Drbg rand = new AesCtrDrbg(HelperForTests.seedThree);
    StrictBitVector choices = new StrictBitVector(iterations, rand);
    Callable<List<?>> partyOneOt = () -> otSend(iterations);
    Callable<List<?>> partyTwoOt = () -> otReceive(choices);
    // run tasks and get ordered list of results
    List<List<?>> extendResults =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
    for (int i = 0; i < iterations; i++) {
      Pair<StrictBitVector, StrictBitVector> senderResult =
          (Pair<StrictBitVector, StrictBitVector>) extendResults.get(0).get(i);
      StrictBitVector receiverResult = (StrictBitVector) extendResults.get(1).get(i);
      if (choices.getBit(i, false) == false) {
        // Check that the 0 message is the one the receiver got if his choicebit
        // was 0
        assertTrue(senderResult.getFirst().equals(receiverResult));
      } else {
        // Check that the 1 message is the one the receiver got if his choicebit
        // was 1
        assertTrue(senderResult.getSecond().equals(receiverResult));
      }
      // Do sanity checks:
      // Check the messages are not 0 vectors
      StrictBitVector zeroVec = new StrictBitVector(messageLength);
      assertEquals(zeroVec.getSize(), senderResult.getFirst().getSize());
      assertEquals(zeroVec.getSize(), senderResult.getSecond().getSize());
      assertNotEquals(zeroVec, senderResult.getFirst());
      assertNotEquals(zeroVec, senderResult.getSecond());
      assertEquals(zeroVec.getSize(), receiverResult.getSize());
      assertNotEquals(zeroVec, receiverResult);
      // Check that the sender's two messages are not the same
      assertNotEquals(senderResult.getFirst(), senderResult.getSecond());
      // Check that all messages are not all equal
      if (i > 0) {
        assertNotEquals(extendResults.get(0).get(i - 1), extendResults.get(0).get(i));
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

  /***** NEGATIVE TESTS. *****/
  private List<StrictBitVector> otSendCheat() throws IOException {
    Network network =
        new CheatingNetworkDecorator(
            new SocketNetwork(RuntimeForTests.defaultNetworkConfiguration(1, Arrays.asList(1, 2))));
    try {
      Drbg rand = new AesCtrDrbg(HelperForTests.seedOne);
      Class clazz = this.testClass;
      Constructor[] constructors = clazz.getConstructors();
      Ot otSender = (Ot) constructors[0]
          .newInstance(2, rand, network);
      StrictBitVector msgZero = new StrictBitVector(messageLength, rand);
      StrictBitVector msgOne = new StrictBitVector(messageLength, rand);
      // Send a wrong random value c, than what is actually used
      // byte on position 0 in encoded EcPoint only specifies Compression
      ((CheatingNetworkDecorator) network).cheatInNextSendMessage(0, 1);
      otSender.send(msgZero, msgOne);
      List<StrictBitVector> messages = new ArrayList<>(2);
      messages.add(msgZero);
      messages.add(msgOne);
      return messages;
    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
      e.printStackTrace();
    } finally {
      ((Closeable) network).close();
    }
    return null;
  }

  private List<StrictBitVector> otReceiveCheat(boolean choice) throws IOException {
    Network network =
        new CheatingNetworkDecorator(
            new SocketNetwork(RuntimeForTests.defaultNetworkConfiguration(2, Arrays.asList(1, 2))));
    try {
      Drbg rand = new AesCtrDrbg(HelperForTests.seedTwo);
      Class clazz = this.testClass;
      Constructor[] constructors = clazz.getConstructors();
      Ot otReceiver = (Ot) constructors[0]
          .newInstance(1, rand, network);
      StrictBitVector message = otReceiver.receive(choice);
      List<StrictBitVector> messageList = new ArrayList<>(1);
      messageList.add(message);
      return messageList;
    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
      e.printStackTrace();
    } finally {
      ((Closeable) network).close();
    }
    return null;
  }

  /**
   * Test that a sender who flips a bit in its random choice results in different messages being
   * sent. This is not meant to capture the best possible cheating strategy, but more as a sanity
   * checks that the proper checks are in place.
   */
  @Test
  public void testCheatingInOt() {
    boolean choice = true;
    Callable<List<StrictBitVector>> partyOneInit = () -> otSendCheat();
    Callable<List<StrictBitVector>> partyTwoInit = () -> otReceiveCheat(choice);
    // run tasks and get ordered list of results
    if (this.testClass == ECNaorPinkasOt.class
        ||  this.testClass == ECChouOrlandi.class) {
      // if you (trivially) cheat in ECCNaorPinkas | BouncyCastleNaorPinkas, the cheated Message is not
      // a valid point on the curve anymore, so it should throw an exception
      try {
        List<List<StrictBitVector>> results =
            testRuntime.runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
        List<StrictBitVector> senderResults = results.get(0);
        fail();
      } catch (RuntimeException e) {

      }
    } else {
      List<List<StrictBitVector>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
       List<StrictBitVector> senderResults = results.get(0);
      StrictBitVector receiverResult = results.get(1).get(0);
      // Verify that the Both messages of the sender is different from the message
      // the receiver got
      assertNotEquals(senderResults.get(0), receiverResult);
      assertNotEquals(senderResults.get(1), receiverResult);
    }

  }
}
