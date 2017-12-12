package dk.alexandra.fresco.framework.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KryoNetNetwork implements Network, Closeable {

  private List<Server> servers;

  // Map per partyId to a list of clients where the length of the list is equal to channelAmount.
  private Map<Integer, List<Client>> clients;
  private List<Thread> clientThreads;
  private NetworkConfiguration conf;

  // List is as long as channelAmount and contains a map for each partyId to a queue
  private List<Map<Integer, BlockingQueue<byte[]>>> queues;
  private int channelAmount;

  private static final Logger logger = LoggerFactory.getLogger(KryoNetNetwork.class);

  /**
   * Creates a KryoNet network from the given configuration. Calling the constructor will
   * immediately trigger an attempt at connecting to the other parties.
   *
   * @param conf The configuration informing the network of the number of parties and where to
   *     connect to them.
   */
  public KryoNetNetwork(NetworkConfiguration conf) {
    Log.set(Log.LEVEL_ERROR);
    this.conf = conf;
    this.channelAmount = 1;
    this.clients = new HashMap<>();
    this.clientThreads = new ArrayList<>();
    this.servers = new ArrayList<>();
    this.queues = new ArrayList<>();

    // TODO: How to reason about the upper boundries of what can be send in a single round?
    int writeBufferSize = 1048576;
    int objectBufferSize = writeBufferSize;

    for (int j = 0; j < channelAmount; j++) {
      Server server = new Server(1024, objectBufferSize);
      register(server);
      this.servers.add(server);
      this.queues.add(new HashMap<>());
    }

    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (i != conf.getMyId()) {
        this.clients.put(i, new ArrayList<>());
        for (int j = 0; j < channelAmount; j++) {
          Client client = new Client(writeBufferSize, 1024);
          register(client);
          clients.get(i).add(client);
        }

        String secretSharedKey = conf.getParty(i).getSecretSharedKey();
        if (secretSharedKey != null) {
          logger.warn("Encrypted channel towards Party " + i
              + " should have been enabled, but the KryoNet network implementation does "
              + "not yet support this feature. If important, use the ScapiNetwork implementation, "
              + "or use a VPN connection between parties.");
        }
      }

      for (int j = 0; j < channelAmount; j++) {
        this.queues.get(j).put(i, new ArrayBlockingQueue<>(10000));
      }
    }
    try {
      connect();
    } catch (IOException e) {
      throw new MPCException("Cannot connect to other party", e);
    }
  }

  private static class ClientConnectThread extends Thread {

    private final Client client;
    private final String hostname;
    private final int port;
    private final Semaphore semaphore;

    public ClientConnectThread(Client client, String hostname, int port, Semaphore semaphore) {
      super("Connect");
      this.client = client;
      this.hostname = hostname;
      this.port = port;
      this.semaphore = semaphore;
    }

    public void run() {
      boolean success = false;
      int maxRetries = 30;
      int retries = 0;
      while (!success) {
        try {
          retries++;
          client.connect(2000, hostname, port);
          // Server communication after connection can go here, or in Listener#connected().
          success = true;
        } catch (IOException ex) {
          if (retries >= maxRetries) {
            //release to inform that this thread is done trying to connect            
            logger.error(
                "Could not connect to other party within 30 retries of half a second each.");
            this.semaphore.release();
            break;
          }
          try {
            sleep(500);
          } catch (InterruptedException e) {
            //release to inform that this thread is done trying to connect            
            logger.error("Thread got interrupted while trying to reconnect.");
            this.semaphore.release();
            break;
          }
        }
      }
    }
  }

  private class NaiveListener extends Listener {

    private Map<Integer, BlockingQueue<byte[]>> queue;
    private Map<Integer, Integer> connectionIdToPartyId;

    public NaiveListener(Map<Integer, BlockingQueue<byte[]>> queue) {
      this.queue = queue;
      this.connectionIdToPartyId = new HashMap<>();
    }

    @Override
    public void received(Connection connection, Object object) {
      // Maybe a keep alive message will be offered to the queue. - so we should ignore it.
      if (object instanceof byte[]) {
        byte[] data = (byte[]) object;
        int fromPartyId = this.connectionIdToPartyId.get(connection.getID());
        this.queue.get(fromPartyId).offer(data);
      } else if (object instanceof Integer) {
        // Initial handshake to determine who the remote party is.
        this.connectionIdToPartyId.put(connection.getID(), (Integer) object);
      }
    }
  }

  private void connect() throws IOException {
    final Semaphore semaphore = new Semaphore(-((conf.noOfParties() - 1) * channelAmount - 1));
    final List<Boolean> successes = new ArrayList<>();
    for (int j = 0; j < channelAmount; j++) {
      Server server = this.servers.get(j);
      int port = conf.getMe().getPort() + j;
      logger.debug("P" + conf.getMyId() + ": Trying to bind to " + port);
      server.bind(port);
      server.start();

      server.addListener(new NaiveListener(queues.get(j)));
    }

    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (i != conf.getMyId()) {
        for (int j = 0; j < channelAmount; j++) {

          Client client = clients.get(i).get(j);

          client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
              connection.sendTCP(conf.getMyId());
              successes.add(true);
              semaphore.release();
            }
          });

          client.start();

          String hostname = conf.getParty(i).getHostname();
          int port = conf.getParty(i).getPort() + j;
          Thread clientThread = new ClientConnectThread(client, hostname, port, semaphore);
          clientThread.start();
          clientThreads.add(clientThread);
        }
      }
    }
    try {
      semaphore.acquire();
      if (successes.size() < (conf.noOfParties() - 1) * channelAmount) {
        throw new IOException("Could not successfully connect to all parties.");
      }
    } catch (InterruptedException e) {
      for (Thread t : clientThreads) {
        t.interrupt();
      }
      throw new IOException("Interrupted during wait for connect", e);
    }
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (this.conf.getMyId() == partyId) {
      ExceptionConverter.safe(
          () -> {
            this.queues.get(0).get(partyId).put(data);
            return null;
          },
          "Send got interrupted");
    } else {
      this.clients.get(partyId).get(0).sendTCP(data);
    }
  }

  @Override
  public byte[] receive(int partyId) {
    return ExceptionConverter.safe(
        () -> this.queues.get(0).get(partyId).take(),
        "Receive got interrupted");
  }

  @Override
  public int getNoOfParties() {
    return conf.noOfParties();
  }

  @Override
  public void close() throws IOException {
    logger.debug("Shutting down KryoNet network");

    for (int j = 0; j < channelAmount; j++) {
      this.servers.get(j).stop();
    }

    for (Thread t : clientThreads) {
      t.interrupt();
    }

    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (i != conf.getMyId()) {
        for (int j = 0; j < channelAmount; j++) {
          this.clients.get(i).get(j).stop();
        }
      }
    }
  }

  private static void register(EndPoint endpoint) {
    Kryo kryo = endpoint.getKryo();
    kryo.register(byte[].class);
    kryo.register(Integer.class);
  }
}
