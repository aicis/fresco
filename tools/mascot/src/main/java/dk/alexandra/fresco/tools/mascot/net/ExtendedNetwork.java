package dk.alexandra.fresco.tools.mascot.net;

import dk.alexandra.fresco.framework.network.Network;

public interface ExtendedNetwork extends Network {

  // TODO: extend this to fully merge SceNetwork, Network, ... ? 

  public void sendToAll(byte[] data);

}
