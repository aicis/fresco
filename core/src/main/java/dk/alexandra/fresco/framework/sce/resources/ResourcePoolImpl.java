package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.network.Network;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Container for resources needed by runtimes (protocol suites).
 *
 * @author Kasper Damgaard
 */
public class ResourcePoolImpl implements ResourcePool {

  private int myId;
  private int noOfPlayers;
  protected Network network;
  protected Random random;
  private SecureRandom secRand;

  public ResourcePoolImpl(int myId, int noOfPlayers, Network network,
      Random random, SecureRandom secRand) {
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
