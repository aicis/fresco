package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Class that stores data and resources common across all two-party protocols.
 */
public abstract class TwoPartyProtocol extends BaseProtocol {

  private final int otherId;

  public TwoPartyProtocol(MascotResourcePool resourcePool, Network network, int otherId) {
    super(resourcePool, network);
    this.otherId = otherId;
  }

  public int getOtherId() {
    return otherId;
  }
}
