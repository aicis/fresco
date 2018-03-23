package dk.alexandra.fresco.framework.network.async;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
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

  private static final int TWO_MINUTE_TIMEOUT_MILLIS = 120000;
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

  // TEST CLOSE

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testClose() {
    networks = createNetworks(3);
    closeNetworks(networks);
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testNegativeTimeout() {
    List<NetworkConfiguration> conf = getNetConfs(2);
    new AsyncNetwork(conf.get(1), Duration.ofMillis(-10));
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
    networks.keySet().stream().forEach(i -> networks.get(i).send(i, new byte[] {0x01}));
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

  // Send 100Mb through the network to stress-test it.
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
   */
  private void sendMultipleToSingleReceiver(int numParties, int numMessages) {
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
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        fail("Test should not throw exception");
      }
    }
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testManyPartiesSendToOneReceiver() {
    sendMultipleToSingleReceiver(5, 100);
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testSendManyMessagesToOneReceiver() {
    sendMultipleToSingleReceiver(2, 10000);
  }

  // TESTING FOR FAILURE

  @SuppressWarnings("unchecked")
  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testFailedSend() throws InterruptedException {
    networks = createNetworks(2);
    for (int i = 0; i < 1000; i++) {
      networks.get(1).send(2, new byte[] { 0x01 });
    }
    try {
      // Cancel sendfuture to provoke an exception while sending
      Field f = networks.get(1).getClass().getDeclaredField("sendFutures");
      f.setAccessible(true);
      ((HashMap<Integer, Future<Object>>)f.get(networks.get(1))).get(2).cancel(true);
      f.setAccessible(false);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
        | IllegalAccessException e) {
      fail("Reflection related error");
    }
    // Below the first call should make the sending thread fail
    // The subsequent calls should provoke the exception to propagate to the main thread
    int retries = 10;
    networks.get(1).send(2, new byte[] { 0x01 });
    Thread.sleep(10);
    for (int i = 0; i < retries; i++) {
      networks.get(1).send(2, new byte[] { 0x01 });
    }
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

  @SuppressWarnings("unchecked")
  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testFailedReceive() throws Exception {
    networks = createNetworks(2);
    try {
      // Cancel receiveFuture to provoke exception when receiving
      Field f = networks.get(1).getClass().getDeclaredField("receiveFutures");
      f.setAccessible(true);
      ((HashMap<Integer, Future<Object>>)f.get(networks.get(1))).get(2).cancel(true);
      f.setAccessible(false);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
        | IllegalAccessException e) {
      fail("Reflection related error");
    } catch (Exception e) {
      fail("Should not throw exception yet");
    }
    Thread.sleep(10);
    networks.get(1).receive(2);
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testFailToBind() throws Throwable {
    ServerSocket socket = null;
    try {
      List<NetworkConfiguration> confs = getNetConfs(2);
      socket = ServerSocketFactory.getDefault().createServerSocket(confs.get(0).getMe().getPort());
      new AsyncNetwork(confs.get(0));
    } finally {
      socket.close();
    }
  }

  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
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
  @Test(expected = RuntimeException.class, timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testConnectTimeout() {
    List<NetworkConfiguration> confs = getNetConfs(2);
    // This should time out waiting for a connection to a party that is not listening
    new AsyncNetwork(confs.get(0), Duration.ofMillis(10));
  }

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS)
  public void testPartiesReconnect() {
    testMultiplePartiesReconnect(2, 10);
    testMultiplePartiesReconnect(5, 20);
  }

  // TEST STUFF THAT WILL PROBABLY NEVER HAPPEN TO GET FULL TEST COVERAGE

  @Test(timeout = TWO_MINUTE_TIMEOUT_MILLIS, expected = RuntimeException.class)
  public void testFinishingReceivers() {
    networks = createNetworks(2);
    // Set alive = false in order for the receiver to stop
    try {
      Field f = networks.get(1).getClass().getDeclaredField("alive");
      f.setAccessible(true);
      ((AtomicBoolean)f.get(networks.get(1))).set(false);
      f.setAccessible(false);
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
        | IllegalAccessException e) {
      fail("Reflection related error");
    }
    // wake up the receiver for it notice it should stop
    networks.get(2).send(1, new byte[] {0x01});
    networks.get(1).receive(2);
    // Give the receiver some time
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
    // receiver should now be stopped
    try {
     networks.get(1).receive(2);
     fail("The above receive should throw an exception");
    } finally {
      // Set alive = true so we can close the network properly
      try {
        Field f = networks.get(1).getClass().getDeclaredField("alive");
        f.setAccessible(true);
        ((AtomicBoolean)f.get(networks.get(1))).set(true);
        f.setAccessible(false);
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
          | IllegalAccessException e) {
        fail("Reflection related error");
      }
    }
  }

  // HELPER METHODS

  private void testMultiplePartiesReconnect(int numParties, int noOfRetries) {
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    for (int i = 0; i < noOfRetries; i++) {
      networks = createNetworks(confs);
      closeNetworks(networks);
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
    networks.values().stream().forEach(t -> {
      try {
        t.close();
      } catch (IOException e) {
        fail("This should never happen");
        e.printStackTrace();
      }
    });
  }

  private int nextParty(int myId, int numParties) {
    return (myId == numParties) ? 1 : myId + 1;
  }

  private int prevParty(int myId, int numParties) {
    return (myId == 1) ? numParties : myId - 1;
  }

}

