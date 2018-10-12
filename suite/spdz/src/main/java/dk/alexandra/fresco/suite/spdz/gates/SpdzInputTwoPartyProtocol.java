package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.math.BigInteger;

/**
 * SPDZ input protocol for the two party case. <p>Unlike the multi-party case we don't need to
 * perform broadcast validation.</p>
 */
public class SpdzInputTwoPartyProtocol extends SpdzNativeProtocol<SInt> {

  SpdzInputMask inputMask; // is opened by this gate.
  BigInteger input;
  BigInteger valueMasked;
  SpdzSInt out;
  int inputter;

  public SpdzInputTwoPartyProtocol(BigInteger input, int inputter) {
    this.input = input;
    this.inputter = inputter;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int myId = spdzResourcePool.getMyId();
    BigInteger modulus = spdzResourcePool.getModulus();
    SpdzDataSupplier dataSupplier = spdzResourcePool.getDataSupplier();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      this.inputMask = dataSupplier.getNextInputMask(this.inputter);
      if (myId == this.inputter) {
        BigInteger bcValue = this.input.subtract(this.inputMask.getRealValue());
        bcValue = bcValue.mod(modulus);
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      this.valueMasked = serializer.deserialize(network.receive(inputter));
      SpdzSInt valueMaskedElement =
          new SpdzSInt(
              valueMasked,
              dataSupplier.getSecretSharedKey().multiply(valueMasked).mod(modulus),
              modulus);
      this.out = this.inputMask.getMask().add(valueMaskedElement, myId);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SpdzSInt out() {
    return out;
  }

}
