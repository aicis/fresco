package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;

public class CopeShared extends TwoPartyProtocol {

  protected boolean initialized;

  public CopeShared(MascotResourcePool resourcePool, Network network, Integer otherId) {
    super(resourcePool, network, otherId);
    this.initialized = false;
  }

}
