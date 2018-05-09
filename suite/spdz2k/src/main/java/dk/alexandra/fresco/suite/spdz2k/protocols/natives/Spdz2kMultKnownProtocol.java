package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

/**
 * Native protocol from computing the product of a secret value and a public constant.
 */
public class Spdz2kMultKnownProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<OInt> left;
  private final DRes<SInt> right;
  private SInt out;

  /**
   * Creates new {@link Spdz2kMultKnownProtocol}.
   *
   * @param left public factor
   * @param right secret factor
   */
  public Spdz2kMultKnownProtocol(DRes<OInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    out = factory.toSpdz2kSIntArithmetic(right).multiply(factory.fromOInt(left));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
