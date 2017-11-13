package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Arrays;
import java.util.LinkedList;

public class SequentialProtocolProducer implements ProtocolProducer, ProtocolProducerCollection {

  private LinkedList<ProtocolProducer> protocolProducers = new LinkedList<>();
  private ProtocolProducer currentProducer;

  public SequentialProtocolProducer(ProtocolProducer... protocolProducers) {
    this.protocolProducers.addAll(Arrays.asList(protocolProducers));
  }

  public SequentialProtocolProducer() {

  }

  public void append(ProtocolProducer protocolProducer) {
    this.protocolProducers.add(protocolProducer);
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
    ProtocolProducer current = protocolProducers.getFirst();
    if (current instanceof LazyProtocolProducerDecorator) {
      protocolProducers.removeFirst();
      LazyProtocolProducerDecorator currentProducer = (LazyProtocolProducerDecorator) current;
      protocolProducers.add(0, currentProducer.getInnerProtocolProducer());
      return inline();
    } else if (current instanceof SequentialProtocolProducer) {
      protocolProducers.removeFirst();
      SequentialProtocolProducer seq = (SequentialProtocolProducer) current;
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
    while (!protocolProducers.isEmpty() && !protocolProducers.getFirst().hasNextProtocols()) {
      protocolProducers.removeFirst();
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
