package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

/**
 * Native protocol from computing the sum of two secret values.
 */
public class Spdz2kSubtractProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private SInt out;

  /**
   * Creates new {@link Spdz2kSubtractProtocol}.
   *
   * @param left secret summand
   * @param right secret summand
   */
  public Spdz2kSubtractProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    out = toSpdz2kSInt(left).subtract(toSpdz2kSInt(right));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
