package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproResourcePool;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;

/**
 * <p>
 * This class represents a NOT protocol for the preprocessing phase of the TinyTables protocol
 * suite.
 * </p>
 *
 * <p>
 * Here both players assign their share of the mask of the output wire to be the same as their share
 * of the input wire, hence letting the mask of the output wire to be equal to the mask of the input
 * wire.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesPreproNOTProtocol extends TinyTablesPreproProtocol<SBool> {

  private DRes<SBool> in;
  private TinyTablesPreproSBool out;

  public TinyTablesPreproNOTProtocol(DRes<SBool> in) {
    this.in = in;
  }

  @Override
  public EvaluationStatus evaluate(int round, TinyTablesPreproResourcePool resourcePool,
      Network network) {

    /*
     * Use same masking parameter for the output
     */
    out = new TinyTablesPreproSBool(((TinyTablesPreproSBool) in.out()).getValue());

    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return out;
  }

}
