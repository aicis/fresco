package dk.alexandra.fresco.suite.spdz2k.protocols.natives.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.Deferred;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kNativeProtocol;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Batched version of {@link dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kInputOnlyProtocol}.
 */
public class Spdz2kBatchedInputOnly<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<byte[], PlainT> {

  private final Deque<PlainT> inputs;
  private final int inputPartyId;
  private final Deque<Deferred<SInt>> closed;
  private final boolean storeMaskBytesFlag;
  private byte[] inputMaskBytes;
  private List<Spdz2kInputMask<PlainT>> inputMasks;

  public Spdz2kBatchedInputOnly(int inputPartyId, boolean storeMaskBytesFlag) {
    this.inputPartyId = inputPartyId;
    this.inputs = new LinkedList<>();
    this.closed = new LinkedList<>();
    this.storeMaskBytesFlag = storeMaskBytesFlag;
  }

  public DRes<SInt> append(PlainT input) {
    Deferred<SInt> deferred = new Deferred<>();
    inputs.add(input);
    closed.add(deferred);
    return deferred;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    int myId = resourcePool.getMyId();
    Spdz2kDataSupplier<PlainT> dataSupplier = resourcePool.getDataSupplier();
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
      List<PlainT> receivedMasks = resourcePool.getPlainSerializer()
          .deserializeList(inputMaskBytes);
      PlainT macKeyShare = dataSupplier.getSecretSharedKey();
      for (int i = 0; i < inputMasks.size(); i++) {
        Spdz2kSInt<PlainT> maskShare = inputMasks.get(i).getMaskShare();
        Spdz2kSInt<PlainT> out = maskShare.addConstant(
            receivedMasks.get(i),
            macKeyShare,
            factory.zero(),
            myId == 1);
        closed.pop().callback(out);
      }
      inputMasks.clear();
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public byte[] out() {
    return inputMaskBytes;
  }

  private void maskAndSend(Spdz2kResourcePool<PlainT> resourcePool, Network network) {
    int byteLength = resourcePool.getFactory().getCompositeBitLength() / Byte.SIZE;
    int numInputs = inputs.size();
    final byte[] bytes = new byte[numInputs * byteLength];
    for (int i = 0; i < numInputs; i++) {
      PlainT input = inputs.pop();
      PlainT masked = input.subtract(inputMasks.get(i).getOpenValue());
      System.arraycopy(resourcePool.getPlainSerializer().serialize(masked), 0, bytes,
          i * byteLength, byteLength);
    }
    network.sendToAll(bytes);
  }

}
