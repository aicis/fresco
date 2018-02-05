package dk.alexandra.fresco.suite.marlin.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.storage.MarlinDataSupplier;

public class MarlinInputProtocol<T extends BigUInt<T>> extends MarlinNativeProtocol<SInt, T> {

  private final T input;
  private final int inputPartyId;
  private MarlinInputMask<T> inputMask;
  private T maskedInput;
  private MarlinSInt<T> out;

  public MarlinInputProtocol(T input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<T> resourcePool,
      Network network) {
    // TODO check that we don't need to add anything to storage
    int myId = resourcePool.getMyId();
    MarlinDataSupplier<T> supplier = resourcePool.getDataSupplier();
    BigUIntFactory<T> factory = resourcePool.getFactory();
    ByteSerializer<T> serializer = resourcePool.getRawSerializer();
    if (round == 0) {
      this.inputMask = supplier.getNextInputMask(this.inputPartyId);
      if (myId == this.inputPartyId) {
        T bcValue = this.input.subtract(this.inputMask.getOpenValue());
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      // TODO put back broadcast validation
      this.maskedInput = serializer.deserialize(network.receive(inputPartyId));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      MarlinElement<T> result = this.inputMask.getMaskShare()
          .add(maskedInput, myId, supplier.getSecretSharedKey(), factory.createZero());
      this.out = new MarlinSInt<>(result);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return out;
  }

}
