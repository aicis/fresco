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
import java.util.HashMap;
import java.util.Map;
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
 * This network functions asynchronously in that sending happens within a thread, meaning that the
 * method returns immediately after calling send. Receiving is blocking, but threads listen for
 * incoming messages and passes them to the blocking queues where the main thread is potentially
 * waiting.
 */
public class AsyncNetwork implements CloseableNetwork {

  public static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 15000;
  public static final int SHUTDOWN_DELAY_MILLIS = 500;

  private static final Logger logger = LoggerFactory.getLogger(AsyncNetwork.class);

  ServerSocketChannel server;
  Map<Integer, SocketChannel> clients;
  Map<Integer, BlockingQueue<byte[]>> queues;
  NetworkConfiguration conf;
  ExecutorService receiverService;
  Map<Integer, ExecutorService> senderServices;
  Exception sendException = null;
  Exception receiveException = null;

  /**
   * Creates a network with the given configuration and a default timeout of 15 seconds. Calling the
   * constructor will automatically trigger an attempt to connect to the other parties.
   *
   * @param conf the network configuration
   */
  public AsyncNetwork(NetworkConfiguration conf) {
    this(conf, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
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
        this.senderServices.put(i, Executors.newSingleThreadExecutor());
      }
    }
    ExecutorService es = Executors.newFixedThreadPool(2);
    CompletionService<?> cs = new ExecutorCompletionService<>(es);
    cs.submit(() -> {
      connectClient();
      return null;
    });
    cs.submit(() -> {
      bindServer();
      connectServer();
      return null;
    });
    try {
      for (int i = 0; i < 2; i++) {
        Future<?> f = cs.poll(timeout, TimeUnit.MILLISECONDS);
        if (f == null) {
          throw new TimeoutException("Timed out");
        } else {
          f.get();
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
      throw new RuntimeException("Timed out connecting network", e.getCause());
    } finally {
      es.shutdownNow();
    }
  }

  /**
   * Makes connections to the opposing parties.
   * <p>
   * The resulting connections will be used to send messages.
   * </p>
   * @throws InterruptedException thrown if interrupted while waiting to do a connection attempt
   */
  private void connectClient() throws InterruptedException {
    for (int i = 1; i <= conf.noOfParties(); i++) {
      this.queues.put(i, new LinkedBlockingQueue<byte[]>());
      if (i != conf.getMyId()) {
        Party p = conf.getParty(i);
        SocketAddress addr = new InetSocketAddress(p.getHostname(), p.getPort());
        boolean connectionMade = false;
        int attempts = 0;
        while (!connectionMade) {
          try {
            SocketChannel channel = SocketChannel.open();
            channel.connect(addr);
            this.clients.put(i, channel);
            ByteBuffer b = ByteBuffer.allocate(1);
            b.put((byte) conf.getMyId());
            b.position(0);
            channel.write(b);
            connectionMade = true;
          } catch (IOException e) {
            attempts++;
            Thread.sleep(1 << attempts);
          }
        }
      }
    }
  }

  /**
   * Listens for connections from the opposing parties.
   * <p>
   * The resulting connections will be used to receive messages.
   * </p>
   * @throws IOException thrown if an {@link IOException} occurs while listening.
   */
  void connectServer() throws IOException {
    for (int i = 0; i < getNoOfParties() - 1; i++) {
      SocketChannel channel = server.accept();
      channel.configureBlocking(true);
      ByteBuffer buf = ByteBuffer.allocate(1);
      channel.read(buf);
      buf.position(0);
      int id = buf.get();
      ServerWaiter sw = new ServerWaiter(id, channel);
      this.receiverService.submit(sw);
    }
  }

  /**
   * Binds the server to the port of this party.
   */
  private void bindServer() {
    SocketAddress sock = new InetSocketAddress(conf.getMe().getPort());
    try {
      this.server = ServerSocketChannel.open();
      this.server.bind(sock);
      logger.info("Bound at {}", sock);
    } catch (IOException e) {
      throw new RuntimeException("Failed to bind to " + sock, e);
    }
  }

  private class ServerWaiter implements Callable<Object> {

    private SocketChannel channel;
    private int fromPartyId;

    public ServerWaiter(int fromPartyId, SocketChannel channel) {
      this.channel = channel;
      this.fromPartyId = fromPartyId;
    }

    @Override
    public Object call() throws Exception {
      boolean running = true;
      while (running) {
        try {
          ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
          channel.read(buf);
          buf.flip();
          int nextMessageSize = buf.getInt();
          buf = ByteBuffer.allocate(nextMessageSize);
          while (buf.remaining() > 0) {
            channel.read(buf);
          }
          queues.get(fromPartyId).add(buf.array());
        } catch (IOException e) {
          channel.close();
          running = false;
          receiveException = e;
        }
      }
      return null;
    }

  }

  @Override
  public void send(int partyId, byte[] data) {
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
            this.clients.get(partyId).write(buf);
          }
        } catch (Exception e) {
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
    if (receiveException != null) {
      throw new RuntimeException("Previous receive operation failed due to exception",
          receiveException);
    }
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
        executorService.awaitTermination(SHUTDOWN_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
      }
      // TODO: this should be closed when all connections are accepted
      if (this.server != null) {
        this.server.close();
      }
      for (SocketChannel channel : this.clients.values()) {
        channel.close();
      }
      logger.debug("P{}: Network closed", conf.getMyId());
      return null;
    }, "Unable to properly close the network.");
  }
}
