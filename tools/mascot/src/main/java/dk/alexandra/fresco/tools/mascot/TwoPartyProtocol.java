package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;

public class TwoPartyProtocol extends BaseProtocol {

  protected final Integer otherId;

  public TwoPartyProtocol(MascotResourcePool resourcePool, Network network, Integer otherId) {
    super(resourcePool, network);
    this.otherId = otherId;
  }

}
