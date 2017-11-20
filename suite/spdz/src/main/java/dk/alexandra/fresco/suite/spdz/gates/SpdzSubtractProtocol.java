package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;

public class SpdzSubtractProtocol extends SpdzNativeProtocol<SInt> {

  private DRes<SInt> left;
  private DRes<SInt> right;
  private SpdzSInt out;

  public SpdzSubtractProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public SpdzSInt out() {
    return out;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool SpdzResourcePool,
      Network network) {
    SpdzSInt left = (SpdzSInt) this.left.out();
    SpdzSInt right = (SpdzSInt) this.right.out();
    this.out = new SpdzSInt(left.value.subtract(right.value));
    return EvaluationStatus.IS_DONE;
  }

}
