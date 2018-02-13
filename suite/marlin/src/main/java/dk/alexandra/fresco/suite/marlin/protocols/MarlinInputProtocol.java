package dk.alexandra.fresco.suite.marlin.protocols;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;

public class MarlinInputProtocol<T extends BigUInt<T>> extends MarlinNativeProtocol<SInt, T> {

  private final T input;
  private final int inputPartyId;
  private MarlinInputMask<T> inputMask;
  private byte[] digest;
  private MarlinSInt<T> out;
  private Broadcast broadcast; // TODO this belongs on the resource pool

  public MarlinInputProtocol(T input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
    this.broadcast = null;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool, Network network) {
    BigUIntFactory<T> factory = resourcePool.getFactory();
    int myId = resourcePool.getMyId();
    if (round == 0) {
      inputMask = resourcePool.getDataSupplier().getNextInputMask(inputPartyId);
      if (myId == inputPartyId) {
        T bcValue = this.input.subtract(inputMask.getOpenValue());
        network.sendToAll(resourcePool.getRawSerializer().serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      T maskedInput = resourcePool.getRawSerializer().deserialize(network.receive(inputPartyId));
      out = inputMask.getMaskShare()
          .addConstant(maskedInput, myId, resourcePool.getDataSupplier().getSecretSharedKey(),
              factory.zero());
      if (resourcePool.getNoOfParties() <= 2) {
        return EvaluationStatus.IS_DONE;
      } else {
        broadcast = resourcePool.createBroadcast(network);
        // TODO maybe better to run broadcast directly on byte array received from network
        digest = broadcast
            .computeAndSendDigests(resourcePool.getRawSerializer().serialize(maskedInput));
        return EvaluationStatus.HAS_MORE_ROUNDS;
      }
    } else {
      // TODO more elegant way to deal with broadcast
      if (broadcast != null) {
        broadcast.receiveAndValidateDigests(digest);
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return out;
  }

}
