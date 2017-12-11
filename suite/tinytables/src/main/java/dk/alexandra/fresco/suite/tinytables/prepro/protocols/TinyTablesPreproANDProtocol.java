package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;

/**
 * <p>
 * This class represents an AND protocol in the preprocessing phase of the TinyTables protocol.
 * </p>
 *
 * <p>
 * Here, each of the two players picks random shares for the mask of the output wire, <i>r
 * <sub>O</sub></i>. Each player also has to calculate a so called <i>TinyTable</i> for this
 * protocol, which are 2x2 matrices such that the <i>(c,d)</i>'th entries from the two tables is an
 * additive secret sharing of <i>(r<sub>u</sub> + c)(r<sub>v</sub> + d) + r<sub>o</sub></i>.
 * <p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesPreproANDProtocol extends TinyTablesPreproProtocol<SBool> {

  private DRes<SBool> inLeft, inRight;
  private TinyTablesPreproSBool out;

  public TinyTablesPreproANDProtocol(int id, DRes<SBool> inLeft, DRes<SBool> inRight,
      SBool out) {
    super();
    this.id = id;
    this.inLeft = inLeft;
    this.inRight = inRight;
    this.out = (TinyTablesPreproSBool) out;
  }

  public TinyTablesPreproSBool getInLeft() {
    return (TinyTablesPreproSBool) inLeft.out();
  }

  public TinyTablesPreproSBool getInRight() {
    return (TinyTablesPreproSBool) inRight.out();
  }

  public TinyTablesPreproSBool getOut() {
    return out;
  }

  @Override
  public SBool out() {
    return out;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {

    TinyTablesPreproProtocolSuite ps =
        TinyTablesPreproProtocolSuite.getInstance(resourcePool.getMyId());

    switch (round) {
      case 0:

        /*
         * Here we only pick the mask of the output wire. The TinyTable is calculated after all AND
         * gates has been preprocessed.
         */
        boolean rO = ps.getSecureRandom().nextBoolean();
        out = (out == null) ? new TinyTablesPreproSBool() : out;
        out.setValue(new TinyTablesElement(rO));

        /*
         * We need to finish the processing of this gate after all preprocessing is done (see
         * calculateTinyTable). To do this, we keep a reference to all AND gates.
         */
        ps.addANDGate(this);

        return EvaluationStatus.IS_DONE;

      default:
        throw new MPCException("Cannot evaluate more than one round");
    }
  }

  /**
   * Calculate the TinyTable for this gate.
   *
   * @param playerId The ID of this player.
   * @param product A share of the product of input values for this gate.
   */
  public TinyTable calculateTinyTable(int playerId, TinyTablesElement product) {

    TinyTablesElement[] entries = new TinyTablesElement[4];
    entries[0] = product.add(this.out.getValue());
    entries[1] = entries[0].add(getInLeft().getValue());
    entries[2] = entries[0].add(getInRight().getValue());
    entries[3] = entries[0].add(getInLeft().getValue()).add(getInRight().getValue()).not(playerId);
    return new TinyTable(entries);
  }

}
