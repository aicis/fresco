package dk.alexandra.fresco.tools.mascot.net;

import java.util.Map;

import dk.alexandra.fresco.framework.network.Network;

public interface ExtendedNetwork extends Network {

  // TODO: extend this to fully merge SceNetwork, Network, ... ? 

  public void sendToAll(byte[] data);

  public Map<Integer, byte[]> receiveFromAll();

}
