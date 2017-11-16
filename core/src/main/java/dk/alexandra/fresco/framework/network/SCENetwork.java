package dk.alexandra.fresco.framework.network;

import java.util.List;

/**
 * Network towards the protocols. This does not expose the real network, and
 * sending has no effect on the TCP layer. A higher level should handle the
 * input/output (typically the evaluator)
 */
public interface SCENetwork extends Network {

  /**
   * Retrieves input from all players (including yourself)
   *
   * @return A list of byte buffers where the data from party 1 resides at
   *     index 0 and so forth.
   */
  List<byte[]> receiveFromAll();

  /**
   * Queues up a value to be send to all parties (yourself included). Values
   * are not send by TCP by calling this method, but queued up for the higher
   * layer to send later.
   *
   * @param data The value to send to all parties
   */
  void sendToAll(byte[] data);

  /**
   * Clears the internal maps to ensure that the returned values next round is
   * correct.
   */
  void flushBuffer();

}
