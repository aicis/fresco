package dk.alexandra.fresco.tools.ot.otextension;

/**
 * Factory for getting a protocol instance of random OT extension.
 */
public interface Rot {

  /**
   * Returns the sender object for the protocol.
   *
   * @return Returns the sender object for the protocol
   */
  RotSender getSender();

  /**
   * Returns the receiver object for the protocol.
   *
   * @return Returns the receiver object for the protocol
   */
  RotReceiver getReceiver();
}
