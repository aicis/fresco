package dk.alexandra.fresco.framework.sce.resources;

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

}
