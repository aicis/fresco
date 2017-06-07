package dk.alexandra.fresco.framework;

import java.util.LinkedList;
import java.util.List;

public class ProtocolCollectionLinkedList implements ProtocolCollection {

  private int capacoty;
  private List<Protocol> protocols;

  public ProtocolCollectionLinkedList(int capacoty) {
    this.capacoty = capacoty;
    this.protocols = new LinkedList<>();
  }

  @Override
  public void addProtocol(Protocol protocol) {
    protocols.add(protocol);
  }

  @Override
  public boolean hasFreeCapacity() {
    return protocols.size() < capacoty;
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
