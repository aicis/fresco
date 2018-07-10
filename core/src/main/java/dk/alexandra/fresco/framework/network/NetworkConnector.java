package dk.alexandra.fresco.framework.network;

import java.net.Socket;
import java.util.Map;

public interface NetworkConnector {


  /**
   * Gets a mapping from party id to a socket for communicating with the given party.
   *
   * @return map from party id to socket
   */
  Map<Integer, Socket> getSocketMap();

}
