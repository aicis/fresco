package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link CloseableNetwork} implementation based on regular the {@link Socket} interface (i.e.,
 * not using Java's nio API).
 *
 * <p>
 * Only handles the communication with the parties over a set of given sockets. I.e., delegates the
 * responsibility of creating and connecting of these sockets to the client of the class. This is to
 * allow us to decouple the communication strategy and connection strategies. Specifically, this
 * allows a client to pass secure sockets to the network such as {@link javax.net.ssl.SSLSocket}.
 * </p>
 * <p>
 * Two threads are used for each external party; one for sending and one for receiving messages.
 * Sending is non-blocking but receiving may block waiting for messages to arrive.
 * A very simple message format is used where each message is
 * prefixed by an integer indicating the byte length of the message.
 * </p>
 */
public class SocketNetwork implements CloseableNetwork {

  private static final Duration RECEIVE_TIMEOUT = Duration.ofMillis(100);
  private static final Logger logger = LoggerFactory.getLogger(SocketNetwork.class);
  private final BlockingQueue<byte[]> selfQueue;
  private final NetworkConfiguration conf;
  private boolean alive;
  private final Collection<Socket> sockets;
  private final Map<Integer, Sender> senders;
  private final Map<Integer, Receiver> receivers;

  /**
   * Creates a network with the given a configuration and a mapping from party ids to sockets.
   *
   * <p>
   * The mapping of party ids to sockets must be consistent with the network configuration. I.e.,
   * there should be exactly one mapping for each opposing party (but not for the local party).
   * Also, the sockets must be connected and open.
   * </p>
   *
   * @param conf the network configuration
   * @param socketMap a mapping from party ids to the socket to be used for communicating with
   *     the given party.
   * @throws IllegalArgumentException if {@code socketMap} and {@code conf} are inconsistent or
   *     the sockets are not open and connected.
   */
  public SocketNetwork(NetworkConfiguration conf, Map<Integer, Socket> socketMap) {
    Objects.requireNonNull(conf);
    Objects.requireNonNull(socketMap);
    for (int i = 1; i < conf.noOfParties() + 1; i++) {
      if (i == conf.getMyId()) {
        continue;
      }
      if (!socketMap.containsKey(i)) {
        throw new IllegalArgumentException("Missing socket for P" + i);
      }
      Socket s = socketMap.get(i);
      if (s.isClosed()) {
        throw new IllegalArgumentException("Closed socket for P" + i);
      }
      if (!s.isConnected()) {
        throw new IllegalArgumentException("Unconnected socket for P" + i);
      }
      ExceptionConverter.safe(() -> {
        s.setTcpNoDelay(true);
        return null;
      }, "Could not set delayless TCP connection");
    }
    this.conf = conf;
    int externalParties = conf.noOfParties() - 1;
    this.receivers = new HashMap<>(externalParties);
    this.senders = new HashMap<>(externalParties);
    this.alive = true;
    this.selfQueue = new LinkedBlockingQueue<>();
    if (conf.noOfParties() > 1) {
      this.sockets = Collections.unmodifiableCollection(new ArrayList<>(socketMap.values()));
      startCommunication(socketMap);
    } else {
      this.sockets = Collections.emptyList();
    }
  }

  /**
   * Default constructor using one minute timeout.
   *
   * @param conf the configuration to load the network from.
   */
  public SocketNetwork(NetworkConfiguration conf) {
    this(conf, Duration.of(1, ChronoUnit.MINUTES));
  }

  /**
   * Construct a socket network with a specific timeout.
   *
   * @param conf the configuration to load the network from.
   * @param timeout duration to wait until timeout when connecting the network.
   */
  public SocketNetwork(NetworkConfiguration conf, Duration timeout) {
    this(conf, new Connector(conf, timeout).getSocketMap());
  }

  /**
   * Construct a socket network with specific implementations of socket factories.
   *
   * @param conf the configuration to load the network from.
   * @param socketFactory factory for creating client sockets.
   * @param serverSocketFactory factory for creating server sockets.
   */
  public SocketNetwork(NetworkConfiguration conf, SocketFactory socketFactory,
      ServerSocketFactory serverSocketFactory) {
    this(conf, new Connector(conf, Duration.of(1, ChronoUnit.MINUTES), socketFactory,
        serverSocketFactory).getSocketMap());
  }

  /**
   * Starts communication threads to handle incoming and outgoing messages.
   *
   * @param sockets a map from party ids to the associated communication channels
   */
  private void startCommunication(Map<Integer, Socket> sockets) {
    for (Entry<Integer, Socket> entry : sockets.entrySet()) {
      final int id = entry.getKey();
      assertPartyIdInRange(id);
      Socket socket = entry.getValue();
      Receiver receiver = new Receiver(socket);
      this.receivers.put(id, receiver);
      Sender sender = new Sender(socket);
      this.senders.put(id, sender);
    }
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (partyId == conf.getMyId()) {
      this.selfQueue.add(data);
    } else {
      assertPartyIdInRange(partyId);
      if (!senders.get(partyId).isRunning()) {
        throw new RuntimeException(
            "P" + conf.getMyId() + ": Unable to send to P" + partyId + ". Sender not running");
      }
      this.senders.get(partyId).queueMessage(data);
    }
  }

  @Override
  public byte[] receive(final int partyId) {
    if (partyId == conf.getMyId()) {
      return ExceptionConverter.safe(selfQueue::take, "Receiving from self failed");
    }
    assertPartyIdInRange(partyId);
    byte[] data;
    data = receivers.get(partyId).pollMessage(RECEIVE_TIMEOUT);
    while (data == null) {
      if (!receivers.get(partyId).isRunning()) {
        throw new RuntimeException("P" + conf.getMyId() + ": Unable to recieve from P" + partyId
            + ". Receiver not running");
      }
      data = receivers.get(partyId).pollMessage(RECEIVE_TIMEOUT);
    }
    return data;
  }

  /**
   * Check if a party ID is in the range of known parties.
   *
   * @param partyId an ID for a party
   */
  private void assertPartyIdInRange(final int partyId) {
    ValidationUtils.assertValidId(partyId, getNoOfParties());
  }

  /**
   * Safely closes the threads and channels used for sending/receiving messages. Note: this should
   * be only be called once.
   */
  private void closeCommunication() {
    for (Sender s : senders.values()) {
      s.stop();
    }
    for (Receiver r : receivers.values()) {
      r.stop();
    }
    for (Socket sock : sockets) {
      try {
        sock.close();
      } catch (IOException ignored) {
      }
    }
  }

  /**
   * Closes the network down and releases held resources.
   */
  @Override
  public void close() {
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

  @Override
  public int getNoOfParties() {
    return this.conf.noOfParties();
  }
}
