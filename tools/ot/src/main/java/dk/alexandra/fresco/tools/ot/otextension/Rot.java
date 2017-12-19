package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.ot.base.Ot;

/**
 * Container class for a protocol instance of random OT extension.
 * 
 * @author jot2re
 *
 */
public class Rot {
  private final RotSender sender;
  private final RotReceiver receiver;

  /**
   * Constructs a new random OT protocol and constructs the internal sender and
   * receiver objects.
   * 
   * @param resources
   *          The common resource pool for OT extension
   * @param network
   *          The network instance
   * @param ot
   *          The OT functionality to use for seed OTs
   */
  public Rot(OtExtensionResourcePool resources, Network network, Ot ot) {
    CoteSender sender = new CoteSender(resources, network, ot);
    CoteReceiver receiver = new CoteReceiver(resources, network, ot);
    this.sender = new RotSender(sender);
    this.receiver = new RotReceiver(receiver);
  }

  /**
   * Returns the sender object for the protocol.
   * 
   * @return Returns the sender object for the protocol
   */
  public RotSender getSender() {
    return sender;
  }

  /**
   * Returns the receiver object for the protocol.
   * 
   * @return Returns the receiver object for the protocol
   */
  public RotReceiver getReceiver() {
    return receiver;
  }
}
