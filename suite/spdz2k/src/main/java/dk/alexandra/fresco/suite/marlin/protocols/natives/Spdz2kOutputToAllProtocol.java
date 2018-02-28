package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kOpenedValueStore;
import java.math.BigInteger;
import java.util.List;

public class Spdz2kOutputToAllProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<BigInteger, PlainT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private BigInteger opened;
  private Spdz2kSInt<PlainT> authenticatedElement;

  public Spdz2kOutputToAllProtocol(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    Spdz2kOpenedValueStore<PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    if (round == 0) {
      authenticatedElement = (Spdz2kSInt<PlainT>) share.out();
      network.sendToAll(authenticatedElement.getShare().getLeastSignificant().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<PlainT> shares = resourcePool.getRawSerializer()
          .deserializeList(network.receiveFromAll());
      PlainT recombined = UInt.sum(shares);
      openedValueStore.pushOpenedValue(authenticatedElement, recombined);
      this.opened = resourcePool.convertRepresentation(recombined);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public BigInteger out() {
    return opened;
  }

}
