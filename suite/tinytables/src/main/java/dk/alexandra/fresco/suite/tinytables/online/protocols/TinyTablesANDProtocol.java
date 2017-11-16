package dk.alexandra.fresco.suite.tinytables.online.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class represents an AND protocol in the TinyTables protocol's online phase.
 * </p>
 * <p>
 * Here it is assumed that each of the two players have computed a TinyTable for the protocol such
 * that the <i>(c,d)</i>'th entries from the two tables is an additive secret sharing of <i>(r
 * <sub>u</sub> + c)(r<sub>v</sub> + d) + r<sub>o</sub></i>.
 * <p>
 * Now, both players know the encrypted inputs of the input wires wires <i>e<sub>u</sub> = b
 * <sub>u</sub>+r<sub>u</sub></i> and <i>e<sub>v</sub> = b<sub>v</sub>+r<sub>v</sub></i> where <i>b
 * <sub>u</sub></i> and <i>b<sub>v</sub></i> are the clear text bits, and each now looks up entry
 * <i>(e<sub>u</sub>, e<sub>v</sub>)</i> in his TinyTable and shares this with the other player.
 * Both players now add their share with the other players share to get the masked value of the
 * output wire.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesANDProtocol extends TinyTablesProtocol<SBool> {

  private int id;
  private DRes<SBool> inLeft, inRight;
  private TinyTablesSBool out;

  public TinyTablesANDProtocol(int id, DRes<SBool> inLeft, DRes<SBool> inRight) {
    this.id = id;
    this.inLeft = inLeft;
    this.inRight = inRight;
    this.out = new TinyTablesSBool();
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {
    TinyTablesProtocolSuite ps = TinyTablesProtocolSuite.getInstance(resourcePool.getMyId());

    switch (round) {
      case 0:
        TinyTable tinyTable = ps.getStorage().getTinyTable(id);
        if (tinyTable == null) {
          throw new MPCException("Unable to find TinyTable for gate with id " + id);
        }
        TinyTablesElement myShare = tinyTable.getValue(((TinyTablesSBool) inLeft.out()).getValue(),
            ((TinyTablesSBool) inRight.out()).getValue());

        network.sendToAll(new byte[]{BooleanSerializer.toBytes(myShare.getShare())});
        return EvaluationStatus.HAS_MORE_ROUNDS;
      case 1:
        List<byte[]> buffers = network.receiveFromAll();
        List<TinyTablesElement> shares = new ArrayList<>();
        for (byte[] bytes : buffers) {
          shares.add(new TinyTablesElement(BooleanSerializer.fromBytes(bytes[0])));
        }
        boolean open = TinyTablesElement.open(shares);
        this.out = (out == null) ? new TinyTablesSBool() : out;
        this.out.setValue(new TinyTablesElement(open));
        return EvaluationStatus.IS_DONE;
      default:
        throw new MPCException("Cannot evaluate rounds larger than 0");
    }
  }

  @Override
  public SBool out() {
    return out;
  }

}
