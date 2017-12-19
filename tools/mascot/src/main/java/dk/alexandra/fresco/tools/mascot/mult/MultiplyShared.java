package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

public class MultiplyShared extends TwoPartyProtocol {

  protected RotBatch rot;
  protected int numLeftFactors;

  MultiplyShared(MascotResourcePool resourcePool, Network network, Integer otherId,
      int numLeftFactors) {
    super(resourcePool, network, otherId);
    this.numLeftFactors = numLeftFactors;
    this.rot = resourcePool.createRot(otherId, network);
  }

}
