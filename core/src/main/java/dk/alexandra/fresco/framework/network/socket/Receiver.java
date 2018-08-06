package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Implements the receiver receiving messages.
 */
class Receiver {

  private final DataInputStream in;
  private final BlockingQueue<byte[]> queue;
  private final FutureTask<Object> future;
  private final AtomicBoolean run;

  /**
   * Create a new Receiver.
   *
   * @param sock the channel receive messages on
   * @param es the executor used to execute the receiving thread
   */
  Receiver(Socket sock) {
    Objects.requireNonNull(sock);
    this.in = ExceptionConverter.safe(() ->
    new DataInputStream(new BufferedInputStream(sock.getInputStream())),
        "Unable to get inputstream from socket.");
    this.queue = new LinkedBlockingQueue<>();
    this.run = new AtomicBoolean(true);
    this.future = new FutureTask<>(this::call);
    new Thread(this.future).start();
  }

  /**
   * Tests if the Receiver is running. If not throws an exception that made it stop.
   *
   * @return true if the Receiver is running
   * @throws InterruptedException if an interrupt occurred
   * @throws ExecutionException if an exception occurred during execution
   */
  boolean isRunning() throws InterruptedException, ExecutionException {
    if (future.isDone()) {
      future.get();
      return false;
    }
    return true;
  }

  /**
   * Stops the receiver nicely.
   *
   * @throws ExecutionException if the sender failed due to an exception
   * @throws InterruptedException if the sender was interrupted
   * @throws IOException if exception occurs while closing channel
   */
  void stop() throws InterruptedException, ExecutionException, IOException {
    if (isRunning()) {
      run.set(false);
      future.cancel(true);
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


  private Object call() throws IOException, InterruptedException {
    while (run.get()) {
      int length = 0;
      try {
        length = this.in.readInt();
      } catch (EOFException e) {
        run.getAndSet(false);
      }
      if (run.get()) {
        if (length < 0) {
          run.set(false);
        } else {
          byte[] msgBuf = new byte[length];
          this.in.readFully(msgBuf);
          queue.add(msgBuf);
        }
      }
    }
    return null;
  }

}
