package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;

/**
 * Native protocol for inputting data.
 */
public class Spdz2kTwoPartyInputProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<SInt, PlainT> {

  private final PlainT input;
  private final int inputPartyId;
  private Spdz2kInputMask<PlainT> inputMask;
  private SInt share;

  /**
   * Creates new {@link Spdz2kTwoPartyInputProtocol}.
   *
   * @param input value to secret-share
   * @param inputPartyId id of input party
   */
  public Spdz2kTwoPartyInputProtocol(PlainT input, int inputPartyId) {
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
      this.share = out;
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return share;
  }

}
