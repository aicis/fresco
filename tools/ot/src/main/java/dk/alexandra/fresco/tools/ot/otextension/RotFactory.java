package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import java.util.Objects;

/**
 * Abstract factory for a protocol instance of random OT extension.
 */
public final class RotFactory {

  private final OtExtensionResourcePool resources;
  private final Network network;

  /**
   * Constructs a new random OT protocol and constructs the internal sender and receiver objects.
   *
   * @param resources The common resource pool for OT extension
   * @param network   The network instance
   */
  public RotFactory(OtExtensionResourcePool resources, Network network) {
    this.resources = Objects.requireNonNull(resources);
    this.network = Objects.requireNonNull(network);
  }

  public OtExtensionResourcePool getResources() {
    return resources;
  }

  public RotSender createSender() {
    CoteSender sender = new CoteSender(resources, network);
    return new RotSenderImpl(sender, resources, network);
  }

  public RotReceiver createReceiver() {
    CoteReceiver receiver = new CoteReceiver(resources, network);
    return new RotReceiverImpl(receiver, resources, network);
  }
}
