package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

public class MaliciousSpdzInputProtocol extends SpdzNativeProtocol<SInt> {

  private SpdzInputMask inputMask; // is opened by this gate.
  protected BigInteger input;
  private BigInteger valueMasked;
  protected SpdzSInt out;
  private int inputter;
  private byte[] digest;

  public MaliciousSpdzInputProtocol(BigInteger input, int inputter) {
    this.input = input;
    this.inputter = inputter;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool, Network network) {
    int myId = spdzResourcePool.getMyId();
    BigInteger modulus = spdzResourcePool.getModulus();
    SpdzStorage storage = spdzResourcePool.getStore();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      this.inputMask = storage.getSupplier().getNextInputMask(this.inputter);
      if (myId == this.inputter) {
        BigInteger bcValue = this.input.subtract(this.inputMask.getRealValue());
        bcValue = bcValue.mod(modulus);
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      this.valueMasked = serializer.deserialize(network.receive(inputter));
      this.digest = sendMaliciousBroadcastValidation(spdzResourcePool.getMessageDigest(), network,
          valueMasked);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      boolean validated = receiveMaliciousBroadcastValidation(network, digest);
      if (!validated) {
        throw new MaliciousException("SecureBroadcastUtil digests did not match");
      }
      SpdzElement valueMaskedElm = new SpdzElement(valueMasked,
          storage.getSecretSharedKey().multiply(valueMasked).mod(modulus), modulus);
      this.out = new SpdzSInt(this.inputMask.getMask().add(valueMaskedElm, myId));
      return EvaluationStatus.IS_DONE;
    }

  }

  @Override
  public SpdzSInt out() {
    return out;
  }

  byte[] sendMaliciousBroadcastValidation(MessageDigest dig, Network network, BigInteger b) {
    dig.update(b.toByteArray());
    return sendAndReset(dig, network);
  }

  private byte[] sendAndReset(MessageDigest dig, Network network) {
    byte[] digest = dig.digest();
    dig.reset();
    digest[0] = (byte) 0xFF;
    network.sendToAll(digest);
    return digest;
  }

  private boolean receiveMaliciousBroadcastValidation(Network network, byte[] digest) {
    // TODO: should we check that we get messages from all players?
    boolean validated = true;
    List<byte[]> digests = network.receiveFromAll();
    for (byte[] d : digests) {
      boolean equals = Arrays.equals(d, digest);
      validated = validated && equals;
    }
    return validated;
  }
}
