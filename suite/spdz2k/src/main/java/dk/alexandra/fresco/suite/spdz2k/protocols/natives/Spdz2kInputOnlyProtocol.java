package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;

/**
 * Native protocol for inputting data. <p>This is used by native computation {@link
 * dk.alexandra.fresco.suite.spdz2k.protocols.computations.Spdz2kInputComputation}. The result of
 * this protocol is this party's share of the input, as well as the bytes of the masked input which
 * are later used in a broadcast validation.</p>
 */
public class Spdz2kInputOnlyProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<Pair<DRes<SInt>, byte[]>, PlainT> {

  private final PlainT input;
  private final int inputPartyId;
  private Spdz2kInputMask<PlainT> inputMask;
  private Pair<DRes<SInt>, byte[]> shareAndMaskBytes;

  /**
   * Creates new {@link Spdz2kInputOnlyProtocol}.
   *
   * @param input value to secret-share
   * @param inputPartyId id of input party
   */
  public Spdz2kInputOnlyProtocol(PlainT input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    int myId = resourcePool.getMyId();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    Spdz2kDataSupplier<PlainT> dataSupplier = resourcePool.getDataSupplier();
    if (round == 0) {
      inputMask = dataSupplier.getNextInputMask(inputPartyId);
      if (myId == inputPartyId) {
        PlainT bcValue = this.input.subtract(inputMask.getOpenValue());
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      byte[] inputMaskBytes = network.receive(inputPartyId);
      PlainT macKeyShare = dataSupplier.getSecretSharedKey();
      Spdz2kSInt<PlainT> maskShare = inputMask.getMaskShare();
      Spdz2kSInt<PlainT> out = maskShare.addConstant(
          serializer.deserialize(inputMaskBytes),
          macKeyShare,
          factory.zero(),
          myId == 1);
      this.shareAndMaskBytes = new Pair<>(out, inputMaskBytes);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Pair<DRes<SInt>, byte[]> out() {
    return shareAndMaskBytes;
  }

}
