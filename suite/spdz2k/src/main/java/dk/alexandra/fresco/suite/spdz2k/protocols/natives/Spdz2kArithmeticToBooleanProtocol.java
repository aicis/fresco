package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

public class Spdz2kArithmeticToBooleanProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> arithmetic;
  private SInt bool;

  public Spdz2kArithmeticToBooleanProtocol(DRes<SInt> arithmetic) {
    this.arithmetic = arithmetic;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    Spdz2kSIntArithmetic<PlainT> value = resourcePool.getFactory()
        .toSpdz2kSIntArithmetic(arithmetic);
    bool = new Spdz2kSIntBoolean<>(
        value.getShare().toBitRep(),
        value.getMacShare().toBitRep().toArithmeticRep() // results in shift
    );
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return bool;
  }

}
