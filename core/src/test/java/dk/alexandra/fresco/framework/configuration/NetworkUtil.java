package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.util.ExceptionConverter;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkUtil {
  /**
   * Creates a map of party IDs to network address (uses localhost and supplied ports) information.
   *
   * @return a map of party IDs to network address information
   */
  public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(List<Integer> ports) {
    Map<Integer, NetworkConfiguration> confs = new HashMap<>(ports.size());
    Map<Integer, Party> partyMap = new HashMap<>();
    int id = 1;
    for (int port : ports) {
      partyMap.put(id, new Party(id, "localhost", port));
      id++;
    }
    for (int i = 0; i < ports.size(); i++) {
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
  public static List<Integer> getFreePorts(int portsRequired) {
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

  /**
   * As getConfigurations(n, ports) but tries to find free ephemeral ports (but note that there is
   * no guarantee that ports will remain unused).
   */
  public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n) {
    List<Integer> ports = getFreePorts(n);
    return getNetworkConfigurations(ports);
  }
}
