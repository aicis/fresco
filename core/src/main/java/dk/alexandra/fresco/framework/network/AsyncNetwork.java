package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple network implementation.
 * <p>
 * Implements non-blocking sends and blocking receives. Uses two threads per opposing party, one for
 * sending and one for receiving messages.
 * </p>
 */
public class AsyncNetwork implements CloseableNetwork {

  public static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofMinutes(1);
  private static final int PARTY_ID_BYTES = 1;
  private static final Duration RECEIVE_TIMEOUT = Duration.ofMillis(100);
  private static final Logger logger = LoggerFactory.getLogger(AsyncNetwork.class);
  private final BlockingQueue<byte[]> selfQueue;
  private final NetworkConfiguration conf;
  private boolean alive;
  private ExecutorService communicationService;
  private Collection<SocketChannel> channels;
  private final Map<Integer, Sender> senders;
  private final Map<Integer, Receiver> receivers;

  /**
   * Creates a network with the given configuration and a default timeout of
   * {@link #DEFAULT_CONNECTION_TIMEOUT}. Calling the constructor will automatically trigger an
   * attempt to connect to the other parties. If this fails a {@link RuntimeException} is thrown.
   *
   * @param conf the network configuration
   */
  public AsyncNetwork(NetworkConfiguration conf) {
    this(conf, DEFAULT_CONNECTION_TIMEOUT);
  }

  /**
   * Creates a network with the given configuration and a timeout of <code>timeout</code> in
   * milliseconds. Calling the constructor will automatically trigger an attempt to connect to the
   * other parties. If this fails a {@link RuntimeException} is thrown.
   *
   * @param conf The network configuration
   * @param timeout the time to wait until timeout
   */
  public AsyncNetwork(NetworkConfiguration conf, Duration timeout) {
    this.conf = conf;
    int externalParties = conf.noOfParties() - 1;
    this.receivers = new HashMap<>(externalParties);
    this.senders = new HashMap<>(externalParties);
    this.alive = true;
    this.selfQueue = new LinkedBlockingQueue<>();
    if (conf.noOfParties() > 1) {
      Map<Integer, SocketChannel> channelMap = connectNetwork(timeout);
      channels = channelMap.values();
      startCommunication(channelMap);
    }
    logger.info("P{}: successfully connected network", conf.getMyId());
  }

  /**
   * Fully connects the network.
   * <p>
   * Connects a channels to each external party (i.e., parties other than this party).
   * </p>
   *
   * @param timeout duration to wait until timeout
   * @return a map from party ids to the associated communication channel
   */
  private Map<Integer, SocketChannel> connectNetwork(Duration timeout) {
    Map<Integer, SocketChannel> channelMap = new HashMap<>(conf.noOfParties());
    ExecutorService es = Executors.newFixedThreadPool(2);
    CompletionService<Map<Integer, SocketChannel>> cs = new ExecutorCompletionService<>(es);
    cs.submit(() -> {
      return connectClient();
    });
    cs.submit(() -> {
      return connectServer(bindServer());
    });
    try {
      for (int i = 0; i < 2; i++) {
        Future<Map<Integer, SocketChannel>> f = cs.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (f == null) {
          throw new TimeoutException("Timed out");
        } else {
          channelMap.putAll(f.get());
        }
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while connecting network", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to connect network", e.getCause());
    } catch (TimeoutException e) {
      throw new RuntimeException("Timed out connecting network", e);
    } finally {
      es.shutdownNow();
    }
    return channelMap;
  }

  /**
   * Makes connections to the opposing parties with higher id's.
   *
   * @throws InterruptedException thrown if interrupted while waiting to do a connection attempt
   */
  private Map<Integer, SocketChannel> connectClient() throws InterruptedException {
    Map<Integer, SocketChannel> channelMap = new HashMap<>(conf.noOfParties() - conf.getMyId());
    for (int i = conf.getMyId() + 1; i <= conf.noOfParties(); i++) {
      Party p = conf.getParty(i);
      SocketAddress addr = new InetSocketAddress(p.getHostname(), p.getPort());
      boolean connectionMade = false;
      int attempts = 0;
      while (!connectionMade) {
        try {
          SocketChannel channel = SocketChannel.open();
          channel.socket().setTcpNoDelay(true);
          channel.connect(addr);
          channel.configureBlocking(true);
          ByteBuffer b = ByteBuffer.allocate(PARTY_ID_BYTES);
          b.put((byte) conf.getMyId());
          b.position(0);
          while (b.hasRemaining()) {
            channel.write(b);
          }
          connectionMade = true;
          channelMap.put(i, channel);
          logger.info("P{}: connected to {}", conf.getMyId(), p);
        } catch (IOException e) {
          Thread.sleep(1 << ++attempts);
        }
      }
    }
    return channelMap;
  }

  /**
   * Binds the server to the port of this party.
   */
  private ServerSocketChannel bindServer() {
    SocketAddress sock = new InetSocketAddress(conf.getMe().getPort());
    try {
      ServerSocketChannel server = ServerSocketChannel.open();
      server.bind(sock);
      logger.info("P{}: bound at {}", conf.getMyId(), sock);
      return server;
    } catch (IOException e) {
      throw new RuntimeException("Failed to bind to " + sock, e);
    }
  }

  /**
   * Listens for connections from the opposing parties with lower id's.
   *
   * @throws IOException thrown if an {@link IOException} occurs while listening.
   */
  private Map<Integer, SocketChannel> connectServer(ServerSocketChannel server) throws IOException {
    Map<Integer, SocketChannel> channelMap = new HashMap<>(conf.getMyId() - 1);
    try {
      for (int i = 1; i < conf.getMyId(); i++) {
        SocketChannel channel = server.accept();
        channel.socket().setTcpNoDelay(true);
        channel.configureBlocking(true);
        ByteBuffer buf = ByteBuffer.allocate(PARTY_ID_BYTES);
        while (buf.hasRemaining()) {
          channel.read(buf);
        }
        buf.position(0);
        final int id = buf.get();
        channelMap.put(id, channel);
        logger.info("P{}: accepted connection from {}", conf.getMyId(), conf.getParty(id));
        channelMap.put(id, channel);
      }
    } finally {
      server.close();
    }
    return channelMap;
  }

  /**
   * Starts communication threads to handle incoming and outgoing messages.
   *
   * @param channels a map from party ids to the associated communication channels
   */
  private void startCommunication(Map<Integer, SocketChannel> channels) {
    int externalParties = this.conf.noOfParties() - 1;
    this.communicationService = Executors.newFixedThreadPool(externalParties * 2);
    for (Entry<Integer, SocketChannel> entry : channels.entrySet()) {
      final int id = entry.getKey();
      SocketChannel channel = entry.getValue();
      Receiver receiver = new Receiver(channel, this.communicationService);
      this.receivers.put(id, receiver);
      Sender sender = new Sender(channel, this.communicationService);
      this.senders.put(id, sender);
    }
  }

  /**
   * Implements the receiver receiving a single message and starting a new receiver.
   */
  static class Receiver implements Callable<Object> {

    private final SocketChannel channel;
    private final BlockingQueue<byte[]> queue;
    private final Future<Object> future;
    private final AtomicBoolean run;

    /**
     * Create a new Receiver.
     *
     * @param channel the channel receive messages on
     * @param es the executor used to execute the receiving thread
     */
    Receiver(SocketChannel channel, ExecutorService es) {
      Objects.requireNonNull(channel);
      Objects.requireNonNull(es);
      this.channel = channel;
      this.queue = new LinkedBlockingQueue<>();
      this.run = new AtomicBoolean(true);
      this.future = es.submit(this);
    }

    /**
     * Tests if the Receiver is running. If not throws the exception that made it stop.
     *
     * @return true if the Receiver is running
     * @throws InterruptedException if an interrupt occurred
     * @throws ExecutionException if an exception occurred during execution
     */
    boolean isRunning() throws InterruptedException, ExecutionException {
      if (future.isDone()) {
        future.get();
        return false;
      }
      return true;
    }

    /**
     * Stops the receiver nicely.
     *
     * @throws ExecutionException if the sender failed due to an exception
     * @throws InterruptedException if the sender was interrupted
     * @throws IOException if exception occurs while closing channel
     */
    void stop() throws InterruptedException, ExecutionException, IOException {
      if (isRunning()) {
        run.set(false);
        channel.shutdownInput();
        try {
          future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
          future.cancel(true);
        }
//        future.cancel(true);
        //todo is cancel correct? get was blocking because partner was already closed
      }
    }

    /**
     * Polls for a message.
     *
     * @param timeout when to timeout waiting for a new message
     * @return the message
     */
    byte[] pollMessage(Duration timeout) {
      return ExceptionConverter.safe(() -> queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS),
          "Receive interrupted");
    }

    @Override
    public Object call() throws IOException, InterruptedException {
      while (run.get()) {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
        while (buf.hasRemaining() && run.get()) {
          channel.read(buf);
        }
        if (run.get()) {
          buf.flip();
          int nextMessageSize = buf.getInt();
          buf = ByteBuffer.allocate(nextMessageSize);
          while (buf.remaining() > 0) {
            channel.read(buf);
          }
          queue.add(buf.array());
        }
      }
      return null;
    }
  }

  /**
   * Implements the sender sending messages.
   */
  static class Sender implements Callable<Object> {

    private final SocketChannel channel;
    private final BlockingQueue<byte[]> queue;
    private final AtomicBoolean flush;
    private final AtomicBoolean ignoreNext;
    private Future<Object> future;

    Sender(SocketChannel channel, ExecutorService es) {
      Objects.requireNonNull(channel);
      Objects.requireNonNull(es);
      this.channel = channel;
      this.queue = new LinkedBlockingQueue<>();
      this.flush = new AtomicBoolean(false);
      this.ignoreNext = new AtomicBoolean(false);
      this.future = es.submit(this);
    }

    /**
     * Unblocks the sending thread and lets it stop nicely flushing out any outgoing messages.
     */
    private void unblock() {
      this.flush.set(true);
      if (queue.isEmpty()) {
        this.ignoreNext.set(true);
        queue.add(new byte[]{});
      }
    }

    /**
     * Queues an outgoing message.
     *
     * @param msg a message
     */
    void queueMessage(byte[] msg) {
      queue.add(msg);
    }

    /**
     * Tests if the Sender is running. If not throws the exception that made it stop.
     *
     * @return true if the Sender is running, false if it stopped nicely
     * @throws InterruptedException if an interrupt occurred
     * @throws ExecutionException if an exception occurred during execution
     */
    boolean isRunning() throws InterruptedException, ExecutionException {
      if (future.isDone()) {
        future.get();
        return false;
      } else {
        return true;
      }
    }

    /**
     * Stops the sender nicely.
     *
     * @throws ExecutionException if the sender failed due to an exception
     * @throws InterruptedException if the sender was interrupted
     * @throws IOException if exception occurs while closing channel
     */
    void stop() throws InterruptedException, ExecutionException, IOException {
      if (isRunning()) {
        unblock();
      }
      try {
        future.get(5, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
        future.cancel(true);
      }
      //todo is cancel correct? get was blocking because partner was already closed
      channel.shutdownOutput();
    }

    @Override
    public Object call() throws IOException, InterruptedException {
      while (!queue.isEmpty() || !flush.get()) {
        byte[] data = queue.take();
        if (!ignoreNext.get()) {
          ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES + data.length);
          buf.putInt(data.length);
          buf.put(data);
          buf.position(0);
          while (buf.hasRemaining()) {
            channel.write(buf);
          }
        }
      }
      return null;
    }
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (partyId == conf.getMyId()) {
      this.selfQueue.add(data);
    } else {
      inRange(partyId);
      ExceptionConverter.safe(() -> {
        if (!senders.get(partyId).isRunning()) {
          throw new RuntimeException("Sender not running");
        }
        return null;
      }, "P" + conf.getMyId() + ": Unable to send to P" + partyId);
      this.senders.get(partyId).queueMessage(data);
    }
  }

  @Override
  public byte[] receive(final int partyId) {
    if (partyId == conf.getMyId()) {
      return ExceptionConverter.safe(() -> selfQueue.take(), "Receiving from self iterrupted");
    }
    inRange(partyId);
    byte[] data = null;
    while (data == null) {
      ExceptionConverter.safe(() -> {
        if (!receivers.get(partyId).isRunning()) {
          throw new RuntimeException("Receiver not running");
        }
        return null;
      }, "P" + conf.getMyId() + ": Unable to receive from P" + partyId);
      data = receivers.get(partyId).pollMessage(RECEIVE_TIMEOUT);
    }
    return data;
  }

  /**
   * Check if a party ID is in the range of known parties.
   *
   * @param partyId an ID for a party
   */
  private void inRange(final int partyId) {
    if (!(0 < partyId && partyId < getNoOfParties() + 1)) {
      throw new IllegalArgumentException(
          "Party id " + partyId + " not in range 1 ... " + getNoOfParties());
    }
  }

  private void teardown() {
    if (alive) {
      alive = false;
      if (conf.noOfParties() < 2) {
        logger.info("P{}: Network closed", conf.getMyId());
        return;
      }
      ExceptionConverter.safe(() -> {
        closeCommunication();
        logger.info("P{}: Network closed", conf.getMyId());
        return null;
      }, "Unable to properly close the network.");
    } else {
      logger.info("P{}: Network already closed", conf.getMyId());
    }
  }

  /**
   * Safely closes the threads and channels used for sending/receiving messages.
   * Note: this should be only be called once.
   */
  private void closeCommunication() {
    for (Sender s : senders.values()) {
      try {
        s.stop();
      } catch (Exception e) {
        logger.warn("P{}: A failed sender detected while closing network", conf.getMyId());
      }
    }
    for (Receiver r : receivers.values()) {
      try {
        r.stop();
      } catch (Exception e) {
        logger.warn("P{}: A failed receiver detected while closing network", conf.getMyId());
      }
    }
    for (SocketChannel c : channels) {
      ExceptionConverter.safe(() -> {
        c.close();
        return null;
      }, "");
    }
    communicationService.shutdownNow();
  }

  /**
   * Closes the network down and releases held resources.
   */
  @Override
  public void close() {
    teardown();
  }

  @Override
  public int getNoOfParties() {
    return this.conf.noOfParties();
  }
}
