package dk.alexandra.fresco.framework.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.crypto.AES;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KryoNetNetwork implements Network {

  private List<Server> servers;

  // Map per partyId to a list of clients where the length of the list is equal to channelAmount.
  private Map<Integer, List<Client>> clients;
  private List<Thread> clientThreads;
  private NetworkConfiguration conf;

  private Map<Integer, AES> ciphers;

  // List is as long as channelAmount and contains a map for each partyId to a queue
  private List<Map<Integer, BlockingQueue<byte[]>>> queues;
  private int channelAmount;

  private boolean encryption;
  private Logger logger = LoggerFactory.getLogger(KryoNetNetwork.class);

  public KryoNetNetwork() {
    Log.set(Log.LEVEL_ERROR);
  }

  @Override
  public void init(NetworkConfiguration conf, int channelAmount) {
    if (channelAmount < 1) {
      throw new IllegalArgumentException("The number of channels must be at least 1");
    }
    this.conf = conf;
    this.channelAmount = channelAmount;
    this.clients = new HashMap<>();
    this.clientThreads = new ArrayList<>();
    this.servers = new ArrayList<>();
    this.queues = new ArrayList<>();

    this.ciphers = new HashMap<>();

    // TODO: How to reason about the upper boundries of what can be send in a single round?
    int writeBufferSize = 1048576;
    int objectBufferSize = writeBufferSize;

    for (int j = 0; j < channelAmount; j++) {
      Server server = new Server(1024, objectBufferSize);
      Registrator.register(server);
      this.servers.add(server);
      this.queues.add(new HashMap<>());
    }

    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (i != conf.getMyId()) {
        this.clients.put(i, new ArrayList<>());
        for (int j = 0; j < channelAmount; j++) {
          Client client = new Client(writeBufferSize, 1024);
          Registrator.register(client);
          clients.get(i).add(client);
        }

        String secretSharedKey = conf.getParty(i).getSecretSharedKey();
        if (secretSharedKey != null) {
          this.ciphers.put(i, new AES(secretSharedKey, writeBufferSize, new SecureRandom()));
          this.encryption = true;
          logger.warn("Encrypted channel towards Party " + i
              + " enabled - but the channel might be insecure due to not fully tested and checked use of AES.");
        }
      }

      for (int j = 0; j < channelAmount; j++) {
        this.queues.get(j).put(i, new ArrayBlockingQueue<>(1000));
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
        if (PerformanceLogger.LOG_NETWORK) {
          PerformanceLogger.getLogger(conf.getMyId()).bytesReceived(data.length, fromPartyId);
        }
        if (encryption) {
          try {
            data = ciphers.get(fromPartyId).decrypt(data);
          } catch (IOException e) {
            throw new RuntimeException("IOException occured while decrypting data stream", e);
          }
        }
        this.queue.get(fromPartyId).offer(data);
      } else if (object instanceof Integer) {
        // Initial handshake to determine who the remote party is.
        this.connectionIdToPartyId.put(connection.getID(), (Integer) object);
      }
    }
  }

  @Override
  public void connect(int timeoutMillis) throws IOException {
    final Semaphore semaphore = new Semaphore(-((conf.noOfParties() - 1) * channelAmount - 1));
    for (int j = 0; j < channelAmount; j++) {
      Server server = this.servers.get(j);
      boolean serverBound = false;
      int port = conf.getMe().getPort() + j;
      logger.debug("P" + conf.getMyId() + ": Trying to bind to " + port);
      final int maxTries = 10;
      int tries = 0;
      while (!serverBound) {
        try {
          server.bind(port);
          server.start();
          serverBound = true;
        } catch (IOException e) {
          try {
            if (tries > maxTries) {
              throw e;
            }
            tries++;
            Thread.sleep(500);
          } catch (InterruptedException e1) {
            throw e;
          }
        }
      }

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
              semaphore.release();

            }
          });

          client.start();

          String hostname = conf.getParty(i).getHostname();
          int port = conf.getParty(i).getPort() + j;
          Thread clientThread = new Thread("Connect") {
            public void run() {
              boolean success = false;
              while (!success) {
                try {
                  client.connect(2000, hostname, port);
                  // Server communication after connection can go here, or in Listener#connected().
                  success = true;
                } catch (IOException ex) {
                  try {
                    sleep(500);
                  } catch (InterruptedException e) {
                    throw new RuntimeException("Thread got interrupted while trying to reconnect.");
                  }
                }
              }
            }
          };
          clientThread.start();
          clientThreads.add(clientThread);
        }
      }
    }
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void send(int channel, int partyId, byte[] data) throws IOException {
    if (this.conf.getMyId() == partyId) {
      try {
        this.queues.get(channel).get(partyId).put(data);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else {
      if (encryption) {
        data = this.ciphers.get(partyId).encrypt(data);
      }
      this.clients.get(partyId).get(channel).sendTCP(data);
    }
  }

  @Override
  public byte[] receive(int channel, int partyId) throws IOException {
    try {
      return this.queues.get(channel).get(partyId).take();
    } catch (InterruptedException e) {
      throw new MPCException("receive got interrupted");
    }
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

  private static class Registrator {

    public static void register(EndPoint endpoint) {
      Kryo kryo = endpoint.getKryo();
      kryo.register(byte[].class);
      kryo.register(Integer.class);
    }
  }
}
