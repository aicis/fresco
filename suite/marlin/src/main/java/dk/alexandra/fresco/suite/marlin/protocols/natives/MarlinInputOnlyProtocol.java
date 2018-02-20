package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinInputOnlyProtocol<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> extends
    MarlinNativeProtocol<Pair<DRes<SInt>, byte[]>, H, L, T> {

  private final T input;
  private final int inputPartyId;
  private MarlinInputMask<H, L, T> inputMask;
  private Pair<DRes<SInt>, byte[]> out;

  public MarlinInputOnlyProtocol(T input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<H, L, T> resourcePool, Network network) {
    CompUIntFactory<H, L, T> factory = resourcePool.getFactory();
    int myId = resourcePool.getMyId();
    ByteSerializer<T> serializer = resourcePool.getRawSerializer();
    if (round == 0) {
      inputMask = resourcePool.getDataSupplier().getNextInputMask(inputPartyId);
      if (myId == inputPartyId) {
        T bcValue = this.input.subtract(inputMask.getOpenValue());
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      byte[] inputMaskBytes = network.receive(inputPartyId);
      T macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      MarlinSInt<H, L, T> out = inputMask.getMaskShare().addConstant(
          serializer.deserialize(inputMaskBytes),
          myId,
          macKeyShare,
          factory.zero());
      this.out = new Pair<>(out, inputMaskBytes);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Pair<DRes<SInt>, byte[]> out() {
    return out;
  }

}
