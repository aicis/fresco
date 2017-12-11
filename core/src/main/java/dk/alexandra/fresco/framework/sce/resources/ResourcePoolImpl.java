package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.util.Drbg;

/**
 * Container for resources needed by runtimes (protocol suites).
 */
public class ResourcePoolImpl implements ResourcePool {

  private final int myId;
  private final int noOfPlayers;
  private final Drbg drbg;

  /**
   * Creates an instance of the default implementation of a resource pool. This contains the basic
   * resources needed within FRESCO.
   *
   * @param myId The ID of the MPC party.
   * @param noOfPlayers The amount of parties within the MPC computation.
   * @param drbg The DRBG providing shared randomness.
   */
  public ResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg) {
    this.myId = myId;
    this.noOfPlayers = noOfPlayers;
    this.drbg = drbg;
  }

  @Override
  public int getMyId() {
    return this.myId;
  }

  @Override
  public int getNoOfParties() {
    return this.noOfPlayers;
  }

  @Override
  public Drbg getRandomGenerator() {
    return this.drbg;
  }

}
