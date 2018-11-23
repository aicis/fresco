package dk.alexandra.fresco.suite.tinytables.ot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.ot.base.Ot;

/**
 * Wrapper interface for (seed) OT to work with TinyTables. Specifically this interface has the
 * purpose to decouple the construction of a seed OT to its network assignment. This is strictly
 * needed in TinyTables as choice of concrete OT is made before a concrete network might be
 * available. The coupling to a concrete network object is done by a call to init, which must be
 * done before any calls to the generic OT interface.
 */
public interface TinyTablesOt extends Ot {
  /**
   * Initializes the underlying OT functionality with a concrete network.
   *
   * @param network The concrete network to use.
   */
  void init(Network network);
}
