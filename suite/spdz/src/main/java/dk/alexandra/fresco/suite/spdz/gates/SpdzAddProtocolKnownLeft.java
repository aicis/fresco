package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;

public class SpdzAddProtocolKnownLeft extends SpdzNativeProtocol<SInt> {

  private final FieldElement left;
  private final DRes<SInt> right;
  private final FieldElement zero;
  private SpdzSInt out;

  public SpdzAddProtocolKnownLeft(FieldElement left, DRes<SInt> right, FieldElement zero) {
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
    SpdzSInt left = SpdzKnownSIntProtocol.createKnownSpdzElement(spdzResourcePool,
        this.left, zero);
    SpdzSInt right = (SpdzSInt) this.right.out();
    this.out = left.add(right);
    return EvaluationStatus.IS_DONE;
  }
}
