package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Container class for a protocol instance of random OT extension.
 */
public class Rot {
  private RotSender sender = null;
  private RotReceiver receiver = null;
  private final OtExtensionResourcePool resources;
  private final Network network;

  /**
   * Constructs a new random OT protocol and constructs the internal sender and
   * receiver objects.
   *
   * @param resources
   *          The common resource pool for OT extension
   * @param network
   *          The network instance
   */
  public Rot(OtExtensionResourcePool resources, Network network) {
    this.resources = resources;
    this.network = network;
  }

  /**
   * Returns the sender object for the protocol.
   *
   * @return Returns the sender object for the protocol
   */
  public RotSender getSender() {
    if (this.sender == null) {
      CoteSender sender = new CoteSender(resources, network);
      this.sender = new RotSender(sender, resources.getCoinTossing());
    }
    return sender;
  }

  /**
   * Returns the receiver object for the protocol.
   *
   * @return Returns the receiver object for the protocol
   */
  public RotReceiver getReceiver() {
    if (this.receiver == null) {
      CoteReceiver receiver = new CoteReceiver(resources, network);
      this.receiver = new RotReceiver(receiver, resources.getCoinTossing());
    }
    return receiver;
  }
}
