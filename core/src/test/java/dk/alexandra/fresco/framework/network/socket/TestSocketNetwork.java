package dk.alexandra.fresco.framework.network.socket;

import static dk.alexandra.fresco.framework.network.socket.Connector.DEFAULT_CONNECTION_TIMEOUT;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.AbstractCloseableNetworkTest;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;

public class TestSocketNetwork extends AbstractCloseableNetworkTest {

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf) {
    return newCloseableNetwork(conf, DEFAULT_CONNECTION_TIMEOUT);
  }

  @Override
  protected CloseableNetwork newCloseableNetwork(NetworkConfiguration conf, Duration timeout) {
    return new SocketNetwork(conf, new Connector(conf, timeout).getSocketMap());
  }


  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("resource")
  public void testMissingParty() throws InterruptedException, ExecutionException {
    final int numParties = 3;
    final int dropId = 2;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<NetworkConnector>> fs = new ArrayList<>(numParties);
    try {
      for (int i = 0; i < numParties; i++) {
        final int id = i;
        fs.add(es.submit(() -> new Connector(confs.get(id), DEFAULT_CONNECTION_TIMEOUT)));
      }
      Map<Integer, Socket> missingPartyMap = fs.get(0).get().getSocketMap();
      missingPartyMap.remove(dropId);
      new SocketNetwork(confs.get(0), missingPartyMap);
    } finally {
      for (Future<NetworkConnector> futureConn : fs) {
        for (Socket s : futureConn.get().getSocketMap().values()) {
          try {
            s.close();
          } catch (IOException e) {
            // ignore
          }
        }
      }
      es.shutdownNow();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("resource")
  public void testIdOutOfRange() throws InterruptedException, ExecutionException, IOException {
    final int numParties = 3;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<NetworkConnector>> fs = new ArrayList<>(numParties);
    try {
      for (int i = 0; i < numParties; i++) {
        final int id = i;
        fs.add(es.submit(() -> new Connector(confs.get(id), DEFAULT_CONNECTION_TIMEOUT)));
      }
      Map<Integer, Socket> extraPartyMap = fs.get(0).get().getSocketMap();
      extraPartyMap.put(numParties + 1, extraPartyMap.get(numParties));
      new SocketNetwork(confs.get(0), extraPartyMap);
    } finally {
      for (Future<NetworkConnector> futureConn : fs) {
        for (Socket s : futureConn.get().getSocketMap().values()) {
          s.close();
        }
      }
      es.shutdownNow();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("resource")
  public void testClosedSockets() throws InterruptedException, ExecutionException, IOException {
    final int numParties = 3;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<NetworkConnector>> fs = new ArrayList<>(numParties);
    try {
      for (int i = 0; i < numParties; i++) {
        final int id = i;
        fs.add(es.submit(() -> new Connector(confs.get(id), DEFAULT_CONNECTION_TIMEOUT)));
      }
      Map<Integer, Socket> socketMap = fs.get(0).get().getSocketMap();
      socketMap.get(numParties).close();
      new SocketNetwork(confs.get(0), socketMap);
    } finally {
      for (Future<NetworkConnector> futureConn : fs) {
        for (Socket s : futureConn.get().getSocketMap().values()) {
          s.close();
        }
      }
      es.shutdownNow();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("resource")
  public void testUnconnectedSocket() throws InterruptedException, ExecutionException, IOException {
    final int numParties = 3;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    Map<Integer, Socket> socketMap = new HashMap<>(numParties);
    for (int i = 0; i < numParties; i++) {
      socketMap.put(i + 1, new Socket());
    }
    new SocketNetwork(confs.get(0), socketMap);

  }

  @Test(expected = RuntimeException.class)
  public void testStoppedReciever() throws InterruptedException, ExecutionException, IOException {
    final int numParties = 3;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<NetworkConnector>> fs = new ArrayList<>(numParties);
    CloseableNetwork network = null;
    try {
      for (int i = 0; i < numParties; i++) {
        final int id = i;
        fs.add(es.submit(() -> new Connector(confs.get(id), DEFAULT_CONNECTION_TIMEOUT)));
      }
      Map<Integer, Socket> socketMap1 = fs.get(0).get().getSocketMap();
      Map<Integer, Socket> socketMap2 = fs.get(1).get().getSocketMap();
      network = new SocketNetwork(confs.get(0), socketMap1);
      new DataOutputStream(socketMap2.get(1).getOutputStream()).writeInt(-1);
      network.receive(2);
    } finally {
      for (Future<NetworkConnector> futureConn : fs) {
        for (Socket s : futureConn.get().getSocketMap().values()) {
          s.close();
        }
      }
      if (network != null) {
        network.close();
      }
      es.shutdownNow();
    }
  }

  @Test
  public void testClosedSocketReciever()
      throws InterruptedException, ExecutionException, IOException {
    final int numParties = 2;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<NetworkConnector>> fs = new ArrayList<>(numParties);
    try {
      for (int i = 0; i < numParties; i++) {
        final int id = i;
        fs.add(es.submit(() -> new Connector(confs.get(id), DEFAULT_CONNECTION_TIMEOUT)));
      }
      Map<Integer, Socket> socketMap1 = fs.get(0).get().getSocketMap();
      Map<Integer, Socket> socketMap2 = fs.get(1).get().getSocketMap();
      Receiver r = new Receiver(socketMap1.get(2));
      socketMap2.get(1).close();
    } finally {
      for (Future<NetworkConnector> futureConn : fs) {
        for (Socket s : futureConn.get().getSocketMap().values()) {
          // s.close();
        }
      }

      es.shutdownNow();
    }
  }

  @Test(expected = RuntimeException.class)
  public void testStoppedSender()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException,
      IllegalAccessException, InterruptedException, ExecutionException, IOException {
    networks = createNetworks(2);
    Field f1 = networks.get(1).getClass().getDeclaredField("senders");
    f1.setAccessible(true);
    @SuppressWarnings("unchecked")
    Sender sender = ((HashMap<Integer, Sender>) f1.get(networks.get(1))).get(2);
    sender.stop();
    networks.get(1).send(2, new byte[]{0x01});
    f1.setAccessible(false);
  }

  @Test
  public void testFailedSender()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException,
      IllegalAccessException, InterruptedException, ExecutionException, IOException {
    networks = createNetworks(2);
    Field f1 = networks.get(1).getClass().getDeclaredField("senders");
    f1.setAccessible(true);
    @SuppressWarnings("unchecked")
    Sender sender = ((HashMap<Integer, Sender>) f1.get(networks.get(1))).get(2);
    Field f2 = sender.getClass().getDeclaredField("thread");
    f2.setAccessible(true);
    ((Thread) f2.get(sender)).interrupt();
    f1.setAccessible(false);
    f2.setAccessible(false);
  }

  @Test
  public void testSenderFailsWhileStopping()
      throws InterruptedException, ExecutionException, IOException {
    final int numParties = 2;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<NetworkConnector>> fs = new ArrayList<>(numParties);
    try {
      for (int i = 0; i < numParties; i++) {
        final int id = i;
        fs.add(es.submit(() -> new Connector(confs.get(id), DEFAULT_CONNECTION_TIMEOUT)));
      }
      Map<Integer, Socket> socketMap = fs.get(0).get().getSocketMap();
      Sender s = new Sender(socketMap.get(2));
      socketMap.get(2).close();
      s.stop();
    } finally {
      for (Future<NetworkConnector> futureConn : fs) {
        for (Socket s : futureConn.get().getSocketMap().values()) {
          s.close();
        }
      }
      es.shutdownNow();
    }
  }

  @Test
  public void testSenderBreakingSocket()
      throws Exception {
    final int numParties = 2;
    List<NetworkConfiguration> confs = getNetConfs(numParties);
    ExecutorService es = Executors.newFixedThreadPool(numParties);
    List<Future<NetworkConnector>> fs = new ArrayList<>(numParties);
    try {
      for (int i = 0; i < numParties; i++) {
        final int id = i;
        fs.add(es.submit(() -> new Connector(confs.get(id), DEFAULT_CONNECTION_TIMEOUT)));
      }
      Map<Integer, Socket> socketMap = fs.get(0).get().getSocketMap();
      Socket socket = socketMap.get(2);
      Sender sender = new Sender(socket);

      socket.close();
      sender.queueMessage(new byte[1]);
    } finally {
      for (Future<NetworkConnector> futureConn : fs) {
        for (Socket s : futureConn.get().getSocketMap().values()) {
          s.close();
        }
      }
      es.shutdownNow();
    }
  }

}
