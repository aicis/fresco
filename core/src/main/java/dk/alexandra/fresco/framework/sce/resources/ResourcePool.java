package dk.alexandra.fresco.framework.sce.resources;

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