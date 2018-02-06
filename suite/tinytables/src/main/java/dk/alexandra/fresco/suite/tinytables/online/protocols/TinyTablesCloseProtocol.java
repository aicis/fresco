package dk.alexandra.fresco.suite.tinytables.online.protocols;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproCloseProtocol;

/**
 * <p>
 * This class represents a close protocol in the online phase of the TinyTables protocol.
 * </p>
 * <p>
 * Here one of the players is the inputter and knows the unmasked input value <i>b</i>. During the
 * preprocessing phase (see {@link TinyTablesPreproCloseProtocol}, the inputter picked a random mask
 * <i>r</i>. Now, he sends the masked value <i>e = b + r</i> to the other player.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesCloseProtocol extends TinyTablesProtocol<SBool> {

  private int id;
  private int inputter;
  private Boolean in;
  private TinyTablesSBool out;

  public TinyTablesCloseProtocol(int id, int inputter, Boolean in) {
    this.id = id;
    this.inputter = inputter;
    this.in = in;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {
    TinyTablesProtocolSuite ps = TinyTablesProtocolSuite.getInstance(resourcePool.getMyId());
    if (round == 0) {
      if (resourcePool.getMyId() == this.inputter) {
        TinyTablesElement r = ps.getStorage().getMaskShare(id);
        TinyTablesElement e = new TinyTablesElement(this.in ^ r.getShare());
        network.sendToAll(new byte[]{BooleanSerializer.toBytes(e.getShare())});
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      TinyTablesElement share =
          new TinyTablesElement(BooleanSerializer.fromBytes(network.receive(this.inputter)[0]));
      out = new TinyTablesSBool(share);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SBool out() {
    return out;
  }

}
