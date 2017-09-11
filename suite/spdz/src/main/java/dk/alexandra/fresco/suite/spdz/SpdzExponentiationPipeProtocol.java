package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;

public class SpdzExponentiationPipeProtocol extends SpdzNativeProtocol<SInt[]> {

  private SInt[] result;

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, SCENetwork network) {
    this.result = resourcePool.getStore().getSupplier().getNextExpPipe();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt[] out() {
    return result;
  }
}
