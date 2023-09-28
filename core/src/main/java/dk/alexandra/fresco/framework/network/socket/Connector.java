package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

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

    try (ServerSocket server = serverFactory.createServerSocket(conf.getMe().getPort())) {
      server.setSoTimeout((int) timeout.toMillis());
      Future<Map<Integer, Socket>> clients = connectionExecutor.submit(() -> connectClient(conf));
      Future<Map<Integer, Socket>> servers = connectionExecutor.submit(() -> connectServer(server, conf));
      connectionExecutor.shutdown();
      boolean termination = connectionExecutor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!termination) {
        throw new IOException("Timed out waiting for client connections");
      }
      socketMap.putAll(servers.get());
      socketMap.putAll(clients.get());
    } catch (ExecutionException | IOException | InterruptedException e) {
      closeSocketMap(socketMap);
      // These two exceptions are expected in this form for some tests, they might break things if they change (maybe).
      if (e instanceof ExecutionException || e instanceof IOException) {
        // HandshakeExceptions need to be explicit.
        if (e.getCause() instanceof SSLHandshakeException) {
          throw new RuntimeException("Failed to connect network", e.getCause());
        } else {
          throw new RuntimeException("Failed to connect network", e);
        }
      }
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
    try {
      for (int i = conf.getMyId() + 1; i <= conf.noOfParties(); i++) {
        if (Thread.interrupted()) {
          closeSocketMap(socketMap);
          break;
        }
        // TODO: Split this up into N async tasks instead
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
            // A ConnectionException is expected if the opposing side is not listening for our
            // connection attempt yet. We ignore this and try again.
            Thread.sleep(Math.min(1 << attempts++, 5_000));
            // This should probably not busy-wait for each party
          }
        }
      }
    }
    catch (Exception e) {
      closeSocketMap(socketMap);
      throw e;
    }
    return socketMap;
  }

  /**
   * Listens for connections from the opposing parties with lower id's.
   */
  private Map<Integer, Socket> connectServer(ServerSocket server, final NetworkConfiguration conf) {
    Map<Integer, Socket> socketMap = new HashMap<>(conf.getMyId() - 1);
    if (conf.getMyId() > 1) {
      try {
        logger.info("P{}: bound at port {}", conf.getMyId(), conf.getMe().getPort());
        for (int i = 1; i < conf.getMyId(); i++) {
          if (Thread.interrupted()) {
            closeSocketMap(socketMap);
            break;
          }
          Socket sock = server.accept();
          int id = 0;
          for (int j = 0; j < PARTY_ID_BYTES; j++) {
            id ^= sock.getInputStream().read() << j * Byte.SIZE;
          }
          socketMap.put(id, sock);
          logger.info("P{}: accepted connection from P{}", conf.getMyId(), id);
          socketMap.put(id, sock);
        }
      } catch (IOException e) {
        logger.info(e.getMessage());
      }
    }
    return socketMap;
  }

  static void closeSocketMap(Map<Integer, Socket> map) {
    for (Socket s : map.values()) {
      try {
        s.close();
      } catch (IOException ignored) {}
    }
  }
}
