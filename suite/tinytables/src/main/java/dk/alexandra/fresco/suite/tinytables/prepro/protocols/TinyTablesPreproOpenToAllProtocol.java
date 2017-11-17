package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;

/**
 * <p>
 * This class represents an open-to-all protocol in the preprocessing phase of the TinyTables
 * protocol.
 * </p>
 * 
 * <p>
 * Here each player stores his share of the mask of the input wire. In the online phase this is then
 * shared with the other player to allow the unmasking of the input value.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesPreproOpenToAllProtocol extends TinyTablesPreproProtocol<Void> {

  private DRes<SBool> toOpen;

  public TinyTablesPreproOpenToAllProtocol(int id, DRes<SBool> toOpen) {
    super();
    this.id = id;
    this.toOpen = toOpen;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {
    TinyTablesPreproProtocolSuite ps =
        TinyTablesPreproProtocolSuite.getInstance(resourcePool.getMyId());

    /*
     * To open a value, we use the same mask share as for the input which will cancel out the mask
     * when the shares are combined.
     */
    ps.getStorage().storeMaskShare(id, ((TinyTablesPreproSBool) toOpen.out()).getValue());

    return EvaluationStatus.IS_DONE;
  }

  @Override
  public Void out() {
    return null;
  }

}
