package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.ot.base.Ot;

/** 
 * Container class for a protocol instance of correlated OT extension with errors.
 * @author jot2re
 *
 */
public class Cote {
  private final CoteSender sender;
  private final CoteReceiver receiver;

  /**
   * Constructs a new correlated OT with errors protocol and constructs the
   * internal sender and receiver objects.
   * 
   * @param resources
   *          The common resource pool needed for OT extension
   * @param network
   *          The network instance
   * @param ot
   *          The OT functionality to use for seed OTs
   */
  public Cote(OtExtensionResourcePool resources, Network network, Ot ot) {
    this.sender = new CoteSender(resources, network, ot);
    this.receiver = new CoteReceiver(resources, network, ot);
  }

  /**
   * Returns the sender object for the protocol.
   * 
   * @return Returns the sender object for the protocol
   */
  public CoteSender getSender() {
    return sender;
  }

  /**
   * Returns the receiver object for the protocol.
   * 
   * @return Returns the receiver object for the protocol
   */
  public CoteReceiver getReceiver() {
    return receiver;
  }
}
