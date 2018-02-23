package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinKnownSIntProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends MarlinNativeProtocol<SInt, PlainT> {

  private final PlainT input;
  private SInt out;

  public MarlinKnownSIntProtocol(PlainT input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<PlainT> resourcePool,
      Network network) {
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
