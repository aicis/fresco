package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.util.ValidationUtils;

/**
 * Container for resources needed by runtimes (protocol suites).
 */
public class ResourcePoolImpl implements ResourcePool {

  private final int myId;
  private final int noOfPlayers;

  /**
   * Creates an instance of the default implementation of a resource pool. This contains the basic
   * resources needed within FRESCO.
   *
   * @param myId The ID of the MPC party. Must be a valid id, but may lie
   * outside of {@code noOfPlayers} as some subclasses create sub-networks, with fewer noOfPlayers.
   * @param noOfPlayers The amount of parties within the MPC computation.
   */
  public ResourcePoolImpl(int myId, int noOfPlayers) {
    ValidationUtils.assertValidId(myId);
    ValidationUtils.assertValidId(noOfPlayers);
    this.myId = myId;
    this.noOfPlayers = noOfPlayers;
  }

  @Override
  public int getMyId() {
    return this.myId;
  }

  @Override
  public int getNoOfParties() {
    return this.noOfPlayers;
  }

}
