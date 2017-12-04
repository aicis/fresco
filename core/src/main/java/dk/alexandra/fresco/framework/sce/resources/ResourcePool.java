package dk.alexandra.fresco.framework.sce.resources;

import dk.alexandra.fresco.framework.util.DeterministicRandomBitGenerator;

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
   * The DRBG is useful for protocols which needs a form of shared randomness which is not
   * completely insecure. This generator will provide exactly that. Note that if the generator is
   * not already seeded within the constructor, the seeding must be done before use to ensure secure
   * randomness.
   * 
   * @return An instance of a DRBG.
   */
  DeterministicRandomBitGenerator getRandomGenerator();

}
