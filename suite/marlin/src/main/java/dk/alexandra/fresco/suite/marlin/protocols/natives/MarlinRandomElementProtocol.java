package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinRandomElementProtocol<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends CompUInt<HighT, LowT, CompT>>
    extends MarlinNativeProtocol<SInt, HighT, LowT, CompT> {

  private SInt element;

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<HighT, LowT, CompT> resourcePool,
      Network network) {
    this.element = resourcePool.getDataSupplier().getNextRandomElementShare();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return element;
  }

}
