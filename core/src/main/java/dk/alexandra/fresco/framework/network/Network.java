package dk.alexandra.fresco.framework.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Network that allows sending and receiving of bytes between the parties of a MPC computation.
 * Simple implementations will just transfer bytes over the wire, one message at a time, however
 * arbitrary complicated buffering mechanism can be implemented.
 * <br/>
 * This interface includes a friendly interface for the native protocols, i.e. send/receive to all.
 */
public interface Network {

  /**
   * Send data to other party with id partyId. Ownership of the data array is transferred to
   * the network through this call. This means that the caller cannot change the byte array after
   * this call nor assume it is unchanged by the network.
   *
   * @param partyId the party to send data to
   * @param data the data to send
   */
  void send(int partyId, byte[] data);

  /**
   * Blocking call that only returns once the data has been fully received. Ownership of the
   * byte array is given to the caller.
   *
   * @param partyId the party to receive from
   * @return the data send by the given partyId through the given channel
   */
  byte[] receive(int partyId);

  /**
   * Gets the total amount of players. Used for building the default methods.
   *
   * @return the number of different parties.
   */
  int getNoOfParties();

  /**
   * Retrieves input from all players (including yourself)
   *
   * @return A list of byte buffers where the data from party 1 resides at
   *     index 0 and so forth.
   */
  default List<byte[]> receiveFromAll() {
    List<byte[]> res = new ArrayList<>();
    for (int i = 1; i <= getNoOfParties(); i++) {
      res.add(receive(i));
    }
    return res;
  }

  /**
   * Queues up a value to be send to all parties (yourself included).
   *
   * @param data The value to send to all parties
   */
  default void sendToAll(byte[] data) {
    for (int i = 1; i <= getNoOfParties(); i++) {
      send(i, data);
    }
  }

}
