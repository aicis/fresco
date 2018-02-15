package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.ArrayList;
import java.util.List;

public class SequentialProtocolProducer implements ProtocolProducer {

  private ProtocolProducer currentProducer;
  private final List<ProtocolProducer> protocolProducers;

  public SequentialProtocolProducer(List<ProtocolProducer> protocols) {
    protocolProducers = new ArrayList<>(protocols);
  }


  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    if (currentProducer == null) {
      currentProducer = inline();
      if (currentProducer == null) {
        return;
      }
    }
    currentProducer.getNextProtocols(protocolCollection);
  }

  private ProtocolProducer inline() {
    if (protocolProducers.isEmpty()) {
      return null;
    }
    ProtocolProducer current = protocolProducers.get(0);
    if (current instanceof LazyProtocolProducerDecorator) {
      LazyProtocolProducerDecorator currentProducer = (LazyProtocolProducerDecorator) current;
      protocolProducers.remove(0);
      protocolProducers.add(0, currentProducer.getInnerProtocolProducer());
      return inline();
    } else if (current instanceof SequentialProtocolProducer) {
      SequentialProtocolProducer seq = (SequentialProtocolProducer) current;
      protocolProducers.remove(0);
      protocolProducers.addAll(0, seq.protocolProducers);
      return inline();
    } else {
      return current;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    if (currentProducer != null && currentProducer.hasNextProtocols()) {
      return true;
    }
    while (!protocolProducers.isEmpty() && !protocolProducers.get(0).hasNextProtocols()) {
      protocolProducers.remove(0);
      currentProducer = null;
    }
    return !protocolProducers.isEmpty();
  }

  @Override
  public String toString() {
    return "SequentialProtocolProducer{"
        + ", protocolProducers=" + protocolProducers
        + '}';
  }
}
