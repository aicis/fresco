package dk.alexandra.fresco.framework.network;

import java.util.List;

/**
 * Network towards the protocols and the evalutors, this interface bridges the raw network
 * with the evaluators, so evaluators can behave nice on the network.
 * This interface holds the possibility to wrap the communication and batch
 * communication after each round. This interface  also includes a slightly more friendly interface
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
