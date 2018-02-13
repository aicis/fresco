package dk.alexandra.fresco.suite.marlin.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinAddProtocol<T extends BigUInt<T>> extends MarlinNativeProtocol<SInt, T> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private SInt sum;

  public MarlinAddProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool, Network network) {
    MarlinSInt<T> left = (MarlinSInt<T>) this.left.out();
    MarlinSInt<T> right = (MarlinSInt<T>) this.right.out();
    this.sum = left.add(right);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return sum;
  }

}
