package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;

public class SpdzInputProtocol extends SpdzNativeProtocol<SInt> {

  private SpdzInputMask inputMask; // is opened by this gate.
  protected BigInteger input;
  private BigInteger value_masked;
  protected SpdzSInt out;
  private int inputter;
  private byte[] digest;

  public SpdzInputProtocol(BigInteger input, int inputter) {
    this.input = input;
    this.inputter = inputter;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int myId = spdzResourcePool.getMyId();
    BigInteger modulus = spdzResourcePool.getModulus();
    SpdzStorage storage = spdzResourcePool.getStore();
    BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
    switch (round) {
      case 0:
        this.inputMask = storage.getSupplier().getNextInputMask(this.inputter);
        if (myId == this.inputter) {
          BigInteger bcValue = this.input.subtract(this.inputMask.getRealValue());
          bcValue = bcValue.mod(modulus);
          network.sendToAll(serializer.toBytes(bcValue));
        }
        return EvaluationStatus.HAS_MORE_ROUNDS;
      case 1:
        this.value_masked = serializer.toBigInteger(network.receive(inputter));
        this.digest = sendBroadcastValidation(
            spdzResourcePool.getMessageDigest(), network,
            value_masked);
        return EvaluationStatus.HAS_MORE_ROUNDS;
      case 2:
        boolean validated = receiveBroadcastValidation(network, digest);
        if (!validated) {
          throw new MPCException("Broadcast digests did not match");
        }
        SpdzElement value_masked_elm =
            new SpdzElement(
                value_masked,
                storage.getSSK().multiply(value_masked).mod(modulus),
                modulus);
        this.out = new SpdzSInt(this.inputMask.getMask().add(value_masked_elm, myId));
        return EvaluationStatus.IS_DONE;
    }
    throw new MPCException("Cannot evaluate rounds larger than 2");
  }

  @Override
  public SpdzSInt out() {
    return out;
  }

}
