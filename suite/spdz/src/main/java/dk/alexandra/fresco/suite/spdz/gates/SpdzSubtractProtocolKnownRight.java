package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;

public class SpdzSubtractProtocolKnownRight extends SpdzNativeProtocol<SInt> {

  private final DRes<SInt> left;
  private final BigIntegerI right;
  private final BigIntegerI zero;
  private SpdzSInt out;

  public SpdzSubtractProtocolKnownRight(DRes<SInt> left, BigIntegerI right, BigIntegerI zero) {
    this.left = left;
    this.right = right;
    this.zero = zero;
  }

  @Override
  public SpdzSInt out() {
    return out;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    SpdzSInt left = (SpdzSInt) this.left.out();
    SpdzSInt knownSpdzSInt =
        SpdzKnownSIntProtocol.createKnownSpdzElement(spdzResourcePool, right, zero);
    this.out = left.subtract(knownSpdzSInt);
    return EvaluationStatus.IS_DONE;
  }
}
