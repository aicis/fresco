package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;

/**
 * Native protocol for converting a public constant into a secret value.
 */
public class Spdz2kKnownSIntProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final PlainT input;
  private SInt out;

  /**
   * Creates new {@link Spdz2kKnownSIntProtocol}.
   *
   * @param input public value to input
   */
  public Spdz2kKnownSIntProtocol(PlainT input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    Spdz2kDataSupplier<PlainT> dataSupplier = resourcePool.getDataSupplier();
    boolean isPartyOne = (resourcePool.getMyId() == 1);
    out = new Spdz2kSInt<>(input, dataSupplier.getSecretSharedKey(), factory.zero(), isPartyOne);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
