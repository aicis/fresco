package dk.alexandra.fresco.framework.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.util.TcpIdleSender;
import com.esotericsoftware.minlog.Log;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
  private final int timeout;

  // Map for each partyId to a queue
  private Map<Integer, BlockingQueue<byte[]>> queues;

  private static final Logger logger = LoggerFactory.getLogger(KryoNetNetwork.class);

  private static final int BYTE_SERIALIZATION_SIZE = 8;
  private final int maxSendAmount; // buffer size - serialization of a byte[] object.
  private final boolean allowMultipleMessages;

  /**
   * Creates a KryoNet network from the given configuration. Calling the constructor will
   * immediately trigger an attempt at connecting to the other parties.
   *
   * @param conf The configuration informing the network of the number of parties and where to
   *        connect to them.
   * @param bufferSize The maximum size of messages that we expect through the network.
   * @param allowMultipleMessages If true, the network can handle arbitrary message sizes, but comes
   *        with the cost of sending extra information over the wire for each message. If false, a
   *        buffer overflow exception will occur if one tries to send messages of a larger size than
   *        <code>bufferSize</code>.
   * @param timeout Denotes the timeout in milliseconds of when we give up on connecting to other
   *        parties.
   */
  public KryoNetNetwork(NetworkConfiguration conf, int bufferSize, boolean allowMultipleMessages,
      int timeout) {
    Log.set(Log.LEVEL_ERROR);
    this.allowMultipleMessages = allowMultipleMessages;
    this.maxSendAmount = bufferSize - BYTE_SERIALIZATION_SIZE;
    this.conf = conf;
    this.timeout = timeout;
    this.clients = new HashMap<>();
    this.clientThreads = new ArrayList<>();
    this.queues = new HashMap<>();

    int writeBufferSize = bufferSize;
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

      this.queues.put(i, new ArrayBlockingQueue<>(1000000));
    }

    try {
      connect();
    } catch (IOException e) {
      throw new RuntimeException("Failed to connect to all parties", e);
    }
  }

  /**
   * Creates a KryoNet network from the given configuration. Calling the constructor will
   * immediately trigger an attempt at connecting to the other parties. This constructor will use a
   * default value for the size of messages send over the network (524284) and does not allow
   * sending messages above this size threshold. This gives a slight boost in speed since we don't
   * have to send the expected number of messages that will appear each time. It also uses a default
   * timeout of 15 seconds. If you want to configure these yourself, instead use:
   * {@link #KryoNetNetwork(NetworkConfiguration, int, boolean)}.
   *
   * @param conf The configuration informing the network of the number of parties and where to
   *        connect to them.
   */
  public KryoNetNetwork(NetworkConfiguration conf) {
    this(conf, 1048568, false, 15000);
  }

  private static class ClientConnectThread extends Thread {

    private final Client client;
    private final String hostname;
    private final int port;
    private final Semaphore semaphore;
    private final int timeout;

    public ClientConnectThread(Client client, String hostname, int port, Semaphore semaphore,
        int timeout) {
      super("Connect");
      this.client = client;
      this.hostname = hostname;
      this.port = port;
      this.semaphore = semaphore;
      this.timeout = timeout;
    }

    @Override
    public void run() {
      boolean success = false;
      int maxRetries = 10;
      int sleepTime = this.timeout / maxRetries;
      int retries = 0;
      while (!success) {
        try {
          retries++;
          client.connect(1000, hostname, port);
          // Server communication after connection can go here, or in Listener#connected().
          success = true;
        } catch (IOException ex) {
          if (retries >= maxRetries) {
            // release to inform that this thread is done trying to connect
            logger.error("Could not connect to other party within "
                + timeout + "ms.");
            this.semaphore.release();
            break;
          }
          try {
            Thread.sleep(sleepTime);
          } catch (InterruptedException e) {
            logger.error("Client connect thread got interrupted");
            this.semaphore.release();
          }
        }
      }
    }
  }

  private class NaiveListener extends Listener {

    private final Map<Integer, BlockingQueue<byte[]>> queues;
    private final Map<Integer, ByteBuffer> temporaryLists;
    private final Map<Integer, Integer> connectionIdToPartyId;
    private final Map<Integer, Integer> messagesExpected;

    public NaiveListener(Map<Integer, BlockingQueue<byte[]>> queue) {
      this.queues = queue;
      this.temporaryLists = new HashMap<>();
      this.connectionIdToPartyId = new HashMap<>();
      this.messagesExpected = new HashMap<>();
    }

    @Override
    public void received(Connection connection, Object object) {
      // Maybe a keep alive message will be offered to the queue. - so we should ignore it.
      if (object instanceof byte[]) {
        byte[] data = (byte[]) object;
        int fromPartyId = this.connectionIdToPartyId.get(connection.getID());
        if (allowMultipleMessages) {
          Integer expected = messagesExpected.get(fromPartyId);
          if (expected == null || expected == 0) {
            // Case where the message fits in a single communication round
            this.queues.get(fromPartyId).offer(data);
            return;
          }
          // Multiple messages are needed to obtain the original large payload
          ByteBuffer tmp = this.temporaryLists.get(fromPartyId);
          tmp.put(data);
          expected--;
          messagesExpected.put(fromPartyId, expected);
          if (expected == 0) {
            // No more messages expected. Merge what we got so far into one array
            byte[] dataToReturn = new byte[tmp.capacity() - (maxSendAmount - data.length)];
            tmp.position(0);
            tmp.get(dataToReturn);
            this.queues.get(fromPartyId).offer(dataToReturn);
          }
        } else {
          this.queues.get(fromPartyId).offer(data);
        }
      } else if (object instanceof Integer) {
        int number = (Integer) object;
        messagesExpected.put(connection.getID(), number);
        temporaryLists.put(connection.getID(), ByteBuffer.allocate(maxSendAmount * number));
      } else if (object instanceof Handshake) {
        Handshake hs = (Handshake) object;
        int id = hs.id;
        this.connectionIdToPartyId.put(connection.getID(), id);
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
            Handshake hs = new Handshake();
            hs.id = conf.getMyId();
            connection.sendTCP(hs);
            successes.add(true);
            semaphore.release();
          }
        });

        client.start();

        String hostname = conf.getParty(i).getHostname();
        int clientPort = conf.getParty(i).getPort();
        Thread clientThread =
            new ClientConnectThread(client, hostname, clientPort, semaphore, timeout);
        clientThread.start();
        clientThreads.add(clientThread);
      }
    }
    try {
      semaphore.acquire();
      if (successes.size() < (conf.noOfParties() - 1)) {
        this.close();
        throw new IOException(
            "P" + conf.getMyId() + ": Could not successfully connect to all parties.");
      }
      logger.debug("P" + conf.getMyId() + ": Successfully connected to all parties!");
    } catch (InterruptedException e) {
      this.close();
      throw new IOException("Interrupted during wait for connect", e);
    }
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (this.conf.getMyId() == partyId) {
      ExceptionConverter.safe(() -> {
        this.queues.get(partyId).put(data);
        return null;
      } , () -> {this.close(); return null;}, "Send got interrupted");
    } else {
      if (allowMultipleMessages) {
        if (data.length > maxSendAmount) {
          // Will result in a buffer overflow. Splitting data
          ByteBuffer buffer = ByteBuffer.wrap(data);
          int bufferAmount = (data.length / this.maxSendAmount);
          int modRes = data.length % this.maxSendAmount;
          if (modRes > 0) {
            bufferAmount++;
          }
          this.clients.get(partyId).sendTCP(bufferAmount);
          Queue<byte[]> buffers = new LinkedList<>();
          for (int i = 0; i < bufferAmount; i++) {
            byte[] toQueue;
            if (i == (bufferAmount - 1)) {
              int size = modRes;
              if (modRes == 0) {
                // Corner case
                size = this.maxSendAmount;
              }
              toQueue = new byte[size];
              buffers.add(toQueue);
            } else {
              toQueue = new byte[this.maxSendAmount];
              buffers.add(toQueue);
            }
            buffer.get(toQueue);
          }
          Semaphore doneSending = new Semaphore(0);
          TcpIdleSender sender = new TcpIdleSender() {

            @Override
            protected Object next() {
              byte[] toSend = buffers.poll();
              if (toSend == null) {
                doneSending.release();
              } else {
                logger.debug("P" + conf.getMyId() + ": ToSend length: " + toSend.length
                    + ". Max length: " + maxSendAmount);
              }
              return toSend;
            }
          };
          this.clients.get(partyId).addListener(sender);
          ExceptionConverter.safe(() -> {
            doneSending.acquire();
            return null;
          } , () -> {this.close(); return null;}, "Interrupted while sending");
        } else {
          // Only one message is needed
          this.clients.get(partyId).sendTCP(data);
        }
      } else {
        this.clients.get(partyId).sendTCP(data);
      }
    }
  }

  @Override
  public byte[] receive(int partyId) {
    return ExceptionConverter.safe(() -> this.queues.get(partyId).take(),
        "Receive got interrupted");
  }

  @Override
  public int getNoOfParties() {
    return conf.noOfParties();
  }

  @Override
  public void close() {
    logger.debug("P" + conf.getMyId() + ": Shutting down KryoNet network");

    this.server.stop();

    for (Thread t : clientThreads) {
      t.interrupt();
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
    kryo.register(Handshake.class);
  }

  /**
   * Used as an identifier for KryoNet in order to distinguish between integers and handshakes.
   *
   */
  private static class Handshake {
    public int id;
  }
}
