package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

public class Spdz2kSubtractFromKnownProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final PlainT left;
  private final DRes<SInt> right;
  private SInt out;

  public Spdz2kSubtractFromKnownProtocol(PlainT input, DRes<SInt> right) {
    this.left = input;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    Spdz2kSInt<PlainT> leftSInt = new Spdz2kSInt<>(
        (resourcePool.getMyId() == 1) ? left : resourcePool.getFactory().zero(),
        left.multiply(resourcePool.getDataSupplier().getSecretSharedKey())
    );
    out = leftSInt.subtract(toSpdz2kSInt(right));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
