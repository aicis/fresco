package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinAllBroadcastProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinBroadcastValidationProtocol;
import java.util.List;

public class MarlinBroadcastProtocolProducer<T extends BigUInt<T>> implements ProtocolProducer {

  private final SequentialProtocolProducer protocolProducer;
  private MarlinAllBroadcastProtocol<T> allBroadcast;

  MarlinBroadcastProtocolProducer(byte[] input) {
    protocolProducer = new SequentialProtocolProducer();
    protocolProducer.lazyAppend(() -> {
      allBroadcast = new MarlinAllBroadcastProtocol<>(input);
      return new SingleProtocolProducer<>(allBroadcast);
    });
    protocolProducer.lazyAppend(() -> new SingleProtocolProducer<>(
        new MarlinBroadcastValidationProtocol<>(allBroadcast.out()))
    );
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

  public List<byte[]> out() {
    return allBroadcast.out();
  }

}
