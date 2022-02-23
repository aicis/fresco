package dk.alexandra.fresco.tools.bitTriples.commit;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePoolImpl;
import dk.alexandra.fresco.tools.bitTriples.cointossing.CoinTossingMpc;
import java.util.ArrayList;
import org.junit.Test;

public class CommitmentBasedInputTest {

  @Test (expected = IllegalArgumentException.class)
  public void openThrowsCorrectException() {
    CoinTossingMpc coinTossingMpc =
        new CoinTossingMpc(mock(BitTripleResourcePoolImpl.class), mock(Network.class));
    ArrayList<byte[]> notEmpty = new ArrayList<>();
    notEmpty.add(new byte[]{23});
    coinTossingMpc.open(new ArrayList<>(),notEmpty);
  }
}
