package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;

/**
 * <p>
 * This class represents an XOR protocol in the preprocessing phase of the TinyTables protocol
 * suite.
 * </p>
 *
 * <p>
 * Here each player lets his additive share of the mask of the output wire, <i>r<sub>O</sub></i> be
 * the sum of his shares of the masks of the input wires, <i>r<sub>u</sub></i> and <i>r
 * <sub>v</sub></i>, so in turn, <i>r<sub>O</sub> = r<sub>u</sub> + r<sub>v</sub></i>.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesPreproXORProtocol extends TinyTablesPreproProtocol<SBool> {

  private DRes<SBool> inLeft;
  private DRes<SBool> inRight;
  private TinyTablesPreproSBool out;

  public TinyTablesPreproXORProtocol(DRes<SBool> inLeft, DRes<SBool> inRight) {
    super();
    this.inLeft = inLeft;
    this.inRight = inRight;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {
    /*
     * Set r_O = r_u XOR r_v
     */
    TinyTablesPreproSBool left = (TinyTablesPreproSBool) inLeft.out();
    TinyTablesPreproSBool right = (TinyTablesPreproSBool) inRight.out();
    this.out = new TinyTablesPreproSBool(left.getValue().add(right.getValue()));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return out;
  }

}
