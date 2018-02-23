package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;
import java.util.List;

public class MarlinOutputToAllProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends MarlinNativeProtocol<BigInteger, PlainT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private BigInteger opened;
  private MarlinSInt<PlainT> authenticatedElement;

  public MarlinOutputToAllProtocol(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<PlainT> resourcePool,
      Network network) {
    MarlinOpenedValueStore<PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    if (round == 0) {
      authenticatedElement = (MarlinSInt<PlainT>) share.out();
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
