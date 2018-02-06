package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;

public class SpdzRandomBitProtocol extends SpdzNativeProtocol<SInt> {

  private SInt out;

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, Network network) {
    this.out = resourcePool.getStore().getSupplier().getNextBit();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }
}
