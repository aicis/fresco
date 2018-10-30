package dk.alexandra.fresco.framework.configuration;

import dk.alexandra.fresco.framework.network.Network;

import java.util.List;
import java.util.Map;

/**
 * Network-related functionality such as creating valid network configurations and finding free
 * ports.
 */
public class NetworkTestUtils {

  private NetworkTestUtils() {
  }

  /**
   * As getConfigurations(n, ports) but tries to find free ephemeral ports (but note that there is
   * no guarantee that ports will remain unused).
   */
  public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n) {
    List<Integer> ports = Network.getFreePorts(n);
    return Network.getNetworkConfigurations(n, ports);
  }

}
