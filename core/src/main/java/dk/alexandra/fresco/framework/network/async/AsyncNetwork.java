package dk.alexandra.fresco.framework.network.async;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This network functions asynchronously in that sending happens within a thread, meaning that the
 * method returns immediately after calling send. Receiving is blocking, but threads listen for
 * incoming messages and passes them to the blocking queues where the main thread is potentially
 * waiting.
 */
public class AsyncNetwork implements Network, Closeable {

  private static final Logger logger = LoggerFactory.getLogger(AsyncNetwork.class);

  ServerSocketChannel server;
  Map<Integer, SocketChannel> clients;
  Map<Integer, BlockingQueue<byte[]>> queues;
  NetworkConfiguration conf;
  ExecutorService receiverService;
  Map<Integer, ExecutorService> senderServices;

  /**
   * Creates a network with the given configuration and a default timeout of 15 seconds. Calling the
   * constructor will automatically trigger an attempt to connect to the other parties.
   *
   * @param conf The network configuration
   */
  public AsyncNetwork(NetworkConfiguration conf) {
    this(conf, 15000);
  }

  /**
   * Creates a network with the given configuration and a timeout of <code>timeout</code> in
   * milliseconds. Calling the constructor will automatically trigger an attempt to connect to the
   * other parties.
   *
   * @param conf The network configuration
   * @param timeout timeout in milliseconds
   */
  public AsyncNetwork(NetworkConfiguration conf, int timeout) {
    this.conf = conf;
    this.clients = new HashMap<>();
    this.queues = new HashMap<>();
    int fixedSize = Math.max(1, getNoOfParties() - 1);
    this.receiverService = Executors.newFixedThreadPool(fixedSize);
    this.senderServices = new HashMap<>();
    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (this.conf.getMyId() != i) {
        this.senderServices.put(i, Executors.newFixedThreadPool(1));
      }
    }
    SocketAddress sock = new InetSocketAddress(conf.getParty(conf.getMyId()).getPort());
    try {
      this.server = ServerSocketChannel.open();
      this.server.bind(sock);
      logger.info("Bound at {}", sock);
    } catch (IOException e) {
      throw new RuntimeException("Failed to bind to " + sock, e);
    }
    for (int i = 1; i <= conf.noOfParties(); i++) {
      this.queues.put(i, new ArrayBlockingQueue<>(1000000));
      if (i != conf.getMyId()) {
        Party p = conf.getParty(i);
        SocketAddress addr = new InetSocketAddress(p.getHostname(), p.getPort());
        try {
          int retries;
          int milliSecondsBetween;
          if (timeout < 500) {
            retries = 1;
            milliSecondsBetween = timeout;
          } else {
            retries = timeout / 500;
            milliSecondsBetween = 500;
          }
          int count = 0;
          boolean connected = false;
          while (!connected) {
            try {
              count++;
              SocketChannel channel = SocketChannel.open();
              channel.connect(addr);
              connected = true;
              this.clients.put(i, channel);
            } catch (IOException ex) {
              if (count > retries) {
                throw ex;
              }
              try {
                Thread.sleep(milliSecondsBetween);
              } catch (InterruptedException e) {
                close();
                throw new RuntimeException("Got interrupted while trying to connect.");
              }
            }
          }
        } catch (IOException e) {
          close();
          throw new RuntimeException("Could not open a connection to the addr " + addr, e);
        }
      }
    }

    handshake();
  }

  private class ServerWaiter implements Runnable {

    private SocketChannel channel;
    private int fromPartyId;

    public ServerWaiter(int fromPartyId, SocketChannel channel) {
      this.channel = channel;
      // TODO: Important? Maybe it doesn't matter?
      // ExceptionConverter.safe(() -> this.channel.configureBlocking(true),
      // "Could not configure channel to blocking");
      this.fromPartyId = fromPartyId;
    }

    @Override
    public void run() {
      boolean running = true;
      while (running) {
        try {
          ByteBuffer buf = ByteBuffer.allocate(4);
          channel.read(buf);
          buf.flip();
          int nextMessageSize = buf.getInt();
          buf = ByteBuffer.allocate(nextMessageSize);
          while (buf.remaining() > 0) {
            channel.read(buf);
          }
          queues.get(fromPartyId).add(buf.array());
        } catch (IOException e) {
          running = false;
        }
      }
    }

  }

  private class Handshaker implements Runnable {
    private ServerSocketChannel server;
    private Map<Integer, SocketChannel> partyIdToChannel;
    private Semaphore blocker;
    private ConcurrentLinkedDeque<Boolean> successes;

    public Handshaker(ServerSocketChannel server, Map<Integer, SocketChannel> partyIdToChannel,
        Semaphore blocker, ConcurrentLinkedDeque<Boolean> successes) {
      this.server = server;
      this.partyIdToChannel = partyIdToChannel;
      this.blocker = blocker;
      this.successes = successes;
    }

    @Override
    public void run() {
      int count = 0;
      while (count < getNoOfParties() - 1) {
        try {
          SocketChannel channel = server.accept();
          channel.configureBlocking(true);
          ByteBuffer buf = ByteBuffer.allocate(1);
          channel.read(buf);
          buf.position(0);
          int id = buf.get();
          partyIdToChannel.put(id, channel);
          successes.add(true);
          blocker.release();
          count++;
        } catch (IOException e) {
          logger.error("IOException occured during handshake", e);
          count++;
          successes.add(false);
          blocker.release();
        }
      }
    }
  }

  /**
   * Communicate with all parties and obtain their ID. This is required since we use a dual
   * connection strategy which means all parties serve as both client and server. Server just
   * accepts incoming connections, and we need to map the connection to a party. Malicious parties
   * could send an incorrect ID, which would overwrite an honest party. This, however, leads to a
   * null pointer exception as soon as we try to communicate with a missing partyID. Can still lead
   * to leaking information. Cannot be fixed without authenticated channels.
   */
  void handshake() {
    ConcurrentHashMap<Integer, SocketChannel> partyIdToChannel = new ConcurrentHashMap<>();
    Semaphore blocker = new Semaphore(-getNoOfParties() + 2);
    ConcurrentLinkedDeque<Boolean> successes = new ConcurrentLinkedDeque<>();
    Thread t = new Thread(new Handshaker(this.server, partyIdToChannel, blocker, successes));
    t.start();
    for (SocketChannel channel : this.clients.values()) {
      ByteBuffer id = ByteBuffer.allocate(1);
      id.put((byte) this.conf.getMyId());
      id.position(0);
      ExceptionConverter.safe(() -> channel.write(id), "Not able to send my id");
    }
    ExceptionConverter.safe(() -> {
      blocker.acquire();
      return null;
    }, "Blocker got interrupted");
    boolean connected = true;
    for (Boolean succ : successes) {
      if (!succ) {
        connected = false;
      }
    }
    if (!connected) {
      logger.error("Could not connect to all parties");
      close();
      throw new RuntimeException("Failed to connect to all parties");
    }
    logger.info("Connected to all parties. PartyId to channel map: {}", partyIdToChannel);
    Enumeration<Integer> keys = partyIdToChannel.keys();
    while (keys.hasMoreElements()) {
      int partyId = keys.nextElement();
      SocketChannel channel = partyIdToChannel.get(partyId);
      this.receiverService.submit(new ServerWaiter(partyId, channel));
    }
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (this.conf.getMyId() == partyId) {
      this.queues.get(conf.getMyId()).offer(data);
    } else {
      this.senderServices.get(partyId).submit(() -> {
        ExceptionConverter.safe(() -> {
          ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
          //set length
          buf.putInt(data.length);
          //set data
          buf.put(data);
          buf.position(0);
          while (buf.hasRemaining()) {
            this.clients.get(partyId).write(buf);
          }
          return null;
        }, "Could not send data");
      });
    }
  }

  @Override
  public byte[] receive(int partyId) {
    return ExceptionConverter.safe(() -> {
      return this.queues.get(partyId).take();
    }, "Thread got interrupted while waiting for input");
  }

  @Override
  public int getNoOfParties() {
    return this.conf.noOfParties();
  }

  /**
   * Sends the shutdown signal to all internal threads, then awaits termination for half a second by
   * default for each party before the network is closed. This is to allow for sending of bytes to
   * be processed before closing the connection. In practice, the maximum amount of waiting time
   * will never occur.
   */
  @Override
  public void close() {
    ExceptionConverter.safe(() -> {
      this.receiverService.shutdownNow();
      for (ExecutorService executorService : this.senderServices.values()) {
        executorService.shutdown();
        // REVIEW: Please try to avoid literal constants
        executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
      }
      this.server.close();
      for (SocketChannel channel : this.clients.values()) {
        channel.close();
      }
      logger.debug("P{}: Network closed", conf.getMyId());
      return null;
    }, "Could not close the network");
  }
}
