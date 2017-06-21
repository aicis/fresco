package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ProtocolCollectionList implements ProtocolCollection {

  private int capacity;
  private List<NativeProtocol> protocols;

  public ProtocolCollectionList(int capacity) {
    this.capacity = capacity;
    this.protocols = new LinkedList<>();
  }

  @Override
  public void addProtocol(NativeProtocol protocol) {
    protocols.add(protocol);
  }

  @Override
  public boolean hasFreeCapacity() {
    return protocols.size() < capacity;
  }

  /**
   * Gets the protocols collected
   *
   * @return the protocols previously added
   */
  public List<NativeProtocol> getProtocols() {
    return protocols;
  }

  @Override
  public Iterator<NativeProtocol> iterator() {
    return protocols.iterator();
  }

  public int size() {
    return protocols.size();
  }
}
