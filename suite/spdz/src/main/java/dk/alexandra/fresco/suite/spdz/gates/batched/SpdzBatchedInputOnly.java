package dk.alexandra.fresco.suite.spdz.gates.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.framework.Deferred;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.math.BigInteger;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class SpdzBatchedInputOnly extends SpdzNativeProtocol<byte[]> {

  private final Deque<BigInteger> inputs;
  private final int inputPartyId;
  private final Deque<Deferred<SInt>> closed;
  private final boolean storeMaskBytesFlag;
  private byte[] inputMaskBytes;
  private List<SpdzInputMask> inputMasks;

  public SpdzBatchedInputOnly(int inputPartyId, boolean storeMaskBytesFlag) {
    this.inputPartyId = inputPartyId;
    this.inputs = new LinkedList<>();
    this.closed = new LinkedList<>();
    this.storeMaskBytesFlag = storeMaskBytesFlag;
  }

  public DRes<SInt> append(BigInteger input) {
    Deferred<SInt> deferred = new Deferred<>();
    inputs.add(input);
    closed.add(deferred);
    return deferred;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, Network network) {
    int myId = resourcePool.getMyId();
    ByteSerializer<BigInteger> serializer = resourcePool.getSerializer();
    SpdzDataSupplier dataSupplier = resourcePool.getDataSupplier();
    if (round == 0) {
      inputMasks = dataSupplier.getNextMasks(inputPartyId, inputs.size());
      if (myId == inputPartyId) {
        maskAndSend(resourcePool, network);
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      final byte[] inputMaskBytes = network.receive(inputPartyId);
      if (storeMaskBytesFlag) {
        this.inputMaskBytes = inputMaskBytes;
      }
      List<BigInteger> masked = serializer.deserializeList(inputMaskBytes);
      BigInteger macKeyShare = dataSupplier.getSecretSharedKey();
      for (int i = 0; i < inputMasks.size(); i++) {
        SpdzSInt maskShare = inputMasks.get(i).getMask();
        SpdzSInt out = maskShare.addConstant(
            masked.get(i),
            macKeyShare,
            resourcePool.getModulus(),
            myId == 1);
        closed.pop().callback(out);
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public byte[] out() {
    return inputMaskBytes;
  }

  private void maskAndSend(SpdzResourcePool resourcePool, Network network) {
    int byteLength = resourcePool.getModBitLength() / Byte.SIZE;
    int numInputs = inputs.size();
    final byte[] bytes = new byte[numInputs * byteLength];
    for (int i = 0; i < numInputs; i++) {
      BigInteger pop = inputs.pop();
      BigInteger masked = pop.subtract(inputMasks.get(i).getRealValue())
          .mod(resourcePool.getModulus());
      System.arraycopy(resourcePool.getSerializer().serialize(masked), 0, bytes,
          i * byteLength, byteLength);
    }
    network.sendToAll(bytes);
  }

}
