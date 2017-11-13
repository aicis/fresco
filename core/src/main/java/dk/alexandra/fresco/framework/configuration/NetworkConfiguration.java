package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.Party;

/**
 * How to configure the network. That is, which MPC parties takes part of the computation and which
 * IP and port can they be contacted.
 */
public interface NetworkConfiguration {

  /**
   * @param pId A non-zero integer that uniquely identifies the party in a computation.
   * @return The MPC party with the given ID.
   */
  Party getParty(int pId);

  /**
   * 
   * @return Ourselves
   */
  Party getMe();

  /**
   * 
   * @return The pId of ourselves
   */
  int getMyId();

  /**
   * 
   * @return The number of parties within the MPC computation.
   */
  int noOfParties();

}
