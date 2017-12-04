package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.util.Drbg;

/**
 * The ResourcePool gives the protocol suites and their native protocols access to whatever is
 * within a Resource pool. A basic implementation contains the methods seen here, but often protocol
 * suites would extend this.
 */
public interface ResourcePool {

  /**
   * Returns the id of the party.
   */
  int getMyId();

  /**
   * Returns the number of players.
   */
  int getNoOfParties();

  /**
   * The DRBG is useful for protocols which needs a form of shared randomness where the random bytes
   * are not easily guessed by an adversary. This generator will provide exactly that. For explicit
   * security guarantees, we refer to implementations of
   * {@link dk.alexandra.fresco.framework.util.Drbg}.
   * 
   * @return An instance of a DRBG.
   */
  Drbg getRandomGenerator();

}
