package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinBroadcastValidationProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinInputOnlyProtocol;

public class MarlinInputProtocolProducer<T extends BigUInt<T>> implements ProtocolProducer {

  private final SequentialProtocolProducer protocolProducer;
  private MarlinInputOnlyProtocol<T> unvalidatedInput;

  public MarlinInputProtocolProducer(T input, int inputPartyId) {
    protocolProducer = new SequentialProtocolProducer();
    protocolProducer.lazyAppend(() -> {
      unvalidatedInput = new MarlinInputOnlyProtocol<>(input, inputPartyId);
      return new SingleProtocolProducer<>(unvalidatedInput);
    });
    protocolProducer.lazyAppend(() -> new SingleProtocolProducer<>(
        new MarlinBroadcastValidationProtocol<>(unvalidatedInput.out().getSecond())
    ));
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    protocolProducer.getNextProtocols(protocolCollection);
  }

  @Override
  public boolean hasNextProtocols() {
    return protocolProducer.hasNextProtocols();
  }

  public SInt out() {
    return unvalidatedInput.out().getFirst().out();
  }

}
