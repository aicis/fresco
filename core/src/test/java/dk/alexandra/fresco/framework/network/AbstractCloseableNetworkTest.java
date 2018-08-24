package dk.alexandra.fresco.framework.network;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.configuration.NetworkTestUtils;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ServerSocketFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstracts tests for implementations of the {@link CloseableNetwork} interface.
 *
 */
public abstract class AbstractCloseableNetworkTest {

  protected static final int TWO_MINUTE_TIMEOUT_MILLIS = 120000;
  protected Map<Integer, CloseableNetwork> networks;

  /**
   * Should create an instance of the CloseableNetwork implementation that is being tested from a
   * given network configuration.
   *
   * @param conf a network configuration
   * @return an implementation of CloseableNetwork to be tested
   */
  protected abstract CloseableNetwork newCloseableNetwork(NetworkConfiguration conf);

  /**
   * Should create an instance of the CloseableNetwork implementation that is being tested from a
   * given network configuration and a connection timeout duration.
   *
   * @param conf a network configuration
   * @param timeout a duration in which to wait before timing out waiting for the network to be
   *        connected.
   * @return an implementation of CloseableNetwork to be tested
   */
  protected abstract CloseableNetwork newCloseableNetwork(NetworkConfiguration conf,
      Duration timeout);

  @Before
  public void setup() {
    networks = new HashMap<>();
  }

  @After
  public void tearDown() {
    closeNetworks(networks);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testConnectOneParty() {
    networks = createNetworks(1);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testConnectTwoParties() {
    networks = createNetworks(2);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testConnectMultipleParties() {
    networks = createNetworks(7);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testClose() {
    networks = createNetworks(3);
    closeNetworks(networks);
  }

  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testNegativeTimeout() {
    List<NetworkConfiguration> conf = getNetConfs(2);
    newCloseableNetwork(conf.get(1), Duration.ofMillis(-10));
  }

  /**
   * In this test each party sends a message of a given size to the next party, with the last party
   * sending to the first party.
   *
   * @param numParties the number of parties
   * @param messageSize the size of the message
   */
  private void circularSendSingleMessage(int numParties, int messageSize) {
    networks = createNetworks(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<?>> fs = new ArrayList<>(numParties);
    for (int i = 1; i < numParties + 1; i++) {
      final int id = i;
      Future<?> f = es.submit(() -> {
        byte[] data = new byte[messageSize];
        byte[] expectedData = new byte[messageSize];
        Arrays.fill(data, (byte) id);
        Arrays.fill(expectedData, (byte) prevParty(id, numParties));
        networks.get(id).send(nextParty(id, numParties), data);
        byte[] receivedData = networks.get(id).receive(prevParty(id, numParties));
        assertArrayEquals(expectedData, receivedData);
      });
      fs.add(f);
    }
    for (Future<?> future : fs) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        fail("Test should not throw exception");
      }
    }
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSelfSendOneParty() {
    circularSendSingleMessage(1, 1024);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSelfSendThreeParties() {
    networks = createNetworks(3);
    networks.keySet().stream().forEach(i -> networks.get(i).send(i, new byte[] { 0x01 }));
    networks.keySet().stream().forEach(i -> networks.get(i).receive(i));
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS, expected = RuntimeException.class)
  public void testSendAfterClose() {
    networks = createNetworks(3);
    closeNetworks(networks);
    networks.get(1).send(2, new byte[] {});
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS, expected = RuntimeException.class)
  public void testReceiveAfterClose() {
    networks = createNetworks(3);
    closeNetworks(networks);
    networks.get(1).receive(2);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendEmpty() {
    circularSendSingleMessage(2, 0);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendByte() {
    circularSendSingleMessage(2, 1);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendHugeAmount() {
    circularSendSingleMessage(2, 104857600);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendManyParties() {
    circularSendSingleMessage(5, 1024);
  }

  /**
   * This test sends multiple messages from all parties to a single receiver.
   *
   * @param numParties the number of parties
   * @param numMessages the number of messages
   * @throws ExecutionException
   * @throws InterruptedException
   */
  private void sendMultipleToSingleReceiver(int numParties, int numMessages)
      throws InterruptedException, ExecutionException {
    Map<Integer, CloseableNetwork> networks = createNetworks(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<?>> fs = new ArrayList<>(numParties);
    for (int i = 1; i < numParties + 1; i++) {
      final int id = i;
      Future<?> f = es.submit(() -> {
        if (id == numParties) {
          for (int k = 1; k < numParties; k++) {
            Random r = new Random(k);
            final byte[] data = new byte[1024];
            byte[] receivedData;
            for (int j = 0; j < numMessages; j++) {
              receivedData = networks.get(id).receive(k);
              r.nextBytes(data);
              assertArrayEquals(data, receivedData);
            }
          }
        } else {
          Random r = new Random(id);
          final byte[] data = new byte[1024];
          for (int j = 0; j < numMessages; j++) {
            r.nextBytes(data);
            networks.get(id).send(numParties, data.clone());
          }
        }
        try {
          networks.get(id).close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      fs.add(f);
    }
    for (Future<?> future : fs) {
      future.get();
    }
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testManyPartiesSendToOneReceiver() throws InterruptedException, ExecutionException {
    sendMultipleToSingleReceiver(5, 100);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendManyMessagesToOneReceiver() throws InterruptedException, ExecutionException {
    sendMultipleToSingleReceiver(2, 10000);
  }

  @Test(expected = IllegalArgumentException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendToNegativePartyId() {
    networks = createNetworks(1);
    networks.get(1).send(-1, new byte[] { 0x01 });
  }

  @Test(expected = IllegalArgumentException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendToTooLargePartyId() {
    networks = createNetworks(1);
    networks.get(1).send(2, new byte[] { 0x01 });
  }

  @Test(expected = IllegalArgumentException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testReceiveFromNegativePartyId() {
    networks = createNetworks(1);
    networks.get(1).receive(-1);
  }

  @Test(expected = IllegalArgumentException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testReceiveFromTooLargePartyId() {
    networks = createNetworks(1);
    networks.get(1).receive(2);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testPartiesReconnect() {
    testMultiplePartiesReconnect(2, 10);
    testMultiplePartiesReconnect(5, 20);
  }

  private void testMultiplePartiesReconnect(int numParties, int noOfRetries) {
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    for (int i = 0; i < noOfRetries; i++) {
      networks = createNetworks(confs);
      closeNetworks(networks);
    }
  }

  protected Map<Integer, CloseableNetwork> createNetworks(int numParties) {
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    return createNetworks(confs);
  }

  protected Map<Integer, CloseableNetwork> createNetworks(List<NetworkConfiguration> confs) {
    int numParties = confs.get(0).noOfParties();
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    Map<Integer, CloseableNetwork> netMap = new HashMap<>(numParties);
    Map<Integer, Future<CloseableNetwork>> futureMap = new HashMap<>(numParties);
    try {
      for (NetworkConfiguration conf : confs) {
        Future<CloseableNetwork> f = es.submit(() -> newCloseableNetwork(conf));
        futureMap.put(conf.getMyId(), f);
      }
      for (Entry<Integer, Future<CloseableNetwork>> entry : futureMap.entrySet()) {
        netMap.put(entry.getKey(), entry.getValue().get());
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      fail("Failed to setup networks.");
    } finally {
      es.shutdownNow();
    }
    return netMap;
  }

  protected List<NetworkConfiguration> getNetConfs(int numParties) {
    Map<Integer, Party> parties = new HashMap<>(numParties);
    List<NetworkConfiguration> confs = new ArrayList<>(numParties);
    List<Integer> ports = NetworkTestUtils.getFreePorts(numParties);
    int id = 1;
    for (Integer port : ports) {
      parties.put(id, new Party(id, "localhost", port));
      id++;
    }
    for (int i = 1; i <= numParties; i++) {
      confs.add(new NetworkConfigurationImpl(i, parties));
    }
    return confs;
  }

  protected void closeNetworks(Map<Integer, CloseableNetwork> networks) {
    networks.values().stream().forEach(t -> {
      try {
        t.close();
      } catch (IOException e) {
        fail("This should never happen");
        e.printStackTrace();
      }
    });
  }

  protected int nextParty(int myId, int numParties) {
    return (myId == numParties) ? 1 : myId + 1;
  }

  protected int prevParty(int myId, int numParties) {
    return (myId == 1) ? numParties : myId - 1;
  }

  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testFailToBind() throws Throwable {
    ServerSocket socket = null;
    try {
      List<NetworkConfiguration> confs = getNetConfs(2);
      socket = ServerSocketFactory.getDefault().createServerSocket(confs.get(1).getMe().getPort());
      newCloseableNetwork(confs.get(1));
    } finally {
      socket.close();
    }
  }

  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testConnectTimeout() {
    List<NetworkConfiguration> confs = getNetConfs(2);
    // This should time out waiting for a connection to a party that is not listening
    newCloseableNetwork(confs.get(0), Duration.ofMillis(10));
  }

}
