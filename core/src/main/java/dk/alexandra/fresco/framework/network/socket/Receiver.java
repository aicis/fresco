package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.net.Socket;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Rhe receiver receiving messages.
 */
class Receiver {

  private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
  private final DataInputStream in;
  private final BlockingQueue<byte[]> queue;
  private final AtomicBoolean run;
  private final Thread thread;

  /**
   * Create a new Receiver. This will start a separate thread listening for incoming messages.
   *
   * @param sock the channel receive messages on
   */
  Receiver(Socket sock) {
    Objects.requireNonNull(sock);
    this.in = ExceptionConverter.safe(
        () -> new DataInputStream(new BufferedInputStream(sock.getInputStream())),
        "Unable to get inputstream from socket.");
    this.queue = new LinkedBlockingQueue<>();
    this.run = new AtomicBoolean(true);
    this.thread = new Thread(this::run);
    this.thread.setDaemon(true);
    this.thread.setName("Receiver-" + this.thread.getId());
    this.thread.start();
  }

  /**
   * Tests if the Receiver is running.
   *
   * @return true if the Receiver is running
   */
  boolean isRunning() {
    return thread.isAlive();
  }

  /**
   * Stops the receiver nicely.
   *
   * <p>
   * Note messages received after this method is called can not be expected to be retrieved from the
   * receiver.
   * </p>
   *
   */
  void stop() {
    if (isRunning()) {
      this.run.set(false);
      this.thread.interrupt();
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

  private void run() {
    while (run.get()) {
      try {
        int length = this.in.readInt();
        if (length < 0) {
          run.set(false);
        } else {
          byte[] msgBuf = new byte[length];
          this.in.readFully(msgBuf);
          queue.add(msgBuf);
        }
      } catch (EOFException eof) {
        run.set(false);
      } catch (Exception e) {
        if (run.get()) {
          run.set(false);
          logger.error("Receiver failed unexpectedly", e);
        }
      }
    }
  }

}
