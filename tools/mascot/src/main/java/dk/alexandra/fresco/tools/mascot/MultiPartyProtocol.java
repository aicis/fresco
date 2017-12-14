package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import java.util.List;


public class MultiPartyProtocol extends BaseProtocol {
  
  public MultiPartyProtocol(MascotResourcePool resourcePool, Network network) {
    super(resourcePool, network);
  }

  public List<Integer> getPartyIds() {
    return resourcePool.getPartyIds();
  }

}
