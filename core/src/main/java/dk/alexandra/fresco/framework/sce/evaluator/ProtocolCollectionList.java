package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ProtocolCollectionList<OutputT, ResourcePoolT extends ResourcePool>
    implements ProtocolCollection<OutputT, ResourcePoolT> {

  private int capacity;
  private List<NativeProtocol<OutputT, ResourcePoolT>> protocols;

  public ProtocolCollectionList(int capacity) {
    this.capacity = capacity;
    this.protocols = new LinkedList<>();
  }

  @Override
  public void addProtocol(NativeProtocol<OutputT, ResourcePoolT> protocol) {
    protocols.add(protocol);
  }

  @Override
  public boolean hasFreeCapacity() {
    return protocols.size() < capacity;
  }

  @Override
  public Iterator<NativeProtocol<OutputT, ResourcePoolT>> iterator() {
    return protocols.iterator();
  }

  public int size() {
    return protocols.size();
  }
}
