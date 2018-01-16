package dk.alexandra.fresco.suite.tinytables.online.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class represents an open-to-all protocol in the TinyTables preprocessing phase.
 * </p>
 *
 * <p>
 * Here, each of the two players send his share of the masking parameter <i>r</i> to the other
 * player such that each player can add this to the masked input <i>e = b + r</i> to get the
 * unmasked output <i>b</i>.
 * </p>
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesOpenToAllProtocol extends TinyTablesProtocol<Boolean> {

  private int id;
  private DRes<SBool> toOpen;
  private Boolean opened;

  public TinyTablesOpenToAllProtocol(int id, DRes<SBool> toOpen) {
    super();
    this.id = id;
    this.toOpen = toOpen;
  }

  @Override
  public EvaluationStatus evaluate(int round, ResourcePoolImpl resourcePool, Network network) {
    TinyTablesProtocolSuite ps = TinyTablesProtocolSuite.getInstance(resourcePool.getMyId());

    /*
     * When opening a value, all players send their shares of the masking value r to the other
     * players, and each player can then calculate the unmasked value as the XOR of the masked value
     * and all the shares of the mask.
     */
    if (round == 0) {
      TinyTablesElement myR = ps.getStorage().getMaskShare(id);
      network.sendToAll(new byte[]{BooleanSerializer.toBytes(myR.getShare())});
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      // round > 0
      List<byte[]> buffers = network.receiveFromAll();
      List<TinyTablesElement> maskShares = new ArrayList<>();
      for (byte[] buffer : buffers) {
        maskShares.add(new TinyTablesElement(BooleanSerializer.fromBytes(buffer[0])));
      }
      boolean mask = TinyTablesElement.open(maskShares);
      this.opened = ((TinyTablesSBool) toOpen.out()).getValue().getShare() ^ mask;
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Boolean out() {
    return opened;
  }

}
