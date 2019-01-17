package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.BigIntegerOInt;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SpdzAndKnownBatchedProtocol extends SpdzNativeProtocol<List<DRes<SInt>>> {

  private final DRes<List<OInt>> left;
  private final DRes<List<DRes<SInt>>> right;
  private List<DRes<SInt>> result;

  public SpdzAndKnownBatchedProtocol(
      DRes<List<OInt>> left, DRes<List<DRes<SInt>>> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, Network network) {
    List<OInt> leftOut = left.out();
    List<DRes<SInt>> rightOut = right.out();
    this.result = new ArrayList<>(leftOut.size());
    for (int i = 0; i < leftOut.size(); i++) {
      BigInteger leftEl = ((BigIntegerOInt) leftOut.get(i)).getValue();
      SpdzSInt rightEl = (SpdzSInt) rightOut.get(i).out();
      result.add(rightEl.multiply(leftEl));
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SInt>> out() {
    return result;
  }
}
