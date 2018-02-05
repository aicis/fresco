package dk.alexandra.fresco.suite.marlin.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;

public class MarlinInputProtocol<T extends BigUInt<T>> extends MarlinNativeProtocol<SInt, T> {

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool,
      Network network) {
    return null;
  }

  @Override
  public SInt out() {
    return null;
  }
  
}
