package dk.alexandra.fresco.framework;

import java.util.LinkedList;
import java.util.List;

public class ProtocolCollectionLinkedList implements ProtocolCollection {

  private int capacity;
  private List<Protocol> protocols;

  public ProtocolCollectionLinkedList(int capacity) {
    this.capacity = capacity;
    this.protocols = new LinkedList<>();
  }

  @Override
  public void addProtocol(Protocol protocol) {
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
  public List<Protocol> getProtocols() {
    return protocols;
  }
}
