package dk.alexandra.fresco.framework.network.socket;

import dk.alexandra.fresco.framework.network.Network;
import java.net.Socket;
import java.util.Map;

/**
 * Connects a network using sockets.
 *
 * <p>
 * This allows us to factor out the logic for connecting the network (such as various handshake
 * strategies) from the {@link Network} class.
 * </p>
 */
public interface NetworkConnector {

  /**
   * Gets a mapping from a party id to a socket connected to the given party.
   *
   * <p>
   * Note: that by convention the id's should be consecutive starting from 1. However, a mapping is
   * not required for the party that created this NetworkConnector
   * (as there is no reason to connect the party with a socket to it self).
   * </p>
   *
   * @return map from party id to socket
   */
  Map<Integer, Socket> getSocketMap();

}
