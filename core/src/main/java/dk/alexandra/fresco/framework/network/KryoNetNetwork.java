package dk.alexandra.fresco.framework.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
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
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KryoNetNetwork implements Network, Closeable {

  private Server server;

  // Map per partyId to a client
  private Map<Integer, Client> clients;
  private List<Thread> clientThreads;
  private NetworkConfiguration conf;

  // Map for each partyId to a queue
  private Map<Integer, BlockingQueue<byte[]>> queues;

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
    this.clients = new HashMap<>();
    this.clientThreads = new ArrayList<>();
    this.queues = new HashMap<>();

    // TODO: How to reason about the upper boundries of what can be send in a single round?
    int writeBufferSize = 1048576;
    int objectBufferSize = writeBufferSize;

    this.server = new Server(1024, objectBufferSize);
    register(this.server);    

    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (i != conf.getMyId()) {
        Client client = new Client(writeBufferSize, 1024);
        register(client);
        clients.put(i, client);
        

        String secretSharedKey = conf.getParty(i).getSecretSharedKey();
        if (secretSharedKey != null) {
          logger.warn("Encrypted channel towards Party " + i
              + " should have been enabled, but the KryoNet network implementation does "
              + "not yet support this feature. If important, use the ScapiNetwork implementation, "
              + "or use a VPN connection between parties.");
        }
      }

      this.queues.put(i, new ArrayBlockingQueue<>(1000));
    }
    
    ExceptionConverter.safe(
        () -> {
          connect();
          return null;
        },
        "Failed to connect to all parties");    
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
    final Semaphore semaphore = new Semaphore(-(conf.noOfParties() - 2));
    final ConcurrentLinkedDeque<Boolean> successes = new ConcurrentLinkedDeque<>();
    Server server = this.server;
    int port = conf.getMe().getPort();
    logger.debug("P" + conf.getMyId() + ": Trying to bind to " + port);
    server.bind(port);
    server.start();

    server.addListener(new NaiveListener(this.queues));    

    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (i != conf.getMyId()) {
        Client client = clients.get(i);

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
        int clientPort = conf.getParty(i).getPort();
        Thread clientThread = new ClientConnectThread(client, hostname, clientPort, semaphore);
        clientThread.start();
        clientThreads.add(clientThread);
      }
    }
    try {
      semaphore.acquire();
      if (successes.size() < (conf.noOfParties() - 1)) {
        throw new IOException("Could not successfully connect to all parties.");
      }
      logger.debug("P" + conf.getMyId() + ": Successfully connected to all parties!");
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
            this.queues.get(partyId).put(data);
            return null;
          },
          "Send got interrupted");
    } else {
      this.clients.get(partyId).sendTCP(data);
    }
  }

  @Override
  public byte[] receive(int partyId) {
    return ExceptionConverter.safe(
        () -> this.queues.get(partyId).take(),
        "Receive got interrupted");
  }

  @Override
  public int getNoOfParties() {
    return conf.noOfParties();
  }

  @Override
  public void close() throws IOException {
    logger.debug("P" + conf.getMyId() + ": Shutting down KryoNet network");

    this.server.stop();

    for (Thread t : clientThreads) {
      t.interrupt();
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.error("Got interrupted while waiting for internal threads to shutdown");
        //Ignore - nothing to do about this
      }
    }

    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (i != conf.getMyId()) {
        this.clients.get(i).stop();
      }
    }
  }

  private static void register(EndPoint endpoint) {
    Kryo kryo = endpoint.getKryo();
    kryo.register(byte[].class);
    kryo.register(Integer.class);
  }
}
