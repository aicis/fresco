package dk.alexandra.fresco.suite.marlin.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;
import java.util.List;

public class MarlinOutputProtocol<T extends BigUInt<T>> extends
    MarlinNativeProtocol<BigInteger, T> {

  private final DRes<SInt> share;
  private BigInteger opened;

  public MarlinOutputProtocol(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool,
      Network network) {
    MarlinOpenedValueStore<T> openedValueStore = resourcePool.getOpenedValueStore();
    ByteSerializer<T> serializer = resourcePool.getRawSerializer();
    if (round == 0) {
      MarlinSInt<T> out = (MarlinSInt<T>) share.out();
      network.sendToAll(serializer.serialize(out.getValue().getShare()));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<T> shares = serializer.deserializeList(network.receiveFromAll());
      T openedNotConverted = BigUInt.sum(shares);
      openedValueStore.pushOpenedValue(
          ((MarlinSInt<T>) share.out()).getValue(),
          openedNotConverted);
      // TODO make sure this still works
      this.opened = resourcePool.convertRepresentation(openedNotConverted.toBigInteger());
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public BigInteger out() {
    return opened;
  }

}
