package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.math.BigInteger;

/**
 * SPDZ input protocol for multi-party case. <p>This requires broadcast validation.</p>
 */
public class SpdzInputProtocol extends SpdzInputTwoPartyProtocol {

  private byte[] digest;

  public SpdzInputProtocol(BigInteger input, int inputter) {
    super(input, inputter);
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
    } else if (round == 1) {
      this.valueMasked = serializer.deserialize(network.receive(inputter));
      this.digest = sendBroadcastValidation(
          spdzResourcePool.getMessageDigest(), network,
          valueMasked);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      boolean validated = receiveBroadcastValidation(network, digest);
      if (!validated) {
        throw new MaliciousException("Broadcast digests did not match");
      }
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
