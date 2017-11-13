package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.network.Network;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Container for resources needed by runtimes (protocol suites).
 *
 */
public class ResourcePoolImpl implements ResourcePool {

  private int myId;
  private int noOfPlayers;
  protected Network network;
  protected Random random;
  private SecureRandom secRand;

  /**
   * Creates an instance of the default implementation of a resource pool. This contains the basic
   * resources needed within FRESCO.
   * 
   * @param myId The ID of the MPC party.
   * @param noOfPlayers The amount of parties within the MPC computation.
   * @param network The network to use.
   * @param random The random source to use.
   * @param secRand The secure random source to use.
   */
  public ResourcePoolImpl(int myId, int noOfPlayers, Network network, Random random,
      SecureRandom secRand) {
    this.myId = myId;
    this.noOfPlayers = noOfPlayers;
    this.network = network;
    this.random = random;
    this.secRand = secRand;
  }

  @Override
  public Network getNetwork() {
    return this.network;
  }

  @Override
  public Random getRandom() {
    return this.random;
  }

  @Override
  public SecureRandom getSecureRandom() {
    return this.secRand;
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
