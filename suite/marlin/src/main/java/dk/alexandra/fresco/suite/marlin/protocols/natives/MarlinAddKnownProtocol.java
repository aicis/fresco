package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinAddKnownProtocol<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends CompUInt<HighT, LowT, CompT>>
    extends MarlinNativeProtocol<SInt, HighT, LowT, CompT> {

  private final CompT left;
  private final DRes<SInt> right;
  private SInt out;

  public MarlinAddKnownProtocol(CompT input, DRes<SInt> right) {
    this.left = input;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<HighT, LowT, CompT> resourcePool,
      Network network) {
    out = ((MarlinSInt<CompT>) right).addConstant(left, resourcePool.getMyId(),
        resourcePool.getDataSupplier().getSecretSharedKey(), resourcePool.getFactory().zero());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
