package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Class that stores data and resources common across all two-party protocols.
 */
public abstract class TwoPartyProtocol extends BaseProtocol {

  private final Integer otherId;

  public TwoPartyProtocol(MascotResourcePool resourcePool, Network network, Integer otherId) {
    super(resourcePool, network);
    this.otherId = otherId;
  }

  public Integer getOtherId() {
    return otherId;
  }
}
