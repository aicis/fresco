package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConfiguration {

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
   * As getConfigurations(n, ports) but tries to find free ephemeral
   * ports (but note that there is no guarantee that ports will remain
   * unused).
   */
  public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n) {
    List<Integer> ports = getFreePorts(n);
    return getNetworkConfigurations(n, ports);
  }

  private static List<Integer> getFreePorts(int n) {
    try {
      List<Integer> ports = new ArrayList<>(n);
      for (int i = 0; i < n; i++) {
        ServerSocket s = new ServerSocket(0);
        ports.add(s.getLocalPort());
        s.close();
      }
      return ports;
    } catch (IOException e) {
      throw new MPCException("Could not allocate free ports", e);

    }
  }

}
