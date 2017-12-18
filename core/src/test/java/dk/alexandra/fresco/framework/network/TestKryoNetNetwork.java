package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestKryoNetNetwork {

  private static List<Exception> exceptions = new ArrayList<>();

  @Before
  public void reset() {
    exceptions.clear();
  }

  /**
   * Checks for exceptions and casts an Assert.fail if any was found
   */
  public void check() {
    if (!exceptions.isEmpty()) {
      String messages = "";
      for (Exception e : exceptions) {
        messages += messages + e.getMessage() + "." + System.lineSeparator();
      }
      Assert.fail(messages);
    }
  }

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
        int noOfParties = network.getNoOfParties();
        if (noOfParties != 2) {
          exceptions.add(new RuntimeException("There should only be 2 parties"));
        }
        try {
          network.close();
        } catch (IOException e) {
          exceptions.add(e);
        }
      }
    });

    Thread t2 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        byte[] arr = network.receive(1);
        if (!Arrays.equals(new byte[] {0x04}, arr)) {
          exceptions.add(
              new RuntimeException("Party 2 should have received the byte 0x04. Instead received "
                  + Arrays.toString(arr)));
        }
        try {
          network.close();
        } catch (IOException e) {
          exceptions.add(e);
        }
      }
    });

    t1.start();
    t2.start();
    try {
      t1.join();
      t2.join();
      check();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
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
          network.send(1, new byte[] {0x04});
          exceptions
              .add(new RuntimeException("After 1001 messages the queue should be filled up."));
        } catch (RuntimeException e) {
          // ignore - this should happen
        } finally {
          try {
            network.close();
          } catch (IOException e) {
            exceptions.add(e);
          }
        }
      }
    });

    Thread t2 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(2, parties);
        KryoNetNetwork network = new KryoNetNetwork(conf);
        try {
          network.receive(2);
          exceptions.add(new RuntimeException("Should not be able to receive anything"));
        } catch (RuntimeException e) {
          // ignore - this should happen
        } finally {
          try {
            network.close();
          } catch (IOException e) {
            exceptions.add(e);
          }
        }
      }
    });

    t1.start();
    t2.start();
    try {
      t2.join(400);
      t1.join(400);
      t1.interrupt();
      t2.interrupt();

      t1.join();
      t2.join();
      check();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
    }
  }

  @Test
  public void testKryoNetConnectTimeout() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", ports.get(0)));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = null;
        try {
          network = new KryoNetNetwork(conf);
          exceptions.add(new RuntimeException("Should not be able to connect"));
        } catch (RuntimeException e) {
          // ignore - should happen
        } finally {
          try {
            if (network != null) {
              network.close();
            }
          } catch (IOException e) {
            exceptions.add(e);
          }
        }
      }
    });

    t1.start();
    try {
      t1.join();
      t1.interrupt();
      check();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
    }
  }

  @Test
  public void testKryoNetConnectInterrupt() {
    Map<Integer, Party> parties = new HashMap<>();
    List<Integer> ports = getFreePorts(2);
    parties.put(1, new Party(1, "localhost", 9000));
    parties.put(2, new Party(2, "localhost", ports.get(1)));
    List<Exception> exs = new ArrayList<>();
    Thread t1 = new Thread(new Runnable() {

      @Override
      public void run() {
        NetworkConfiguration conf = new NetworkConfigurationImpl(1, parties);
        KryoNetNetwork network = null;
        try {
          network = new KryoNetNetwork(conf);
          throw new RuntimeException("Should not be able to connect");
        } catch (RuntimeException e) {
          exs.add(e);
        } finally {
          try {
            if (network != null) {
              network.close();
            }
          } catch (IOException e) {
            exceptions.add(e);
          }
        }
      }
    });

    t1.start();
    try {
      t1.join(200);
      t1.interrupt();
      t1.join();
      if (!exs.isEmpty()) {
        if (exs.get(0).getCause() != null && !(exs.get(0).getCause() instanceof IOException)) {
          exs.get(0).printStackTrace();
          Assert.fail("Exception should not have a cause other than IOException");
        }
      }
      check();
    } catch (InterruptedException e) {
      Assert.fail("Threads should finish without main getting interrupted");
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
      List<Thread> threads = new ArrayList<>();
      final List<Exception> exs = new ArrayList<>();
      final ConcurrentHashMap<Integer, KryoNetNetwork> networks = new ConcurrentHashMap<>();

      for (int i = 0; i < noOfParties; i++) {
        final int myId = i + 1;
        final Thread t = new Thread(new Runnable() {

          @Override
          public void run() {
            NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties);
            try {
              KryoNetNetwork network = new KryoNetNetwork(conf);
              networks.put(myId, network);
            } catch (RuntimeException e) {
              exs.add(e);
              throw e;
            }
          }
        });
        threads.add(t);
        t.start();
      }

      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          Assert.fail("Threads should finish without main getting interrupted");
        }
      }
      if (exs.size() != 0) {
        Assert.fail("Exception occured while trying to connect: " + exs.get(0).getMessage());
      }
      // disconnect again.
      for (KryoNetNetwork network : networks.values()) {
        try {
          network.close();
        } catch (IOException e) {
          Assert.fail("Should be able to disconnect again");
        }
      }
    }
  }
}
