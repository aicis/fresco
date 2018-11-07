package dk.alexandra.fresco.framework.network;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.util.ExceptionConverter;

/**
 * Network that allows sending and receiving of bytes between the parties of a MPC computation.
 * Simple implementations will just transfer bytes over the wire, one message at a time, however
 * arbitrary complicated buffering mechanism can be implemented.
 * <br/>
 * This interface includes a friendly interface for the native protocols, i.e. send/receive to all.
 */
public interface Network {

  /**
   * Send data to other party with id partyId. Ownership of the data array is transferred to
   * the network through this call. This means that the caller cannot change the byte array after
   * this call nor assume it is unchanged by the network.
   *
   * @param partyId the party to send data to
   * @param data the data to send
   */
  void send(int partyId, byte[] data);

  /**
   * Blocking call that only returns once the data has been fully received. Ownership of the
   * byte array is given to the caller.
   *
   * @param partyId the party to receive from
   * @return the data send by the given partyId through the given channel
   */
  byte[] receive(int partyId);

  /**
   * Gets the total amount of players. Used for building the default methods.
   *
   * @return the number of different parties.
   */
  int getNoOfParties();

  /**
   * Retrieves input from all players (including yourself)
   *
   * @return A list of byte buffers where the data from party 1 resides at
   *     index 0 and so forth.
   */
  default List<byte[]> receiveFromAll() {
    List<byte[]> res = new ArrayList<>();
    for (int i = 1; i <= getNoOfParties(); i++) {
      res.add(receive(i));
    }
    return res;
  }

  /**
   * Queues up a value to be send to all parties (yourself included).
   *
   * @param data The value to send to all parties
   */
  default void sendToAll(byte[] data) {
    for (int i = 1; i <= getNoOfParties(); i++) {
      send(i, data);
    }
  }

  /**
   * Creates a map of party IDs to network address (uses localhost and supplied ports) information.
   *
   * @param n the number of parties
   * @param ports the ports to be used
   * @return a map of party IDs to network address information
   */
  static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n,
      List<Integer> ports) {
    Map<Integer, NetworkConfiguration> confs = new HashMap<>(n);
    Map<Integer, Party> partyMap = new HashMap<>();
    int id = 1;
    for (int port : ports) {
      partyMap.put(id, new Party(id, "localhost", port));
      id++;
    }
    for (int i = 0; i < n; i++) {
      confs.put(i + 1, new NetworkConfigurationImpl(i + 1, partyMap));
    }
    return confs;
  }

  /**
   * Finds {@code portsRequired} free ports and returns their port numbers. <p>NOTE: two subsequent
   * calls to this method can return overlapping sets of free ports (same with parallel calls).</p>
   *
   * @param portsRequired number of free ports required
   * @return list of port numbers of free ports
   */
  static List<Integer> getFreePorts(int portsRequired) {
    List<ServerSocket> sockets = new ArrayList<>(portsRequired);
    for (int i = 0; i < portsRequired; i++) {
      ServerSocket s = ExceptionConverter.safe(() -> new ServerSocket(0),
          "Could not create new server socket.");
      sockets.add(s);
        // we keep the socket open to ensure that the port is not re-used in a sub-sequent iteration
    }
    return sockets.stream().map(socket -> {
      int portNumber = socket.getLocalPort();
      ExceptionConverter.safe(() -> {
        socket.close();
        return null;
      }, "Could not close server port");
      return portNumber;
    }).collect(Collectors.toList());
  }

}
