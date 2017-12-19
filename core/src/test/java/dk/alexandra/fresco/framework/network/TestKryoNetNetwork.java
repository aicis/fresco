package dk.alexandra.fresco.framework.network;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.junit.Assert;
import org.junit.Test;

public class TestKryoNetNetwork {

  private List<Integer> getFreePorts(int no) {
    try {
      List<Integer> ports = new ArrayList<>();
      List<ServerSocket> socks = new ArrayList<>();
      for (int i = 0; i < no; i++) {
        ServerSocket sock = new ServerSocket(0);
        int port = sock.getLocalPort();
        ports.add(port);
        socks.add(sock);
      }
      for (ServerSocket s : socks) {
        s.close();
      }

      return ports;
    } catch (IOException e) {
      Assert.fail("Could not locate a free port");
      return null;
    }
  }

  @Test
  public void testKryoNetSendBytes() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0), "78dbinb27xi1i"));
    parties.put(2, new Party(2, "localhost", ports.get(1), "h287hs287g22n"));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        network.send(2, new byte[] {0x04});
        try {
          network.close();
        } catch (IOException e) {
          // Ignore
        }
      }
    });
    t1.start();

    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    KryoNetNetwork network = new KryoNetNetwork(conf);
    byte[] arr = network.receive(1);
    assertArrayEquals(new byte[] {0x04}, arr);
    try {
      network.close();
    } catch (IOException e) {
      fail("Failed to close network");
    }
    try {
      t1.join();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
  }

  @Test
  public void testKryoNetSendInterrupt() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        for (int i = 0; i < 1000; i++) {
          network.send(1, new byte[] {0x04});
        }
        try {
          // This should block after sending 1001 messages
          network.send(1, new byte[] {0x04});
        } catch (RuntimeException e) {
          // Interrupting the thread should result in a RuntimeException unblocking the thread
        } finally {
          try {
            network.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });
    t1.start();
    // Connect to the party run in t1 above
    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    KryoNetNetwork network = new KryoNetNetwork(conf);

    try {
      t1.join(400);
      t1.interrupt();
      t1.join();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
    try {
      network.close();
    } catch (IOException e) {
      e.printStackTrace();
      fail("Closing network should not result in an exception");
    }
  }

  @SuppressWarnings("resource")
  @Test (expected = RuntimeException.class)
  public void testKryoNetConnectTimeout() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
    //This should time out waiting for a connection to a party that is not listening
    new KryoNetNetwork(conf);
  }

  @Test (expected = IOException.class)
  public void testKryoNetConnectInterrupt() throws IOException {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", 9000));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    FutureTask<Object> futureTask = new FutureTask<>(new Callable<Object>() {

      @SuppressWarnings("resource")
      @Override
      public Object call() throws Exception {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        new KryoNetNetwork(conf);
        return null;
      }
    });
    Thread t1 = new Thread(futureTask);
    t1.start();
    try {
      t1.join(200);
      t1.interrupt();
      futureTask.get();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    } catch (ExecutionException e) {
      e.printStackTrace();
      throw (IOException)e.getCause().getCause();
    }
  }

  @Test
  public void testPartiesReconnect() {
    List<Integer> ports = getFreePorts(5);
    testMultiplePartiesReconnect(2, 100, ports.subList(0, 2));
    testMultiplePartiesReconnect(3, 50, ports.subList(0, 3));
    testMultiplePartiesReconnect(5, 50, ports);
  }

  private void testMultiplePartiesReconnect(int noOfParties, int noOfRetries, List<Integer> ports) {
    Map<Integer, Party> parties = new HashMap<>();

    for (int i = 1; i <= noOfParties; i++) {
      parties.put(i, new Party(i, "localhost", ports.get(i - 1)));
    }
    for (int retry = 0; retry < noOfRetries; retry++) {
      List<FutureTask<Object>> tasks = new ArrayList<>();
      final ConcurrentHashMap<Integer, KryoNetNetwork> networks = new ConcurrentHashMap<>();
      for (int i = 0; i < noOfParties; i++) {
        final int myId = i + 1;
        FutureTask<Object> t  = new FutureTask<>(new Callable<Object>() {

          @Override
          public Object call() {
            NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties);
            KryoNetNetwork network = new KryoNetNetwork(conf);
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
          Assert.fail("Tasks should not throw exceptions");
        }
      }
      // Closing networks again.
      for (KryoNetNetwork network : networks.values()) {
        try {
          network.close();
        } catch (IOException e) {
          Assert.fail("Closing the networks should not result in exceptions");
        }
      }
    }
  }
}
