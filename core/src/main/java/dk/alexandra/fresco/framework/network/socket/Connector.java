package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connector implements NetworkConnector {

  public static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofMinutes(1);
  private static final int PARTY_ID_BYTES = 1;
  private static final Logger logger = LoggerFactory.getLogger(Connector.class);
  private final Map<Integer, Socket> socketMap;
  private final SocketFactory socketFactory;
  private final ServerSocketFactory serverFactory;

  Connector(NetworkConfiguration conf, Duration timeout) {
    this(conf, timeout, SocketFactory.getDefault(), ServerSocketFactory.getDefault());
  }

  Connector(NetworkConfiguration conf, Duration timeout, SocketFactory socketFactory,
      ServerSocketFactory serverFactory) {
    this.socketFactory = socketFactory;
    this.serverFactory = serverFactory;
    this.socketMap = connectNetwork(conf, timeout);
  }

  @Override
  public Map<Integer, Socket> getSocketMap() {
    return this.socketMap;
  }

  /**
   * Fully connects the network.
   * <p>
   * Connects a socket to each external party (i.e., parties other than this party).
   * </p>
   * <p>
   * The protocol for connecting the network proceeds as follows:
   *
   * Party <i>i</i> opens a server socket and listens for connections from all parties with id's
   * less than <i>i</i> (we denote these connections <i>server connections</i>). Concurrently, Party
   * <i>i</i> attempts to open connections to all parties with id's larger than <i>i</i> (we call
   * these the <i>client connections</i>). Once, a client connection is made the client sends its
   * party id in {@value #PARTY_ID_BYTES} byte to the server. The servers uses this to identify the
   * connecting party.
   *
   * Note: the above means that the party with the lowest id (i.e., party 1) will not make any
   * server connections and the party with the highest id will not make any client connections.
   * </p>
   *
   * @param conf the configuration defining the network to connect
   * @param timeout duration to wait until timeout
   * @return a map from party ids to the associated communication channel
   */
  private Map<Integer, Socket> connectNetwork(final NetworkConfiguration conf,
      final Duration timeout) {
    Map<Integer, Socket> socketMap = new HashMap<>(conf.noOfParties());
    // We use two threads. One for the client connections and one for the server connections.
    final int connectionThreads = 2;
    ExecutorService connectionExecutor = Executors.newFixedThreadPool(connectionThreads);
    // If either the client or the server thread fails we would like cancel the other as soon as
    // possible. For this purpose we use a CompletionService.
    CompletionService<Map<Integer, Socket>> connectionService =
        new ExecutorCompletionService<>(connectionExecutor);
    connectionService.submit(() -> connectClient(conf));
    connectionService.submit(() -> connectServer(conf));
    Duration remainingTime = timeout;
    try {
      Instant start = Instant.now();
      for (int i = 0; i < connectionThreads; i++) {
        remainingTime = remainingTime.minus(Duration.between(start, Instant.now()));
        Future<Map<Integer, Socket>> completed =
            connectionService.poll(remainingTime.toMillis(), TimeUnit.MILLISECONDS);
        if (completed == null) {
          throw new TimeoutException("Timed out waiting for client connections");
        } else {
          // Below, will either collect the connections made by the completed thread, or throw an
          // ExecutionException, if the thread failed while trying to make the required connections.
          socketMap.putAll(completed.get());
        }
      }
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to connect network", e.getCause());
    } catch (Exception e) {
      throw new RuntimeException("Failed to connect network", e);
    } finally {
      connectionExecutor.shutdownNow();
    }
    return socketMap;
  }

  /**
   * Makes connections to the opposing parties with higher id's.
   *
   * @throws InterruptedException if interrupted while waiting to do a connection attempt
   * @throws IOException if an IO exception occurs while connecting
   */
  private Map<Integer, Socket> connectClient(final NetworkConfiguration conf)
      throws InterruptedException, IOException {
    Map<Integer, Socket> socketMap = new HashMap<>(conf.noOfParties() - conf.getMyId());
    for (int i = conf.getMyId() + 1; i <= conf.noOfParties(); i++) {
      Party p = conf.getParty(i);
      boolean connectionMade = false;
      int attempts = 0;
      while (!connectionMade) {
        try {
          Socket sock = socketFactory.createSocket(p.getHostname(), p.getPort());
          for (int j = 0; j < PARTY_ID_BYTES; j++) {
            byte b = (byte) (conf.getMyId() >>> j * Byte.SIZE);
            sock.getOutputStream().write(b);
          }
          connectionMade = true;
          socketMap.put(i, sock);
          logger.info("P{}: connected to {}", conf.getMyId(), p);
        } catch (ConnectException e) {
          // A connect exception is expected if the opposing side is not listening for our
          // connection attempt yet. We ignore this and try again.
          Thread.sleep(1 << ++attempts);
        }
      }
    }
    return socketMap;
  }

  /**
   * Listens for connections from the opposing parties with lower id's.
   *
   * @throws IOException thrown if an {@link IOException} occurs while listening.
   */
  private Map<Integer, Socket> connectServer(final NetworkConfiguration conf) throws IOException {
    Map<Integer, Socket> socketMap = new HashMap<>(conf.getMyId() - 1);
    if (conf.getMyId() > 1) {
      ServerSocket server = serverFactory.createServerSocket(conf.getMe().getPort());
      logger.info("P{}: bound at port {}", conf.getMyId(), conf.getMe().getPort());
      try {
        for (int i = 1; i < conf.getMyId(); i++) {
          Socket sock = server.accept();
          int id = 0;
          for (int j = 0; j < PARTY_ID_BYTES; j++) {
            id ^= sock.getInputStream().read() << j * Byte.SIZE;
          }
          socketMap.put(id, sock);
          logger.info("P{}: accepted connection from P{}", conf.getMyId(), id);
          socketMap.put(id, sock);
        }
      } finally {
        if (server != null) {
          server.close();
        }
      }
    }
    return socketMap;
  }

}
