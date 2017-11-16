package dk.alexandra.fresco.framework.network;

import java.util.List;

/**
 * Network towards the protocols and the evaluators, this interface bridges the raw network
 * with the evaluators. The responsibility for this interface is to make the
 * communication on the network batched and hence throttled so evaluators behave nice
 * on the network.
 * <br/>
 * This interface would be the natural spot to wrap the communication and batch
 * communication after each round.
 * <br/>
 * This interface  also includes a slightly more friendly interface
 * for the native protocols.
 */
public interface SceNetwork extends Network {

  /**
   * Retrieves input from all players (including yourself)
   *
   * @return A list of byte buffers where the data from party 1 resides at
   *     index 0 and so forth.
   */
  List<byte[]> receiveFromAll();

  /**
   * Queues up a value to be send to all parties (yourself included).
   *
   * @param data The value to send to all parties
   */
  void sendToAll(byte[] data);

  /**
   * Flushes the internal buffers and sends the (remaining) pieces over the wire.
   */
  void flush();
}
