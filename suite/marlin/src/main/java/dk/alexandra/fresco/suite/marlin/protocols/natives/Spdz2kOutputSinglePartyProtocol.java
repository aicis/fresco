package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kOpenedValueStore;
import java.math.BigInteger;
import java.util.List;

public class Spdz2kOutputSinglePartyProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<BigInteger, PlainT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private final int outputParty;
  private BigInteger opened;
  private Spdz2kInputMask<PlainT> inputMask;
  private Spdz2kSInt<PlainT> inMinusMask;

  public Spdz2kOutputSinglePartyProtocol(DRes<SInt> share, int outputParty) {
    this.share = share;
    this.outputParty = outputParty;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    Spdz2kOpenedValueStore<PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    Spdz2kDataSupplier<PlainT> supplier = resourcePool.getDataSupplier();
    if (round == 0) {
      this.inputMask = supplier.getNextInputMask(outputParty);
      inMinusMask = ((Spdz2kSInt<PlainT>) this.share.out()).subtract(this.inputMask.getMaskShare());
      network.sendToAll(inMinusMask.getShare().getLeastSignificant().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<PlainT> shares = resourcePool.getRawSerializer()
          .deserializeList(network.receiveFromAll());
      PlainT recombined = UInt.sum(shares);
      openedValueStore.pushOpenedValue(inMinusMask, recombined);
      if (outputParty == resourcePool.getMyId()) {
        this.opened = resourcePool.convertRepresentation(recombined.add(inputMask.getOpenValue()));
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public BigInteger out() {
    return opened;
  }

}
