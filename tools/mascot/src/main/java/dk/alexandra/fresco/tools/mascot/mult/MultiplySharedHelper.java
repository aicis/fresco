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
  private final int numLeftFactors;

  MultiplySharedHelper(MascotResourcePool resourcePool, Network network, Integer otherId,
      int numLeftFactors) {
    super(resourcePool, network, otherId);
    this.numLeftFactors = numLeftFactors;
    this.rot = resourcePool.createRot(otherId, network);
  }

  protected RotBatch getRot() {
    return rot;
  }

  protected int getNumLeftFactors() {
    return numLeftFactors;
  }

}
