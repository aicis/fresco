package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

/**
 * Native protocol for subtracting a secret value from a known public values. <p>Note that the
 * result is a secret value.</p>
 */
public class Spdz2kSubtractFromKnownProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final PlainT left;
  private final DRes<SInt> right;
  private SInt difference;

  /**
   * Creates new {@link Spdz2kSubtractFromKnownProtocol}.
   *
   * @param left plain value
   * @param right secret value to be subtracted
   */
  public Spdz2kSubtractFromKnownProtocol(PlainT left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT secretSharedKey = resourcePool.getDataSupplier().getSecretSharedKey();
    PlainT zero = resourcePool.getFactory().zero();
    Spdz2kSInt<PlainT> leftSInt = new Spdz2kSInt<>(left, secretSharedKey, zero,
        resourcePool.getMyId() == 1);
    difference = leftSInt.subtract(toSpdz2kSInt(right));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return difference;
  }

}
