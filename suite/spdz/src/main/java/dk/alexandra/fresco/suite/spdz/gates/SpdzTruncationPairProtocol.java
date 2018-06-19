package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;

public class SpdzTruncationPairProtocol extends
    SpdzNativeProtocol<TruncationPair> {

  private TruncationPair pair;
  private final int d;

  public SpdzTruncationPairProtocol(int d) {
    this.d = d;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool,
      Network network) {
    pair = resourcePool.getDataSupplier().getNextTruncationPair(d);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public TruncationPair out() {
    return pair;
  }

}
