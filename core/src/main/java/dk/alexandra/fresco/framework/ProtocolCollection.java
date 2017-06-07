package dk.alexandra.fresco.framework;

/**
 * A collection of protocol that has a specific capacity.
 */
public interface ProtocolCollection {

  /**
   * Receives a protocol to be added to the collection.
   *
   * @param protocol new protocol
   */
  void addProtocol(Protocol protocol);

  /**
   * Checks if this collection has a free slots.
   *
   * @return true if there is availabe capacity
   */
  boolean hasFreeCapacity();
}
