package dk.alexandra.fresco.framework.network.async;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.net.ServerSocketFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAsyncNetwork {

  private static final int ONE_MINUTE_TIMEOUT = 60000;
  private Map<Integer, CloseableNetwork> networks;

  @Before
  public void setup() {
    networks = new HashMap<>();
  }

  @After
  public void tearDown() {
    closeNetworks(networks);
  }

  // TEST CONNECT

  @Test
  public void testConnectOneParty() {
    networks = createNetworks(1);
  }

  @Test
  public void testConnectTwoParties() {
    networks = createNetworks(2);
  }

  @Test
  public void testConnectMultipleParties() {
    networks = createNetworks(7);
  }

  // CIRCULAR SENDS

  /**
   * In this test each party sends a message of a given size to the party next party, with the last
   * party sending to the first party.
   *
   * @param numParties the number of parties
   * @param messageSize the size of the message
   */
  private void circularSendSingleMessage(int numParties, int messageSize) {
    networks = createNetworks(numParties);
    IntStream.range(1, numParties + 1).parallel().forEach(id -> {
      byte[] data = new byte[messageSize];
      byte[] expectedData = new byte[messageSize];
      Arrays.fill(data, (byte) id);
      Arrays.fill(expectedData, (byte) prevParty(id, numParties));
      networks.get(id).send(nextParty(id, numParties), data);
      byte[] receivedData = networks.get(id).receive(prevParty(id, numParties));
      assertArrayEquals(expectedData, receivedData);
    });
  }

  @Test(timeout = ONE_MINUTE_TIMEOUT)
  public void testSelfSend() {
    circularSendSingleMessage(1, 1024);
  }

  @Test(timeout = ONE_MINUTE_TIMEOUT)
  public void testSendEmpty() {
    circularSendSingleMessage(2, 0);
  }

  @Test(timeout = ONE_MINUTE_TIMEOUT)
  public void testSendByte() {
    circularSendSingleMessage(2, 1);
  }

  // Send 100Mb through the network to stress-test it.
  @Test(timeout = ONE_MINUTE_TIMEOUT)
  public void testSendHugeAmount() {
    circularSendSingleMessage(2, 104857600);
  }

  @Test(timeout = ONE_MINUTE_TIMEOUT)
  public void testSendManyParties() {
    circularSendSingleMessage(10, 1024);
  }

  /**
   * This test sends multiple messages from all parties to a single receiver.
   *
   * @param numParties the number of parties
   * @param numMessages the number of messages
   */
  private void sendMultipleToSingleReceiver(int numParties, int numMessages) {
    networks = createNetworks(numParties);
    IntStream.range(1, numParties + 1).parallel().forEach(id -> {
      if (id == numParties) {
        for (int i = 1; i < numParties; i++) {
          Random r = new Random(i);
          final byte[] data = new byte[1024];
          byte[] receivedData;
          for (int j = 0; j < numMessages; j++) {
            receivedData = networks.get(id).receive(i);
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
    });
  }

  @Test(timeout = ONE_MINUTE_TIMEOUT)
  public void testManyPartiesSendToOneReceiver() {
    sendMultipleToSingleReceiver(10, 100);
  }

  @Test(timeout = ONE_MINUTE_TIMEOUT)
  public void testSendManyMessagesToOneReceiver() {
    sendMultipleToSingleReceiver(2, 10000);
  }

  // TESTING FOR FAILURE

  @Test(expected = RuntimeException.class, timeout = ONE_MINUTE_TIMEOUT)
  public void testFailedSend() {
    networks = createNetworks(2);
    try {
      // Close channel to provoke IOException while sending
      ((AsyncNetwork) (networks.get(1))).clients.get(2).close();
    } catch (IOException e) {
      fail("IOException closing channel");
      e.printStackTrace();
    }
    networks.get(1).send(2, new byte[] { 0x01 });
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    networks.get(1).send(2, new byte[] { 0x01 });
  }

  @Test(expected = RuntimeException.class, timeout = ONE_MINUTE_TIMEOUT)
  public void testFailedReceive() throws Exception {
    networks = createNetworks(2);
    // Close network to provoke IOException while receiving
    networks.get(1).close();
    networks.get(1).receive(2);
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = ONE_MINUTE_TIMEOUT)
  public void testFailToBind() throws Throwable {
    ServerSocket socket = null;
    try {
      List<NetworkConfiguration> confs = getNetConfs(2);
      socket = ServerSocketFactory.getDefault()
        .createServerSocket(confs.get(0).getMe().getPort());
      new AsyncNetwork(confs.get(0));
    } finally {
      socket.close();
    }
  }

  @Test(expected = RuntimeException.class, timeout = ONE_MINUTE_TIMEOUT)
  public void testConnectInterrupt() throws Throwable {
    List<NetworkConfiguration> confs = getNetConfs(2);
    ExecutorService es = Executors.newSingleThreadExecutor();
    Future<AsyncNetwork> f = es.submit(() -> new AsyncNetwork(confs.get(1)));
    es.shutdownNow();
    try {
      f.get();
    } catch (InterruptedException e) {
      fail("Test should not be interrupted");
    } catch (ExecutionException e) {
      throw e.getCause();
    }
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = ONE_MINUTE_TIMEOUT)
  public void testConnectTimeout() {

    List<NetworkConfiguration> confs = getNetConfs(2);
    // This should time out waiting for a connection to a party that is not listening
    new AsyncNetwork(confs.get(0), 10);
  }

  @Test(timeout = ONE_MINUTE_TIMEOUT)
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

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = ONE_MINUTE_TIMEOUT)
  public void testHandshakeFail() throws IOException, InterruptedException, ExecutionException {
    List<NetworkConfiguration> confs = getNetConfs(2);
    ServerSocketChannel server = ServerSocketChannel.open();
    server.bind(new InetSocketAddress("localhost", confs.get(1).getMe().getPort()));
    try {
      new ClosedEarlyAsyncNetwork(confs.get(0), 15000);
    } catch (RuntimeException e) {
      server.close();
      throw e;
    }
  }

  private List<Integer> getFreePorts(int numPorts) {
    List<Integer> ports = new ArrayList<>();
    for (int i = 0; i < numPorts; i++) {
      try (ServerSocket s = new ServerSocket(0)) {
        ports.add(s.getLocalPort());
      } catch (IOException e) {
        throw new RuntimeException("No free ports", e);
      }
    }
    return ports;
  }

  private Map<Integer, CloseableNetwork> createNetworks(int numParties) {
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    return createNetworks(confs);
  }

  private Map<Integer, CloseableNetwork> createNetworks(List<NetworkConfiguration> confs) {
    int numParties = confs.get(0).noOfParties();
    ForkJoinPool forkJoinPool = new ForkJoinPool(numParties);
    Map<Integer, CloseableNetwork> netMap = new HashMap<>(numParties);
    try {
      List<AsyncNetwork> netList = forkJoinPool.submit(
          () -> confs.parallelStream().map(c -> new AsyncNetwork(c)).collect(Collectors.toList()))
          .get();
      IntStream.range(1, numParties + 1).forEach(i -> netMap.put(i, netList.get(i - 1)));
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      fail("Failed to setup networks.");
    } finally {
      forkJoinPool.shutdown();
    }
    return netMap;
  }

  private List<NetworkConfiguration> getNetConfs(int numParties) {
    Map<Integer, Party> parties = new HashMap<>(numParties);
    List<NetworkConfiguration> confs = new ArrayList<>(numParties);
    List<Integer> ports = getFreePorts(numParties);
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

  private void closeNetworks(Map<Integer, CloseableNetwork> networks) {
    networks.values().parallelStream().forEach(CloseableNetwork::close);
  }

  private int nextParty(int myId, int numParties) {
    return (myId == numParties) ? 1 : myId + 1;
  }

  private int prevParty(int myId, int numParties) {
    return (myId == 1) ? numParties : myId - 1;
  }


  private class ClosedEarlyAsyncNetwork extends AsyncNetwork {

    public ClosedEarlyAsyncNetwork(NetworkConfiguration conf, int timeout) {
      super(conf, timeout);
    }

    @Override
    void connectServer() throws IOException {
      close();
      super.connectServer();
    }
  }
}

