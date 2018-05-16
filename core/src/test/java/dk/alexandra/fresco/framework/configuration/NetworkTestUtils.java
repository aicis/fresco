package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.Party;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkTestUtils {

  public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n,
      List<Integer> ports) {
    Map<Integer, NetworkConfiguration> confs = new HashMap<>(n);
    for (int i = 0; i < n; i++) {
      Map<Integer, Party> partyMap = new HashMap<>();
      int id = 1;
      for (int port : ports) {
        partyMap.put(id, new Party(id, "localhost", port));
        id++;
      }
      confs.put(i + 1, new NetworkConfigurationImpl(i + 1, partyMap));
    }
    return confs;
  }

  /**
   * As getConfigurations(n, ports) but tries to find free ephemeral ports (but note that there is
   * no guarantee that ports will remain unused).
   */
  public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n) {
    List<Integer> ports = getFreePorts(n);
    return getNetworkConfigurations(n, ports);
  }

  /**
   * Finds {@code portsRequired} free ports and returns their port numbers.
   *
   * @param portsRequired number of free ports required
   * @return list of port numbers of free ports
   */
  public static List<Integer> getFreePorts(int portsRequired) {
    List<ServerSocket> sockets = new ArrayList<>(portsRequired);
    for (int i = 0; i < portsRequired; i++) {
      try {
        ServerSocket s = new ServerSocket(0);
        sockets.add(s);
        // we keep the socket open to ensure that the port is not re-used in a sub-sequent iteration
      } catch (IOException e) {
        throw new RuntimeException("No free ports", e);
      }
    }
    return sockets.stream().map(socket -> {
      int portNumber = socket.getLocalPort();
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return portNumber;
    }).collect(Collectors.toList());
  }

}
