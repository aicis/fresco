package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

public class Spdz2kXorProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private SInt result;

  public Spdz2kXorProtocol(
      DRes<SInt> left,
      DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    Spdz2kSIntBoolean<PlainT> leftBit = resourcePool.getFactory().toSpdz2kSIntBoolean(left);
    Spdz2kSIntBoolean<PlainT> rightBit = resourcePool.getFactory().toSpdz2kSIntBoolean(right);
    result = leftBit.xor(rightBit);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return result;
  }

}
