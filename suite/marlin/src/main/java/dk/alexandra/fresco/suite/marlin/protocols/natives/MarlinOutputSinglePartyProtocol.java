package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;
import java.util.List;

public class MarlinOutputSinglePartyProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends MarlinNativeProtocol<BigInteger, PlainT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private final int outputParty;
  private BigInteger opened;
  private MarlinInputMask<PlainT> inputMask;
  private MarlinSInt<PlainT> inMinusMask;

  public MarlinOutputSinglePartyProtocol(DRes<SInt> share, int outputParty) {
    this.share = share;
    this.outputParty = outputParty;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<PlainT> resourcePool,
      Network network) {
    MarlinOpenedValueStore<PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    MarlinDataSupplier<PlainT> supplier = resourcePool.getDataSupplier();
    if (round == 0) {
      this.inputMask = supplier.getNextInputMask(outputParty);
      inMinusMask = ((MarlinSInt<PlainT>) this.share.out()).subtract(this.inputMask.getMaskShare());
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
