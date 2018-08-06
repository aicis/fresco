package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Duration;
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
  private static final Logger logger = LoggerFactory.getLogger(SocketNetwork.class);
  private final Map<Integer, Socket> socketMap;
  private final SocketFactory socketFactory;
  private final ServerSocketFactory serverFactory;

  Connector(NetworkConfiguration conf, Duration timeout) {
    this(conf, timeout, SocketFactory.getDefault(), ServerSocketFactory.getDefault());
  }

  Connector(NetworkConfiguration conf, Duration timeout,
      SocketFactory socketFactory, ServerSocketFactory serverFactory) {
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
   * Connects a channels to each external party (i.e., parties other than this party).
   * </p>
   *
   * @param conf the configuration defining the network to connect
   * @param timeout duration to wait until timeout
   * @return a map from party ids to the associated communication channel
   */
  private Map<Integer, Socket> connectNetwork(final NetworkConfiguration conf,
      final Duration timeout) {
    Map<Integer, Socket> socketMap = new HashMap<>(conf.noOfParties());
    ExecutorService es = Executors.newFixedThreadPool(2);
    CompletionService<Map<Integer, Socket>> cs = new ExecutorCompletionService<>(es);
    cs.submit(() -> {
      return connectClient(conf);
    });
    cs.submit(() -> {
      ServerSocket serverSock = bindServer(conf);
      return connectServer(conf, serverSock);
    });
    try {
      for (int i = 0; i < 2; i++) {
        Future<Map<Integer, Socket>> f = cs.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (f == null) {
          throw new TimeoutException("Timed out");
        } else {
          socketMap.putAll(f.get());
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
          ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
          b.putInt(conf.getMyId());
          byte[] idBytes = new byte[PARTY_ID_BYTES];
          byte[] bytes = b.array();
          for (int j = 0; j < PARTY_ID_BYTES; j++) {
            idBytes[j] = bytes[Integer.BYTES - PARTY_ID_BYTES + j];
          }
          sock.getOutputStream().write(idBytes);
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
   * Binds the server to the port of this party.
   */
  private ServerSocket bindServer(final NetworkConfiguration conf) {
    if (conf.getMyId() == 1) {
      return null;
    }
    try {
      ServerSocket server = serverFactory.createServerSocket(conf.getMe().getPort());
      logger.info("P{}: bound at port {}", conf.getMyId(), conf.getMe().getPort());
      return server;
    } catch (IOException e) {
      throw new RuntimeException("Failed to bind to " + conf.getMe().getPort(), e);
    }
  }

  /**
   * Listens for connections from the opposing parties with lower id's.
   *
   * @throws IOException thrown if an {@link IOException} occurs while listening.
   */
  private Map<Integer, Socket> connectServer(final NetworkConfiguration conf,
      final ServerSocket server) throws IOException {
    Map<Integer, Socket> socketMap = new HashMap<>(conf.getMyId() - 1);
    try {
      for (int i = 1; i < conf.getMyId(); i++) {
        Socket sock = server.accept();
        int id = 0;
        for (int j = 0; j < PARTY_ID_BYTES; j++) {
          id ^= sock.getInputStream().read() << j * Byte.SIZE;
        }
        socketMap.put(id, sock);
        logger.info("P{}: accepted connection from {}", conf.getMyId(), conf.getParty(id));
        socketMap.put(id, sock);
      }
    } finally {
      if (server != null) {
        server.close();
      }
    }

    return socketMap;
  }

}
