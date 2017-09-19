package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;

public class ResourcePoolCreator {

  public static Network getNetworkFromConfiguration(NetworkingStrategy networkStrategy,
      NetworkConfiguration networkConfiguration) {
    int channelAmount = 1;
    Network network;
    switch (networkStrategy) {
      case KRYONET:
        network = new KryoNetNetwork();
        break;
      default:
        throw new ConfigurationException("Unknown networking strategy " + networkStrategy);
    }
    network.init(networkConfiguration, channelAmount);

    return network;
  }

}
