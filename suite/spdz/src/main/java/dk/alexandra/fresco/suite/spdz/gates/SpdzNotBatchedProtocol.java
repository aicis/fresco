package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SpdzNotBatchedProtocol extends SpdzNativeProtocol<List<DRes<SInt>>> {

  private final DRes<List<DRes<SInt>>> bits;
  private List<DRes<SInt>> result;

  public SpdzNotBatchedProtocol(
      DRes<List<DRes<SInt>>> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, Network network) {
    List<DRes<SInt>> bitsOut = bits.out();
    this.result = new ArrayList<>(bitsOut.size());
    for (DRes<SInt> secretBit : bitsOut) {
      SpdzSInt left =
          SpdzKnownSIntProtocol.createKnownSpdzElement(resourcePool, BigInteger.ONE);
      SpdzSInt right = (SpdzSInt) secretBit.out();
      result.add(left.subtract(right));
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SInt>> out() {
    return result;
  }
}
