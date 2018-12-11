package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;

public class SpdzSubtractProtocolKnownRight extends SpdzNativeProtocol<SInt> {

  private final DRes<SInt> left;
  private final FieldElement right;
  private SpdzSInt out;

  public SpdzSubtractProtocolKnownRight(DRes<SInt> left, FieldElement right) {
    this.left = left;
    this.right = right;
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
        SpdzKnownSIntProtocol.createKnownSpdzElement(spdzResourcePool, right);
    this.out = left.subtract(knownSpdzSInt);
    return EvaluationStatus.IS_DONE;
  }
}
