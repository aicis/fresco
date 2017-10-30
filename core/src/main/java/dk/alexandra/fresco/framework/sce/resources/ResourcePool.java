package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.network.Network;
import java.security.SecureRandom;
import java.util.Random;

/**
 * The ResourcePool gives the protocol suites and their native protocols access to whatever is
 * within a Resource pool. A basic implementation contains the methods seen here, but often protocol
 * suites would extend this. 
 */
public interface ResourcePool {

  /**
   * Returns the id of the party
   */
  int getMyId();

  /**
   * Returns the number of players.
   */
  int getNoOfParties();

  /**
   * Returns the raw network in case the protocol suite needs access to this.
   * It should not be used for the individual protocols, but rather only for
   * doing some work before or after an application evaluation.
   */
  Network getNetwork();

  /**
   * Returns the randomness generator of the system. Use this for getting
   * random data that does not need to be cryptographically secure.
   */
  Random getRandom();

  /**
   * Returns the secure version of the randomness generator of the system. Use
   * where the randomness needs to be cryptographically secure.
   */
  SecureRandom getSecureRandom();

}