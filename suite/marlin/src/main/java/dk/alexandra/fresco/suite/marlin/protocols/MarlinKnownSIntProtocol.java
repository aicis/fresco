package dk.alexandra.fresco.suite.marlin.protocols;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinKnownSIntProtocol<T extends BigUInt<T>> extends MarlinNativeProtocol<SInt, T> {

  private final T input;
  private SInt out;

  public MarlinKnownSIntProtocol(T input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool, Network network) {
    out = new MarlinSInt<>(
        (resourcePool.getMyId() == 1) ? input : resourcePool.getFactory().zero(),
        input.multiply(resourcePool.getDataSupplier().getSecretSharedKey())
    );
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
