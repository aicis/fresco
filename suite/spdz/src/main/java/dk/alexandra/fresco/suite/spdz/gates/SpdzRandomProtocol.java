package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;

public class SpdzRandomProtocol extends SpdzNativeProtocol<SInt> {

  private SpdzSInt randomElement;

  @Override
  public SpdzSInt out() {
    return randomElement;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    SpdzDataSupplier dataSupplier = spdzResourcePool.getDataSupplier();
    this.randomElement = dataSupplier.getNextRandomFieldElement();
    return EvaluationStatus.IS_DONE;
  }

}
