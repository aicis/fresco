package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinInputOnlyProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends MarlinNativeProtocol<Pair<DRes<SInt>, byte[]>, PlainT> {

  private final PlainT input;
  private final int inputPartyId;
  private MarlinInputMask<PlainT> inputMask;
  private Pair<DRes<SInt>, byte[]> out;

  public MarlinInputOnlyProtocol(PlainT input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<PlainT> resourcePool,
      Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    int myId = resourcePool.getMyId();
    ByteSerializer<PlainT> serializer = resourcePool.getRawSerializer();
    if (round == 0) {
      inputMask = resourcePool.getDataSupplier().getNextInputMask(inputPartyId);
      if (myId == inputPartyId) {
        PlainT bcValue = this.input.subtract(inputMask.getOpenValue());
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      byte[] inputMaskBytes = network.receive(inputPartyId);
      PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      MarlinSInt<PlainT> out = inputMask.getMaskShare().addConstant(
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
