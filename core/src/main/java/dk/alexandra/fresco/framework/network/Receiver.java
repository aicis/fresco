package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.util.ExceptionConverter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Implements the receiver receiving a single message and starting a new receiver.
 */
class Receiver implements Callable<Object> {

  private final Socket socket;
  private final BlockingQueue<byte[]> queue;
  private final Future<Object> future;
  private final AtomicBoolean run;

  /**
   * Create a new Receiver.
   *
   * @param sock the channel receive messages on
   * @param es the executor used to execute the receiving thread
   */
  Receiver(Socket sock, ExecutorService es) {
    Objects.requireNonNull(sock);
    Objects.requireNonNull(es);
    this.socket = sock;
    this.queue = new LinkedBlockingQueue<>();
    this.run = new AtomicBoolean(true);
    this.future = es.submit(this);
  }

  /**
   * Tests if the Receiver is running. If not throws the exception that made it stop.
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
      socket.shutdownInput();
      future.get();
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

  @Override
  public Object call() throws IOException, InterruptedException {
    while (run.get()) {
      byte[] lengthBuf = new byte[Integer.BYTES];
      int readBytes = 0;
      while (readBytes != Integer.BYTES) {
        InputStream in = socket.getInputStream();
        readBytes += in.read(lengthBuf, readBytes, lengthBuf.length);
      }
      if (run.get()) {
        int nextMessageSize = lengthBuf[3];
        nextMessageSize ^= lengthBuf[2] << 8;
        nextMessageSize ^= lengthBuf[1] << 16;
        nextMessageSize ^= lengthBuf[0] << 24;
        if (nextMessageSize < 0) {
          run.set(false);
        } else {
          readBytes = 0;
          byte[] msgBuf = new byte[nextMessageSize];
          while (readBytes != nextMessageSize) {
            readBytes += socket.getInputStream().read(msgBuf, readBytes, msgBuf.length - readBytes);
          }
          queue.add(msgBuf);
        }
      }
    }
    return null;
  }

}
