package dk.alexandra.fresco.demo.helpers;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.network.ScapiNetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ResourcePoolHelper {

  private static Map<Integer, ResourcePool> networks = new HashMap<>();

  private static Network getNetworkFromConfiguration(
      NetworkingStrategy networkStrategy, NetworkConfiguration networkConfiguration) {
    int channelAmount = 1;
    Network network;
    switch (networkStrategy) {
      case KRYONET:
        // TODO[PSN]
        // KryoNet currently works on mac, but Windows is still in the dark.
        // network = new KryoNetNetwork();
        network = new ScapiNetworkImpl();
        break;
      case SCAPI:
        network = new ScapiNetworkImpl();
        break;
      default:
        throw new ConfigurationException("Unknown networking strategy " + networkStrategy);
    }
    network.init(networkConfiguration, channelAmount);

    return network;
  }

  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> ResourcePoolT createResourcePool(
      ProtocolSuite<ResourcePoolT, Builder> suite,
      NetworkingStrategy networkStrategy,
      NetworkConfiguration networkConfiguration)
      throws IOException {
    int myId = networkConfiguration.getMyId();

    // Secure random by default.
    Random rand = new Random(0);
    SecureRandom secRand = new SecureRandom();

    Network network = getNetworkFromConfiguration(networkStrategy, networkConfiguration);
    network.connect(10000);

    ResourcePoolT resourcePool =
        suite.createResourcePool(myId, networkConfiguration.noOfParties(), network, rand, secRand);

    ResourcePoolHelper.networks.put(myId, resourcePool);

    return resourcePool;

  }

  /**
   * Closes the last network created. Demonstrator applications needs to only create one resource
   * pool at a time, or the previously one will be lost.
   */
  public static void shutdown() {
    try {
      for (int id : networks.keySet()) {
        networks.get(id).getNetwork().close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
