package dk.alexandra.fresco.suite.tinytables.online.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproXORProtocol;

/**
 * <p>
 * This class represents an XOR protocol in the online phase of the TinyTables protocol.
 * </p>
 * 
 * <p>
 * During preprocessing (see {@link TinyTablesPreproXORProtocol}), each of the players let their
 * additive share of the mask of the output wire, <i>r<sub>O</sub></i>, be equal to the sum of their
 * shares of the input masks <i>r<sub>u</sub></i> and <i>r<sub>v</sub></i>, so <i>r<sub>O</sub> = r
 * <sub>u</sub> + r<sub>v</sub></i>.
 * </p>
 * 
 * <p>
 * Now, in the online phase, each player knows the masked input values <i>e<sub>u</sub> = b
 * <sub>u</sub> + r<sub>u</sub></i> and <i>e<sub>v</sub> = b<sub>v</sub> + r<sub>v</sub></i>, and
 * let the output value be equal to
 * </p>
 * <p>
 * <i>e<sub>u</sub> + e<sub>v</sub> = b<sub>u</sub> + r<sub>u</sub> + b<sub>v</sub> + r<sub>v</sub>
 * = b<sub>u</sub> + b<sub>v</sub> + r<sub>O</sub></i>
 * </p>
 * <p>
 * as desired.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesXORProtocol extends TinyTablesProtocol<SBool> {

  private DRes<SBool> inLeft, inRight;
  private TinyTablesSBool out;

  public TinyTablesXORProtocol(DRes<SBool> inLeft, DRes<SBool> inRight) {
    super();
    this.inLeft = inLeft;
    this.inRight = inRight;
  }

  public TinyTablesXORProtocol(DRes<SBool> inLeft, DRes<SBool> inRight, SBool out) {
    super();
    this.inLeft = inLeft;
    this.inRight = inRight;
    this.out = (TinyTablesSBool) out;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {
    if (round == 0) {
      // Free XOR
      out = (out == null) ? new TinyTablesSBool() : out;
      this.out.setValue(((TinyTablesSBool) inLeft.out()).getValue()
          .add(((TinyTablesSBool) inRight.out()).getValue()));
      return EvaluationStatus.IS_DONE;
    } else {
      throw new MPCException("Cannot evaluate XOR in round > 0");
    }
  }

  @Override
  public SBool out() {
    return out;
  }

}
