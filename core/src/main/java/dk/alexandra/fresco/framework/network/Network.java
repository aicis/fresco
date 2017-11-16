package dk.alexandra.fresco.framework.network;

/**
 * A player's view of a network. Should be used when sending messages between MPC parties.
 */
public interface Network {

  /**
   * Send data to other party with id partyId. This network will automatically figure out the size
   * on the receiving end.
   *
   * @param partyId the party to send data to
   * @param data the data to send
   */
  void send(int partyId, byte[] data);

  /**
   * Blocking call that only returns once the data has been fully received and deserialized.
   *
   * @param partyId the party to receive from
   * @return the data send by the given partyId through the given channel
   */
  byte[] receive(int partyId);
}
