package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the sender sending messages.
 */
class Sender implements Callable<Object> {

  private final DataOutputStream out;
  private final BlockingQueue<byte[]> queue;
  private final AtomicBoolean flush;
  private final AtomicBoolean ignoreNext;
  private Future<Object> future;

  Sender(Socket sock, ExecutorService es) {
    Objects.requireNonNull(sock);
    Objects.requireNonNull(es);
    this.out = ExceptionConverter.safe(() ->
    new DataOutputStream(new BufferedOutputStream(sock.getOutputStream())),
        "Unable to get output stream from socket");
    this.queue = new LinkedBlockingQueue<>();
    this.flush = new AtomicBoolean(false);
    this.ignoreNext = new AtomicBoolean(false);
    this.future = es.submit(this);
  }

  /**
   * Unblocks the sending thread and lets it stop nicely flushing out any outgoing messages.
   */
  private void unblock() {
    this.flush.set(true);
    if (queue.isEmpty()) {
      this.ignoreNext.set(true);
      queue.add(new byte[] {});
    }
  }

  /**
   * Queues an outgoing message.
   * @param msg a message
   */
  void queueMessage(byte[] msg) {
    queue.add(msg);
  }

  /**
   * Tests if the Sender is running. If not throws the exception that made it stop.
   *
   * @return true if the Sender is running, false if it stopped nicely
   * @throws InterruptedException if an interrupt occurred
   * @throws ExecutionException if an exception occurred during execution
   */
  boolean isRunning() throws InterruptedException, ExecutionException {
    if (future.isDone()) {
      future.get();
      return false;
    } else {
      return true;
    }
  }

  /**
   * Stops the sender nicely.
   * @throws ExecutionException if the sender failed due to an exception
   * @throws InterruptedException if the sender was interrupted
   * @throws IOException if exception occurs while closing channel
   */
  void stop() throws InterruptedException, ExecutionException, IOException {
    if (isRunning()) {
      unblock();
    }
    future.get();
  }

  @Override
  public Object call() throws IOException, InterruptedException {
    while (!queue.isEmpty() || !flush.get()) {
      byte[] data = queue.take();
      if (!ignoreNext.get()) {
        out.writeInt(data.length);
        out.write(data);
        out.flush();
      }
    }
    return null;
  }

}