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
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
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
  public static final Duration SHUTDOWN_DELAY = Duration.ofSeconds(1);
  private static final Logger logger = LoggerFactory.getLogger(AsyncNetwork.class);

  private ServerSocketChannel server;
  private Map<Integer, SocketChannel> channelMap;
  private Map<Integer, BlockingQueue<byte[]>> queues;
  private NetworkConfiguration conf;
  private ExecutorService receiverService;
  private Map<Integer, ExecutorService> senderServices;
  private Exception sendException = null;
  private Exception receiveException = null;

  /**
   * Creates a network with the given configuration and a default timeout of 15 seconds. Calling the
   * constructor will automatically trigger an attempt to connect to the other parties.
   *
   * @param conf the network configuration
   */
  public AsyncNetwork(NetworkConfiguration conf) {
    this(conf, DEFAULT_CONNECTION_TIMEOUT);
  }

  /**
   * Creates a network with the given configuration and a timeout of <code>timeout</code> in
   * milliseconds. Calling the constructor will automatically trigger an attempt to connect to the
   * other parties.
   *
   * @param conf The network configuration
   * @param timeout the time to wait until timeout
   */
  public AsyncNetwork(NetworkConfiguration conf, Duration timeout) {
    this.conf = conf;
    this.queues = new HashMap<>(conf.noOfParties());
    this.channelMap = new ConcurrentHashMap<>(conf.noOfParties() - 1);
    for (int i = 1; i < conf.noOfParties() + 1; i++) {
      queues.put(i, new LinkedBlockingQueue<>());
    }
    if (conf.noOfParties() > 1) {
      connectNetwork(conf, timeout);
      startReceivers();
    }
    logger.info("P{} successfully connected network", conf.getMyId());
  }

  /**
   * Fully connects the network.
   * <p>
   * Connects two channels to each external party (i.e., parties other than this party). One channel
   * to send and one channel to receive messages.
   * </p>
   *
   * @param conf the configuration defining the network to connect
   * @param timeout duration to wait until timeout
   */
  private void connectNetwork(NetworkConfiguration conf, Duration timeout) {
    int externalParties = getNoOfParties() - 1;
    this.receiverService = Executors.newFixedThreadPool(externalParties);
    this.senderServices = new HashMap<>(externalParties);
    for (int i = 1; i <= conf.noOfParties(); i++) {
      if (this.conf.getMyId() != i) {
        this.senderServices.put(i, Executors.newSingleThreadExecutor());
      }
    }
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
      close();
      throw new RuntimeException("Interrupted while connecting network", e);
    } catch (ExecutionException e) {
      close();
      throw new RuntimeException("Failed to connect network", e.getCause());
    } catch (TimeoutException e) {
      close();
      throw new RuntimeException("Timed out connecting network", e);
    } finally {
      es.shutdownNow();
    }
  }

  private void startReceivers() {
    for (Entry<Integer, SocketChannel> entry : this.channelMap.entrySet()) {
      final int id = entry.getKey();
      SocketChannel channel = entry.getValue();
      this.receiverService.submit(() -> {
        try {
          while (true) {
            ByteBuffer inBuf = ByteBuffer.allocate(Integer.BYTES);
            channel.read(inBuf);
            inBuf.flip();
            int nextMessageSize = inBuf.getInt();
            inBuf = ByteBuffer.allocate(nextMessageSize);
            while (inBuf.remaining() > 0) {
              channel.read(inBuf);
            }
            queues.get(id).put(inBuf.array());
          }
        } catch (IOException e) {
          receiveException = e;
          throw e;
        } finally {
          channel.close();
        }
      });
    }
  }

  /**
   * Makes connections to the opposing parties.
   * <p>
   * The resulting connections will be used to send messages.
   * </p>
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
          attempts++;
          Thread.sleep(1 << attempts);
        }
      }
    }
    return channelMap;
  }


  /**
   * Listens for connections from the opposing parties.
   * <p>
   * The resulting connections will be used to receive messages.
   * </p>
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

  //

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

  @Override
  public void send(int partyId, byte[] data) {
    if (!inRange(partyId)) {
      throw new IllegalArgumentException(
          "Party id " + partyId + " not in range 1 ... " + getNoOfParties());
    }
    if (this.conf.getMyId() == partyId) {
      this.queues.get(conf.getMyId()).offer(data);
    } else {
      this.senderServices.get(partyId).submit(() -> {
        try {
          ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES + data.length);
          buf.putInt(data.length);
          buf.put(data);
          buf.position(0);
          while (buf.hasRemaining()) {
            this.channelMap.get(partyId).write(buf);
          }
        } catch (Exception e) {
          e.printStackTrace();
          this.sendException = e;
        }
        return null;
      });
      if (sendException != null) {
        throw new RuntimeException("Previous send operation failed due to exception",
            sendException);
      }
    }
  }

  @Override
  public byte[] receive(int partyId) {
    if (!inRange(partyId)) {
      throw new IllegalArgumentException(
          "Party id " + partyId + " not in range 1 ... " + getNoOfParties());
    }
    if (receiveException != null) {
      throw new RuntimeException("Previous receive operation failed due to exception",
          receiveException);
    }
    return ExceptionConverter.safe(() -> {
      return this.queues.get(partyId).take();
    }, "Thread got interrupted while waiting for input");

  }

  /**
   * Check if a party ID is in the range of known parties.
   *
   * @param partyId an ID for a party
   * @return whether or not the ID is in the range of parties
   */
  private boolean inRange(int partyId) {
    return (0 < partyId && partyId < getNoOfParties() + 1);
  }

  /**
   * Closes the network down and releases held resources.
   * <p>
   * May wait up to {@link AsyncNetwork#SHUTDOWN_DELAY} to let the network process any pending in or
   * out bound messages.
   * </p>
   */
  @Override
  public void close() {
    if (getNoOfParties() < 2) {
      logger.info("P{}: Network closed", conf.getMyId());
      return;
    }
    ExceptionConverter.safe(() -> {
      this.receiverService.shutdownNow();
      for (ExecutorService executorService : this.senderServices.values()) {
        executorService.shutdown();
        executorService.awaitTermination(SHUTDOWN_DELAY.toMillis(), TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
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

  @Override
  public int getNoOfParties() {
    return this.conf.noOfParties();
  }
}
