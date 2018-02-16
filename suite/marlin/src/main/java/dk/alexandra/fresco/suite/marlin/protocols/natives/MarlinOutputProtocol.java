package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;
import java.util.List;

public class MarlinOutputProtocol<T extends BigUInt<T>> extends
    MarlinNativeProtocol<BigInteger, T> {

  private final DRes<SInt> share;
  private BigInteger opened;
  private MarlinSInt<T> authenticatedElement;

  public MarlinOutputProtocol(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool,
      Network network) {
    MarlinOpenedValueStore<T> openedValueStore = resourcePool.getOpenedValueStore();
    if (round == 0) {
      authenticatedElement = (MarlinSInt<T>) share.out();
      // TODO clean up--only sending lower k bits
      long low = authenticatedElement.getShare().getLow();
      network.sendToAll(ByteAndBitConverter.toByteArray(low));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<T> shares = resourcePool.getRawSerializer().deserializeList(network.receiveFromAll());
      T recombined = BigUInt.sum(shares);
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
