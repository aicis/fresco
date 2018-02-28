package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;

public class Spdz2kKnownSIntProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final PlainT input;
  private SInt out;

  public Spdz2kKnownSIntProtocol(PlainT input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    out = new Spdz2kSInt<>(
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
