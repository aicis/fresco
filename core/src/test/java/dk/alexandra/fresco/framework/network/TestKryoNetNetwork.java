package dk.alexandra.fresco.framework.network;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.junit.Assert;
import org.junit.Test;

public class TestKryoNetNetwork {

  private List<Integer> getFreePorts(int no) {
    List<Integer> res = new ArrayList<>();
    for (int i = 0; i < no; i++) {
      res.add(9000 + i);
    }
    return res;
  }

  @Test(timeout = 5000)
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
        network.send(2, new byte[] { 0x04 });
        network.receive(2);
        network.close();
      }
    });
    t1.start();

    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    KryoNetNetwork network = new KryoNetNetwork(conf);
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
  public void testKryoNetSendBytesAllowMultipleMessages() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf, 1000, true, 4000);
        network.send(2, new byte[] { 0x04 });
        network.receive(2);
        network.close();
      }
    });
    t1.start();

    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    KryoNetNetwork network = new KryoNetNetwork(conf, 1000, true, 4000);
    byte[] arr = network.receive(1);
    network.send(1, new byte[] {0x00});
    assertArrayEquals(new byte[] { 0x04 }, arr);
    network.close();
    try {
      t1.join(1000);
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
  }

  /**
   * First Sends 3Mb of random data and then sends the exact amount of data possible within a single
   * batch. Finally send two times the max number of bytes in a batch. Then check if the received
   * bytes are the same.
   */
  @Test(timeout = 10000)
  public void testKryoNetSendHugeAmount() {
    final byte[] toSendAndExpect1 = new byte[3148800];
    final byte[] toSendAndExpect2 = new byte[524280];
    final byte[] toSendAndExpect3 = new byte[524280 * 2];
    new Random().nextBytes(toSendAndExpect1);
    new Random().nextBytes(toSendAndExpect2);
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf, 524288, true, 4000);
        network.send(2, toSendAndExpect1);
        network.send(2, toSendAndExpect2);
        network.send(2, toSendAndExpect3);
        network.close();
      }
    });
    t1.start();

    NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
    KryoNetNetwork network = new KryoNetNetwork(conf, 524288, true, 4000);
    byte[] arr1 = network.receive(1);
    byte[] arr2 = network.receive(1);
    byte[] arr3 = network.receive(1);
    assertArrayEquals(toSendAndExpect1, arr1);
    assertArrayEquals(toSendAndExpect2, arr2);
    assertArrayEquals(toSendAndExpect3, arr3);
    network.close();
    try {
      t1.join();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
  }

  @Test(timeout = 5000)
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
        for (int i = 0; i < 1000000; i++) {
          network.send(1, new byte[] { 0x04 });
        }
        try {
          // This should block after sending 1001 messages
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
    KryoNetNetwork network = new KryoNetNetwork(conf);

    try {
      t1.join(400);
      t1.interrupt();
      t1.join();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    }
    network.close();
  }

  @SuppressWarnings("resource")
  @Test(expected = RuntimeException.class, timeout = 70000)
  public void testKryoNetConnectTimeout() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
    // This should time out waiting for a connection to a party that is not listening
    new KryoNetNetwork(conf, 1000, false, 1000);
  }

  @Test(expected = IOException.class, timeout = 5000)
  public void testKryoNetConnectInterrupt() throws IOException {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
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
      t1.join(200);
      futureTask.get();
    } catch (InterruptedException e) {
      fail("Threads should finish without main getting interrupted");
    } catch (ExecutionException e) {
      e.printStackTrace();
      throw (IOException) e.getCause().getCause();
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
      final ConcurrentHashMap<Integer, KryoNetNetwork> networks = new ConcurrentHashMap<>();
      for (int i = 0; i < noOfParties; i++) {
        final int myId = i + 1;
        FutureTask<Object> t = new FutureTask<>(new Callable<Object>() {

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
          e.printStackTrace();
          Assert.fail("Tasks should not throw exceptions");
        }
      }
      // Closing networks again.
      for (KryoNetNetwork network : networks.values()) {
        network.close();
      }
    }
  }
}
