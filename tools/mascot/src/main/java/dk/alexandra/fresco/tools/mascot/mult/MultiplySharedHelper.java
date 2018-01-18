package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

/**
 * Class that stores data and resources common across the two sides of the multiplication helper
 * protocol, {@link MultiplyLeftHelper} and {@link MultiplyLeftHelper}.
 */
public abstract class MultiplySharedHelper extends TwoPartyProtocol {

  private final RotBatch rot;

  MultiplySharedHelper(MascotResourcePool resourcePool, Network network, Integer otherId) {
    super(resourcePool, network, otherId);
    this.rot = resourcePool.createRot(otherId, network);
  }

  RotBatch getRot() {
    return rot;
  }

}
