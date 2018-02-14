package dk.alexandra.fresco.suite.marlin.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinInputOnlyProtocol<T extends BigUInt<T>> extends
    MarlinNativeProtocol<Pair<DRes<SInt>, byte[]>, T> {

  private final T input;
  private final int inputPartyId;
  private MarlinInputMask<T> inputMask;
  private Pair<DRes<SInt>, byte[]> out;

  public MarlinInputOnlyProtocol(T input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool, Network network) {
    BigUIntFactory<T> factory = resourcePool.getFactory();
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
      MarlinSInt<T> out = inputMask.getMaskShare().addConstant(
          serializer.deserialize(inputMaskBytes),
          myId,
          macKeyShare,
          factory.zero());
      this.out = new Pair<>(out, inputMaskBytes);
      return EvaluationStatus.IS_DONE;
    }
//      if (resourcePool.getNoOfParties() <= 2) {
//        return EvaluationStatus.IS_DONE;
//      } else {
//        broadcast = resourcePool.createBroadcast(network);
//        // TODO maybe better to run broadcast directly on byte array received from network
//        digest = broadcast
//            .computeAndSendDigests(serializer.serialize(maskedInput));
//        return EvaluationStatus.HAS_MORE_ROUNDS;
//      }
//    } else {
//      // TODO more elegant way to deal with broadcast
//      if (broadcast != null) {
//        broadcast.receiveAndValidateDigests(digest);
//      }
//      return EvaluationStatus.IS_DONE;
//    }
  }

  @Override
  public Pair<DRes<SInt>, byte[]> out() {
    return out;
  }

}
