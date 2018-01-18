package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Container class for a protocol instance of random OT extension.
 */
public class RotImpl implements Rot {
  private RotSenderImpl sender = null;
  private RotReceiverImpl receiver = null;
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
  public RotImpl(OtExtensionResourcePool resources, Network network) {
    this.resources = resources;
    this.network = network;
  }

  @Override
  public RotSender getSender() {
    if (this.sender == null) {
      CoteSender sender = new CoteSender(resources, network);
      this.sender = new RotSenderImpl(sender, resources.getCoinTossing());
    }
    return sender;
  }

  @Override
  public RotReceiver getReceiver() {
    if (this.receiver == null) {
      CoteReceiver receiver = new CoteReceiver(resources, network);
      this.receiver = new RotReceiverImpl(receiver, resources.getCoinTossing());
    }
    return receiver;
  }
}
