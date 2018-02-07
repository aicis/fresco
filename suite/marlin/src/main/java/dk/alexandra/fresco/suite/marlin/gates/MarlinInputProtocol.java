package dk.alexandra.fresco.suite.marlin.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;

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
      this.inputMask = resourcePool.getDataSupplier().getNextInputMask(this.inputPartyId);
      if (myId == this.inputPartyId) {
        T bcValue = this.input.subtract(this.inputMask.getOpenValue());
        network.sendToAll(resourcePool.getRawSerializer().serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      T maskedInput = resourcePool.getRawSerializer().deserialize(network.receive(inputPartyId));
      MarlinElement<T> result = this.inputMask.getMaskShare()
          .addConstant(maskedInput, myId, resourcePool.getDataSupplier().getSecretSharedKey(),
              factory.zero());
      // TODO is it safe to set out before the protocol is done potentially?
      this.out = new MarlinSInt<>(result);
      if (network.getNoOfParties() <= 2) {
        return EvaluationStatus.IS_DONE;
      } else {
        this.broadcast = new Broadcast(network);
        // TODO maybe better to run broadcast directly to byte array received from network
        this.digest = broadcast.computeAndSendDigests(maskedInput.toByteArray());
        return EvaluationStatus.HAS_MORE_ROUNDS;
      }
    } else {
      // TODO more elegant way to deal with broadcast
      if (broadcast != null) {
        broadcast.receiveAndValidateDigests(this.digest);
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return out;
  }

}
