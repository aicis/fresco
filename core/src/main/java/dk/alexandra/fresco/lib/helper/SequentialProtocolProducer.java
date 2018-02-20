package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class SequentialProtocolProducer implements ProtocolProducer {

  private ProtocolProducer currentProducer;
  private final Deque<ProtocolProducer> protocolProducers;

  public SequentialProtocolProducer(List<ProtocolProducer> protocols) {
    protocolProducers = new ArrayDeque<>(protocols);
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
      LazyProtocolProducerDecorator currentProducer = (LazyProtocolProducerDecorator) current;
      protocolProducers.removeFirst();
      protocolProducers.addFirst(currentProducer.getInnerProtocolProducer());
      return inline();
    } else if (current instanceof SequentialProtocolProducer) {
      SequentialProtocolProducer seq = (SequentialProtocolProducer) current;
      protocolProducers.removeFirst();
      for (Iterator<ProtocolProducer> iterator = seq.protocolProducers.descendingIterator();
          iterator.hasNext(); ) {
        ProtocolProducer protocolProducer = iterator.next();
        protocolProducers.addFirst(protocolProducer);
      }
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
