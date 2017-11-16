package dk.alexandra.fresco.framework.network;

/**
 * Network that allows sending and receiving of bytes between the parties of an MPC computation.
 * Simple implementations will just transfer bytes over the wire.
 */
public interface Network {

  /**
   * Send data to other party with id partyId.
   *
   * @param partyId the party to send data to
   * @param data the data to send
   */
  void send(int partyId, byte[] data);

  /**
   * Blocking call that only returns once the data has been fully received.
   *
   * @param partyId the party to receive from
   * @return the data send by the given partyId through the given channel
   */
  byte[] receive(int partyId);
}
