package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketNetwork implements CloseableNetwork {

  private static final Duration RECEIVE_TIMEOUT = Duration.ofMillis(100);
  private static final Logger logger = LoggerFactory.getLogger(SocketNetwork.class);
  private final BlockingQueue<byte[]> selfQueue;
  private final NetworkConfiguration conf;
  private boolean alive;
  private ExecutorService communicationService;
  private Collection<Socket> sockets;
  private final Map<Integer, Sender> senders;
  private final Map<Integer, Receiver> receivers;

  /**
   * Creates a network with the given configuration Calling the constructor will automatically
   * trigger an attempt to connect to the other parties. If this fails a {@link RuntimeException} is
   * thrown.
   *
   * @param conf the network configuration
   */
  public SocketNetwork(NetworkConfiguration conf, Map<Integer, Socket> socketMap) {
    this.conf = conf;
    int externalParties = conf.noOfParties() - 1;
    this.receivers = new HashMap<>(externalParties);
    this.senders = new HashMap<>(externalParties);
    this.alive = true;
    this.selfQueue = new LinkedBlockingQueue<>();
    if (conf.noOfParties() > 1) {
      sockets = socketMap.values();
      startCommunication(socketMap);
    }
    logger.info("P{}: successfully connected network", conf.getMyId());
  }

  /**
   * Starts communication threads to handle incoming and outgoing messages.
   *
   * @param channels a map from party ids to the associated communication channels
   */
  private void startCommunication(Map<Integer, Socket> sockets) {
    int externalParties = this.conf.noOfParties() - 1;
    this.communicationService = Executors.newFixedThreadPool(externalParties * 2);
    for (Entry<Integer, Socket> entry : sockets.entrySet()) {
      final int id = entry.getKey();
      Socket socket = entry.getValue();
      Receiver receiver = new Receiver(socket, this.communicationService);
      this.receivers.put(id, receiver);
      Sender sender = new Sender(socket, this.communicationService);
      this.senders.put(id, sender);
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
      return ExceptionConverter.safe(() -> selfQueue.take(), "Receiving from self failed");
    }
    inRange(partyId);
    byte[] data = null;
    data = receivers.get(partyId).pollMessage(RECEIVE_TIMEOUT);
    while (data == null) {
      ExceptionConverter.safe(() -> {
        if (!receivers.get(partyId).isRunning()) {
          throw new RuntimeException("Receiver not running");
        }
        return null;
      }, "P" + conf.getMyId() + ": Unable to recieve from P" + partyId);
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
   * Safely closes the threads and channels used for sending/receiving messages. Note: this should
   * be only be called once.
   */
  private void closeCommunication() {
    for (Sender s : senders.values()) {
      try {
        s.stop();
      } catch (Exception e) {
        logger.debug("P{}: A failed sender detected while closing network", conf.getMyId(), e);
      }
    }
    for (Receiver r : receivers.values()) {
      try {
        r.stop();
      } catch (Exception e) {
        logger.debug("P{}: A failed receiver detected while closing network", conf.getMyId(), e);
      }
    }
    for (Socket c : sockets) {
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
