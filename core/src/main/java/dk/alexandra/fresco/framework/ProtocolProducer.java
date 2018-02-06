package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * Invariants:
 *
 * <p>INVARIANT1: Once a call to getNextprotocolss() does not return any new protocols or,
 * equivalently, hasMoreprotocolss() returns false, these methods will continue to return no new
 * protocols, or false.
 *
 * <p>INVARIANT2: All protocols returned in a slice must only depend on protocols
 * in previous slices.
 *
 * <p>TODO: Should INVARIANT2 be replaced by another invariant, stating that all protocols must only
 * depend on protocols before itself, possibly in the same slice? Some protocol-evaluator protocol
 * suite constellations may benefit from this??
 */
public interface ProtocolProducer {

  /**
   * Attempt to fill the given protocols array with ready protocols.
   *
   * If no protocols are ready, the result of the method equals the given n.
   *
   *
   * TODO: Does no next protocols mean that evaluation has finished, or just that evaluator should
   * try again later?
   *
   * @param protocolCollection destination for protocols.
   */
  <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection);

  /**
   * Returns true if there is at least one protocols left in the protocol that has not already been
   * evaluated.
   */
  boolean hasNextProtocols();
}
