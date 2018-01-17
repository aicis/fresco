package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Container class for a protocol instance of correlated OT extension with errors.
 */
public class Cote {
  private CoteSender sender = null;
  private CoteReceiver receiver = null;
  private final OtExtensionResourcePool resources;
  private final Network network;

  /**
   * Constructs a new correlated OT with errors protocol and constructs the
   * internal sender and receiver objects.
   *
   * @param resources
   *          The common resource pool needed for OT extension
   * @param network
   *          The network instance
   */
  public Cote(OtExtensionResourcePool resources, Network network) {
    this.resources = resources;
    this.network = network;
  }

  /**
   * Returns the sender object for the protocol.
   *
   * @return Returns the sender object for the protocol
   */
  public CoteSender getSender() {
    if (sender == null) {
      this.sender = new CoteSender(resources, network);
    }
    return sender;
  }

  /**
   * Returns the receiver object for the protocol.
   *
   * @return Returns the receiver object for the protocol
   */
  public CoteReceiver getReceiver() {
    if (receiver == null) {
      this.receiver = new CoteReceiver(resources, network);
    }
    return receiver;
  }
}
