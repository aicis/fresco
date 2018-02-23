package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinAddKnownProtocol<
    CompT extends CompUInt<?, ?, CompT>>
    extends MarlinNativeProtocol<SInt, CompT> {

  private final CompT left;
  private final DRes<SInt> right;
  private SInt out;

  public MarlinAddKnownProtocol(CompT input, DRes<SInt> right) {
    this.left = input;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<CompT> resourcePool,
      Network network) {
    out = ((MarlinSInt<CompT>) right.out()).addConstant(left, resourcePool.getMyId(),
        resourcePool.getDataSupplier().getSecretSharedKey(), resourcePool.getFactory().zero());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
