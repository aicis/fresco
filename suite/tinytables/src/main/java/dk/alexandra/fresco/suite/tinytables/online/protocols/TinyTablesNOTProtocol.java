package dk.alexandra.fresco.suite.tinytables.online.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproNOTProtocol;

/**
 * <p>
 * This class represents a NOT protocol in the online phase of the TinyTables protocol.
 * </p>
 * <p>
 * Here both players know the masked value of the input wire, <i>e<sub>u</sub> = b<sub>u</sub> + r
 * <sub>u</sub></i> where <i>b<sub>u</sub></i> is the unmasked value and <i>r<sub>u</sub></i> is the
 * mask. During the preprocessing phase (see {@link TinyTablesPreproNOTProtocol}, both players have
 * let their share of the mask of the output wire be equal to their share of the input wire, so <i>r
 * <sub>O</sub> = r<sub>u</sub></i>. Now, both players set value of the output wire to be <i>e
 * <sub>O</sub> = !e<sub>u</sub> = !b<sub>u</sub> + r<sub>O</sub></i>.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesNOTProtocol extends TinyTablesProtocol<SBool> {

  private DRes<SBool> in;
  private TinyTablesSBool out;

  public TinyTablesNOTProtocol(DRes<SBool> in) {
    this.in = in;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {
    this.out = new TinyTablesSBool(((TinyTablesSBool) in.out()).getValue().flip());
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return out;
  }

}
