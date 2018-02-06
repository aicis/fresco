package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class SpdzMultProtocolKnownLeft extends SpdzNativeProtocol<SInt> {

  private BigInteger left;
  private DRes<SInt> right;
  private SpdzSInt out;

  public SpdzMultProtocolKnownLeft(BigInteger left, DRes<SInt> right) {
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
    SpdzSInt right = (SpdzSInt) this.right.out();
    out = new SpdzSInt(right.value.multiply(left));
    return EvaluationStatus.IS_DONE;
  }
}
