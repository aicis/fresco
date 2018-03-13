package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;

/**
 * Native protocol from computing the sum of a secret value and a public constant.
 */
public class Spdz2kAddKnownProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final PlainT left;
  private final DRes<SInt> right;
  private SInt out;

  /**
   * Creates new {@link Spdz2kAddKnownProtocol}.
   *
   * @param left public summand
   * @param right secret summand
   */
  public Spdz2kAddKnownProtocol(PlainT left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    Spdz2kDataSupplier<PlainT> dataSupplier = resourcePool.getDataSupplier();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    out = toSpdz2kSInt(right).addConstant(left,
        dataSupplier.getSecretSharedKey(),
        factory.zero(),
        resourcePool.getMyId() == 1);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
