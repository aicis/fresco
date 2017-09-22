package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;

public class NetworkCreator {

  public static Network getNetworkFromConfiguration(NetworkingStrategy networkStrategy,
      NetworkConfiguration networkConfiguration, PerformanceLogger pl) {
    int channelAmount = 1;
    Network network;
    switch (networkStrategy) {
      case KRYONET:
        network = new KryoNetNetwork();
        break;
      default:
        throw new ConfigurationException("Unknown networking strategy " + networkStrategy);
    }
    if (pl != null) {
      // Decorate with performance logger
      network = new NetworkPerformanceDecorator(network, pl);
    }
    network.init(networkConfiguration, channelAmount);

    return network;
  }

}
