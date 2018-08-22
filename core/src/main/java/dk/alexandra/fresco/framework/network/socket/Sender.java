package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The sender sending messages.
 */
class Sender {

  private static final Logger logger = LoggerFactory.getLogger(Sender.class);
  private final DataOutputStream out;
  private final BlockingQueue<byte[]> queue;
  private final AtomicBoolean flushAndStop;
  private final AtomicBoolean ignoreNext;
  private final Thread thread;

  /**
   * Creates a new sender on a given socket. This starts a separate thread for sending queued
   * messages.
   *
   * @param sock the socket to send over
   */
  Sender(Socket sock) {
    Objects.requireNonNull(sock);
    this.out = ExceptionConverter.safe(
        () -> new DataOutputStream(new BufferedOutputStream(sock.getOutputStream())),
        "Unable to get output stream from socket");
    this.queue = new LinkedBlockingQueue<>();
    this.flushAndStop = new AtomicBoolean(false);
    this.ignoreNext = new AtomicBoolean(false);
    this.thread = new Thread(this::run);
    this.thread.setDaemon(true);
    this.thread.setName("sender-" + this.thread.getId());
    this.thread.start();
  }

  /**
   * Queues an outgoing message.
   *
   * <p>
   * Note: messages queued after a call to {@link #stop()} will be ignored
   * </p>
   *
   * @param msg a message
   */
  void queueMessage(byte[] msg) {
    queue.add(msg);
  }

  /**
   * Tests if the Sender is running.
   *
   * @return true if the Sender is running, false if it stopped.
   */
  boolean isRunning() {
    return this.thread.isAlive();
  }

  /**
   * Stops the sender nicely. This will block until all pending messages has been flushed.
   */
  void stop() {
    flushAndStop.set(true);
    if (isRunning()) {
      if (queue.isEmpty()) {
        this.ignoreNext.set(true);
        queue.add(new byte[]{});
      }
      ExceptionConverter.safe(() -> {
        this.thread.join();
        return null;
      }, "Interrupted while stopping sender");
    }
  }

  private void run() {
    try {
      while (shouldRun()) {
        byte[] data = queue.take();
        if (!ignoreNext.get()) {
          out.writeInt(data.length);
          out.write(data);
          out.flush();
        }
      }
      out.writeInt(-1);
      out.flush();
    } catch (Exception e) {
      if (shouldRun()) {
        logger.error("Sender failed unexpectedly", e);
      }
    }
  }

  private boolean shouldRun() {
    return !(flushAndStop.get() && queue.isEmpty());
  }
}
