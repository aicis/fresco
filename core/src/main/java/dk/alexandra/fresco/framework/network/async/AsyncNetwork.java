package dk.alexandra.fresco.framework.network.async;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
  private static final Logger logger = LoggerFactory.getLogger(AsyncNetwork.class);

  private ServerSocketChannel server;
  private final Map<Integer, SocketChannel> channelMap;
  private final Map<Integer, BlockingQueue<byte[]>> outQueues;
  private final Map<Integer, BlockingQueue<byte[]>> inQueues;
  private final NetworkConfiguration conf;
  private ExecutorService communicationService;
  private Future<Object> sendFuture;
  private Future<Object> receiveFuture;

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
    this.outQueues = new HashMap<>(conf.noOfParties());
    this.inQueues = new HashMap<>(conf.noOfParties());
    this.channelMap = new HashMap<>(conf.noOfParties() - 1);
    for (int i = 1; i < conf.noOfParties() + 1; i++) {
      outQueues.put(i, new LinkedBlockingQueue<>());
      inQueues.put(i, new LinkedBlockingQueue<>());
    }
    if (conf.noOfParties() > 1) {
      connectNetwork(conf, timeout);
      startCommunication();
    }
    logger.info("P{} successfully connected network", conf.getMyId());
  }

  /**
   * Fully connects the network.
   * <p>
   * Connects a channels to each external party (i.e., parties other than this party).
   * </p>
   *
   * @param conf the configuration defining the network to connect
   * @param timeout duration to wait until timeout
   */
  private void connectNetwork(NetworkConfiguration conf, Duration timeout) {
    ExecutorService es = Executors.newFixedThreadPool(2);
    CompletionService<Map<Integer, SocketChannel>> cs = new ExecutorCompletionService<>(es);
    cs.submit(() -> {
      return connectClient();
    });
    cs.submit(() -> {
      bindServer();
      return connectServer();
    });
    try {
      for (int i = 0; i < 2; i++) {
        Future<Map<Integer, SocketChannel>> f = cs.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (f == null) {
          throw new TimeoutException("Timed out");
        } else {
          this.channelMap.putAll(f.get());
        }
      }
    } catch (InterruptedException e) {
      teardown();
      throw new RuntimeException("Interrupted while connecting network", e);
    } catch (ExecutionException e) {
      teardown();
      throw new RuntimeException("Failed to connect network", e.getCause());
    } catch (TimeoutException e) {
      teardown();
      throw new RuntimeException("Timed out connecting network", e);
    } finally {
      es.shutdownNow();
    }
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
          channel.connect(addr);
          channel.configureBlocking(true);
          this.channelMap.put(i, channel);
          ByteBuffer b = ByteBuffer.allocate(1);
          b.put((byte) conf.getMyId());
          b.position(0);
          channel.write(b);
          connectionMade = true;
          channelMap.put(i, channel);
          logger.info("P{} connected to {}", conf.getMyId(), p);
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
  private void bindServer() {
    SocketAddress sock = new InetSocketAddress(conf.getMe().getPort());
    try {
      this.server = ServerSocketChannel.open();
      this.server.bind(sock);
      logger.info("P{} bound at {}", conf.getMyId(), sock);
    } catch (IOException e) {
      throw new RuntimeException("Failed to bind to " + sock, e);
    }
  }

  /**
   * Listens for connections from the opposing parties with lower id's.
   *
   * @throws IOException thrown if an {@link IOException} occurs while listening.
   */
  private Map<Integer, SocketChannel> connectServer() throws IOException {
    Map<Integer, SocketChannel> channelMap = new HashMap<>(conf.getMyId() - 1);
    for (int i = 1; i < conf.getMyId(); i++) {
      SocketChannel channel = server.accept();
      channel.configureBlocking(true);
      ByteBuffer buf = ByteBuffer.allocate(1);
      channel.read(buf);
      buf.position(0);
      final int id = buf.get();
      this.channelMap.put(id, channel);
      logger.info("P{} accepted connection from {}", conf.getMyId(), conf.getParty(id));
      channelMap.put(id, channel);
    }
    server.close();
    return channelMap;
  }

  /**
   * Starts communication threads to handle incoming and outgoing messages.
   */
  private void startCommunication() {
    int externalParties = conf.noOfParties() - 1;
    this.communicationService = Executors.newFixedThreadPool(externalParties * 2);
    for (Entry<Integer, SocketChannel> entry : this.channelMap.entrySet()) {
      final int id = entry.getKey();
      SocketChannel channel = entry.getValue();
      receiveFuture = this.communicationService.submit(new Receiver(channel, inQueues.get(id)));
      sendFuture = this.communicationService.submit(new Sender(channel, outQueues.get(id)));
    }
  }

  /**
   * Implements the receiver receiving a single message and starting a new receiver.
   */
  private class Receiver implements Callable<Object> {

    private final SocketChannel channel;
    private final BlockingQueue<byte[]> queue;

    public Receiver(SocketChannel channel, BlockingQueue<byte[]> queue) {
      this.channel = channel;
      this.queue = queue;
    }

    @Override
    public Object call() throws IOException, InterruptedException {
      ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
      channel.read(buf);
      buf.flip();
      int nextMessageSize = buf.getInt();
      buf = ByteBuffer.allocate(nextMessageSize);
      while (buf.remaining() > 0) {
        channel.read(buf);
      }
      queue.offer(buf.array());
      receiveFuture = communicationService.submit(new Receiver(channel, queue));
      return null;
    }

  }

  /**
   * Implements the sender sending a single message and starting a new sender.
   */
  private class Sender implements Callable<Object> {

    private final SocketChannel channel;
    private final BlockingQueue<byte[]> queue;

    public Sender(SocketChannel channel, BlockingQueue<byte[]> queue) {
      this.channel = channel;
      this.queue = queue;
    }

    @Override
    public Object call() throws IOException, InterruptedException {
      byte[] data = queue.take();
      ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES + data.length);
      buf.putInt(data.length);
      buf.put(data);
      buf.position(0);
      while (buf.hasRemaining()) {
        channel.write(buf);
      }
      sendFuture = communicationService.submit(new Sender(channel, queue));
      return null;
    }

  }

  @Override
  public void send(int partyId, byte[] data) {
    inRange(partyId);
    Future<?> temp = sendFuture;
    if (conf.noOfParties() != 1 && temp.isDone()) {
      ExceptionConverter.safe(() -> temp.get(), "A previous send operation failed");
    }
    if (partyId == conf.getMyId()) {
      this.inQueues.get(partyId).add(data);
    } else {
      this.outQueues.get(partyId).offer(data);
    }
  }

  @Override
  public byte[] receive(int partyId) {
    inRange(partyId);
    Future<?> temp = receiveFuture;
    if (conf.noOfParties() != 1 && temp.isDone()) {
      ExceptionConverter.safe(() -> temp.get(), "A previous receive operation failed");
    }
    return ExceptionConverter.safe(() -> this.inQueues.get(partyId).take(), "Receive interrupted");
  }

  /**
   * Check if a party ID is in the range of known parties.
   *
   * @param partyId an ID for a party
   */
  private void inRange(int partyId) {
    if (!(0 < partyId && partyId < getNoOfParties() + 1)) {
      throw new IllegalArgumentException(
          "Party id " + partyId + " not in range 1 ... " + getNoOfParties());
    }
  }

  private void teardown() {
    if (conf.noOfParties() < 2) {
      logger.info("P{}: Network closed", conf.getMyId());
      return;
    }
    ExceptionConverter.safe(() -> {
      if (this.communicationService != null) {
        this.communicationService.shutdownNow();
      }
      if (this.server != null) {
        this.server.close();
      }
      for (SocketChannel channel : this.channelMap.values()) {
        channel.close();
      }
      return null;
    }, "Unable to properly close the network.");
    logger.info("P{}: Network closed", conf.getMyId());
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
