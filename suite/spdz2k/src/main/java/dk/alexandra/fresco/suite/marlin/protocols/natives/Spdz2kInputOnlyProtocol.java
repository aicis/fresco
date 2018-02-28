package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;

public class Spdz2kInputOnlyProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<Pair<DRes<SInt>, byte[]>, PlainT> {

  private final PlainT input;
  private final int inputPartyId;
  private Spdz2kInputMask<PlainT> inputMask;
  private Pair<DRes<SInt>, byte[]> out;

  public Spdz2kInputOnlyProtocol(PlainT input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
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
      Spdz2kSInt<PlainT> out = inputMask.getMaskShare().addConstant(
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
