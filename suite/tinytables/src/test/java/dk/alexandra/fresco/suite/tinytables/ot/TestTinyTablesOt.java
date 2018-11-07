package dk.alexandra.fresco.suite.tinytables.ot;

import static org.junit.Assert.assertEquals;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.crypto.spec.DHParameterSpec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
import dk.alexandra.fresco.tools.ot.base.DhParameters;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class TestTinyTablesOt {
  private RuntimeForTests testRuntime;
  private DHParameterSpec staticParams;
  private Drbg random;
  private StrictBitVector messageZero;
  private StrictBitVector messageOne;

  /**
   * Initializes the test runtime and constructs loads preconstructed Diffe-Hellman parameters.
   */
  @Before
  public void initializeRuntime() throws Exception {
    this.testRuntime = new RuntimeForTests();
    this.staticParams = DhParameters.getStaticDhParams();
    this.random = new AesCtrDrbg(HelperForTests.seedOne);
    this.messageZero = new StrictBitVector(new byte[] { (byte) 0x42, (byte) 0x42 });
    this.messageOne = new StrictBitVector(new byte[] { (byte) 0x13, (byte) 0x37 });
  }

  /**
   * Shuts down the test runtime.
   */
  @After
  public void shutdown() throws IOException {
    testRuntime.shutdown();
  }

  private StrictBitVector npSend() throws IOException {
    Network network = new AsyncNetwork(RuntimeForTests.defaultNetworkConfiguration(1, Arrays.asList(
        1, 2)));
    try {
      TinyTablesOt ot = new TinyTablesNaorPinkasOt(2, random, staticParams);
      ot.init(network);
      ot.send(messageZero, messageOne);
      return null;
    } finally {
      ((Closeable) network).close();
    }
  }

  private StrictBitVector npReceive(boolean choiceBit) throws IOException {
    Network network = new AsyncNetwork(RuntimeForTests.defaultNetworkConfiguration(2, Arrays.asList(
        1, 2)));
    try {
      TinyTablesOt ot = new TinyTablesNaorPinkasOt(1, random, staticParams);
      ot.init(network);
      StrictBitVector receivedMsg = ot.receive(choiceBit);
      return receivedMsg;
    } finally {
      ((Closeable) network).close();
    }
  }

  @Test
  public void testTinyTablesNaorPinkasOt() {
    Callable<StrictBitVector> partyOneOt = () -> npSend();
    Callable<StrictBitVector> partyTwoOt = () -> npReceive(false);
    List<StrictBitVector> res = testRuntime.runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
    assertEquals(messageZero, res.get(1));
  }

  private StrictBitVector dummySend() throws IOException {
    Network network = new AsyncNetwork(RuntimeForTests.defaultNetworkConfiguration(1, Arrays.asList(
        1, 2)));
    try {
      TinyTablesOt ot = new TinyTablesNaorPinkasOt(2, random, staticParams);
      ot.init(network);
      ot.send(messageZero, messageOne);
      return null;
    } finally {
      ((Closeable) network).close();
    }
  }

  private StrictBitVector dummyReceive(boolean choiceBit) throws IOException {
    Network network = new AsyncNetwork(RuntimeForTests.defaultNetworkConfiguration(2, Arrays.asList(
        1, 2)));
    try {
      TinyTablesOt ot = new TinyTablesNaorPinkasOt(1, random, staticParams);
      ot.init(network);
      StrictBitVector receivedMsg = ot.receive(choiceBit);
      return receivedMsg;
    } finally {
      ((Closeable) network).close();
    }
  }

  @Test
  public void testTinyTablesDummyOt() {
    Callable<StrictBitVector> partyOneOt = () -> dummySend();
    Callable<StrictBitVector> partyTwoOt = () -> dummyReceive(true);
    List<StrictBitVector> res = testRuntime.runPerPartyTasks(Arrays.asList(partyOneOt, partyTwoOt));
    assertEquals(messageOne, res.get(1));
  }
}
