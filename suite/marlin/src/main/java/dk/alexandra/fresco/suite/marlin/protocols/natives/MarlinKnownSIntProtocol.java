package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinKnownSIntProtocol<
    H extends UInt<H>,
    L extends UInt<L>,
    T extends CompUInt<H, L, T>> extends MarlinNativeProtocol<SInt, H, L, T> {

  private final T input;
  private SInt out;

  public MarlinKnownSIntProtocol(T input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<H, L, T> resourcePool,
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
