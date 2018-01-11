package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Container class for a protocol instance of correlated OT extension with errors.
 * @author jot2re
 *
 */
public class Cote {
  private CoteSender sender = null;
  private CoteReceiver receiver = null;
  private final OtExtensionResourcePool resources;
  private final Network network;
  private final BristolSeedOts seedOts;
  private final int instanceId;

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
  public Cote(OtExtensionResourcePool resources, Network network,
      BristolSeedOts seedOts, int instanceId) {
    this.resources = resources;
    this.network = network;
    this.seedOts = seedOts;
    this.instanceId = instanceId;
  }

  /**
   * Returns the sender object for the protocol.
   *
   * @return Returns the sender object for the protocol
   */
  public CoteSender getSender() {
    if (sender == null) {
      this.sender = new CoteSender(resources, network, seedOts, instanceId);
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
      this.receiver = new CoteReceiver(resources, network, seedOts, instanceId);
    }
    return receiver;
  }
}
