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

public class MarlinOutputProtocol<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> extends
    MarlinNativeProtocol<BigInteger, H, L, T> {

  private final DRes<SInt> share;
  private BigInteger opened;
  private MarlinSInt<H, L, T> authenticatedElement;

  public MarlinOutputProtocol(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<H, L, T> resourcePool,
      Network network) {
    MarlinOpenedValueStore<H, L, T> openedValueStore = resourcePool.getOpenedValueStore();
    if (round == 0) {
      authenticatedElement = (MarlinSInt<H, L, T>) share.out();
      // TODO figure out serializer
      network.sendToAll(authenticatedElement.getShare().getLow().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<T> shares = resourcePool.getRawSerializer().deserializeList(network.receiveFromAll());
      T recombined = UInt.sum(shares);
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
