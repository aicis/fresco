package dk.alexandra.fresco.framework;

/**
 * A collection of protocol that has a specific capacity. Elements can be added but not expected
 * after the capacity has been reacehd, the Iterable allows traversal and removal.
 */
public interface ProtocolCollection extends Iterable<Protocol> {

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

  /**
   * Gets the actual number of protocols in the collection.
   *
   * @return the number of elements
   */
  int size();
}
