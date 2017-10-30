package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A parallel producer contains a set of protocols producer that are asked to fill the collection
 * eagerly. Given some of the producers are lazy initialized this performs best - and hence does a
 * breadth first search for more protocols. THis skews the evalueration of the protocols in favor of
 * the first, but delivers the best performance in terms of memory.
 */
public class ParallelProtocolProducer implements ProtocolProducer, ProtocolProducerCollection {

  private LinkedList<ProtocolProducer> subProducers;

  public ParallelProtocolProducer() {
    subProducers = new LinkedList<>();
  }

  public void append(ProtocolProducer protocolProducer) {
    subProducers.offer(protocolProducer);
  }

  @Override
  public boolean hasNextProtocols() {
    for (Iterator<ProtocolProducer> iterator = subProducers.iterator(); iterator.hasNext(); ) {
      ProtocolProducer producer = iterator.next();
      if (producer.hasNextProtocols()) {
        return true;
      } else {
        iterator.remove();
      }
    }
    return false;
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    ListIterator<ProtocolProducer> iterator = subProducers.listIterator();
    while (iterator.hasNext() && protocolCollection.hasFreeCapacity()) {
      ProtocolProducer producer = iterator.next();
      if (producer.hasNextProtocols()) {
        producer.getNextProtocols(protocolCollection);
      } else {
        iterator.remove();
      }
    }
  }

}
