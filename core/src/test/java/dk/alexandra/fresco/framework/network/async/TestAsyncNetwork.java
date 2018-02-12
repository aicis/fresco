package dk.alexandra.fresco.framework.network.async;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.net.ServerSocketFactory;
import org.junit.Test;

public class TestAsyncNetwork {

  private List<Integer> getFreePorts(int no) {
    List<Integer> res = new ArrayList<>();
    for (int i = 0; i < no; i++) {
      res.add(9000 + i);
    }
    return res;
  }

  @Test(timeout = 5000)
  public void testSendBytes() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0), "78dbinb27xi1i"));
    parties.put(2, new Party(2, "localhost", ports.get(1), "h287hs287g22n"));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        AsyncNetwork network = new AsyncNetwork(conf);
        network.send(2, new byte[] { 0x04 });
        network.receive(2);
        network.close();
      }
    });
    t1.start();

    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    AsyncNetwork network = new AsyncNetwork(conf);
    byte[] arr = network.receive(1);
    network.send(1, new byte[] { 0x00 });
    assertArrayEquals(new byte[] { 0x04 }, arr);
    // Also test noOfParties
    int noOfParties = network.getNoOfParties();
    assertEquals(parties.size(), noOfParties);
    network.close();
    try {
      t1.join(1000);
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
  }

  @Test(timeout = 5000)
  public void testSendBytesAllowMultipleMessages() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        AsyncNetwork network = new AsyncNetwork(conf);
        network.send(2, new byte[] { 0x04 });
        network.receive(2);
        network.close();
      }
    });
    t1.start();

    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    AsyncNetwork network = new AsyncNetwork(conf);
    byte[] arr = network.receive(1);
    network.send(1, new byte[] { 0x00 });
    assertArrayEquals(new byte[] { 0x04 }, arr);
    network.close();
    try {
      t1.join(1000);
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
  }

  // Send 100Mb through the network to stress-test it.
  @Test(timeout = 10000)
  public void testSendHugeAmount() {
    final byte[] toSendAndExpect1 = new byte[104857600];
    new Random().nextBytes(toSendAndExpect1);
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        AsyncNetwork network = new AsyncNetwork(conf);
        network.send(2, toSendAndExpect1);
        network.close();
      }
    });
    t1.start();

    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    AsyncNetwork network = new AsyncNetwork(conf);
    byte[] arr1 = network.receive(1);
    assertArrayEquals(toSendAndExpect1, arr1);
    network.close();
    try {
      t1.join();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
  }

  @Test(timeout = 5000)
  public void testSendInterrupt() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        AsyncNetwork network = new AsyncNetwork(conf);
        for (int i = 0; i < 1000000; i++) {
          network.send(1, new byte[] { 0x04 });
        }
        try {
          // This should block after sending 1000001 messages
          network.send(1, new byte[] { 0x04 });
        } catch (RuntimeException e) {
          // Interrupting the thread should result in a RuntimeException unblocking the thread
        } finally {
          network.close();
        }
      }
    });
    t1.start();
    // Connect to the party run in t1 above
    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    AsyncNetwork network = new AsyncNetwork(conf);

    try {
      t1.join(400);
      t1.interrupt();
      t1.join();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
    network.close();
  }

  @Test(expected = RuntimeException.class, timeout = 5000)
  public void testConnectInterrupt() throws IOException {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    FutureTask<Object> futureTask = new FutureTask<>(new Callable<Object>() {

      @SuppressWarnings("resource")
      @Override
      public Object call() throws Exception {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        new AsyncNetwork(conf);
        return null;
      }
    });
    Thread t1 = new Thread(futureTask);
    t1.start();
    try {
      t1.join(200);
      t1.interrupt();
      t1.join(200);
      futureTask.get();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    } catch (ExecutionException e) {
      throw (RuntimeException) e.getCause().getCause();
    }
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = 70000)
  public void testConnectTimeout() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
    // This should time out waiting for a connection to a party that is not listening
    new AsyncNetwork(conf, 200);
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = 70000)
  public void testCannotBind() throws IOException {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
    // This should time out waiting for a connection to a party that is not listening
    ServerSocket socket = ServerSocketFactory.getDefault().createServerSocket(ports.get(0));
    try {
      new AsyncNetwork(conf, 200);
    } catch (RuntimeException e) {
      throw e;
    } finally {
      socket.close();
    }
  }

  @Test(timeout = 200000)
  public void testPartiesReconnect() {
    List<Integer> ports = getFreePorts(5);
    testMultiplePartiesReconnect(2, 30, ports.subList(0, 2));
    testMultiplePartiesReconnect(3, 20, ports.subList(0, 3));
    testMultiplePartiesReconnect(5, 20, ports);
  }

  private void testMultiplePartiesReconnect(int noOfParties, int noOfRetries, List<Integer> ports) {
    Map<Integer, Party> parties = new HashMap<>();

    for (int i = 1; i <= noOfParties; i++) {
      parties.put(i, new Party(i, "localhost", ports.get(i - 1)));
    }
    for (int retry = 0; retry < noOfRetries; retry++) {
      List<FutureTask<Object>> tasks = new ArrayList<>();
      final ConcurrentHashMap<Integer, AsyncNetwork> networks = new ConcurrentHashMap<>();
      for (int i = 0; i < noOfParties; i++) {
        final int myId = i + 1;
        FutureTask<Object> t = new FutureTask<>(new Callable<Object>() {

          @Override
          public Object call() {
            NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties);
            AsyncNetwork network = new AsyncNetwork(conf);
            networks.put(myId, network);
            return null;
          }
        });
        tasks.add(t);
        (new Thread(t)).start();
      }

      for (FutureTask<Object> t : tasks) {
        try {
          t.get();
        } catch (Exception e) {
          e.printStackTrace();
          fail("Tasks should not throw exceptions");
        }
      }
      // Closing networks again.
      for (AsyncNetwork network : networks.values()) {
        network.close();
      }
    }
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = 5000)
  public void testHandshakeFail() throws IOException, InterruptedException, ExecutionException {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    ServerSocketChannel server = ServerSocketChannel.open();
    server.bind(new InetSocketAddress("localhost", ports.get(0)));
    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    try {
      new ClosedEarlyAsyncNetwork(conf, 15000);
    } catch (RuntimeException e) {
      server.close();
      throw e;
    }
  }

  private class ClosedEarlyAsyncNetwork extends AsyncNetwork {

    public ClosedEarlyAsyncNetwork(NetworkConfiguration conf, int timeout) {
      super(conf, timeout);
    }

    @Override
    void handshake() {
      close();
      super.handshake();
    }
  }
}

