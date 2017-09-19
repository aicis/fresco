package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ProtocolCollectionList<ResourcePoolT extends ResourcePool>
    implements ProtocolCollection<ResourcePoolT> {

  private int capacity;
  private List<NativeProtocol<?, ResourcePoolT>> protocols;

  public ProtocolCollectionList(int capacity) {
    this.capacity = capacity;
    this.protocols = new LinkedList<>();
  }

  @Override
  public void addProtocol(NativeProtocol<?, ResourcePoolT> protocol) {
    protocols.add(protocol);
  }

  @Override
  public boolean hasFreeCapacity() {
    return protocols.size() < capacity;
  }

  @Override
  public Iterator<NativeProtocol<?, ResourcePoolT>> iterator() {
    return protocols.iterator();
  }

  public int size() {
    return protocols.size();
  }
}
