package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;

public class Spdz2kAddKnownProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final PlainT left;
  private final DRes<SInt> right;
  private SInt out;

  public Spdz2kAddKnownProtocol(PlainT input, DRes<SInt> right) {
    this.left = input;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    out = ((Spdz2kSInt<PlainT>) right.out()).addConstant(left, resourcePool.getMyId(),
        resourcePool.getDataSupplier().getSecretSharedKey(), resourcePool.getFactory().zero());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
