package dk.alexandra.fresco.demo.helpers;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.network.ScapiNetworkImpl;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
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
      int myId, Map<Integer, Party> parties,
      NetworkingStrategy networkStrategy) {
    int channelAmount = 1;
    NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties);
    return buildNetwork(conf, channelAmount, networkStrategy);
  }

  private static Network buildNetwork(NetworkConfiguration conf, int channelAmount,
      NetworkingStrategy networkStrat) {
    Network network;
    switch (networkStrat) {
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
        throw new ConfigurationException("Unknown networking strategy " + networkStrat);
    }
    network.init(conf, channelAmount);

    return network;
  }

  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> ResourcePoolT createResourcePool(
      SCEConfiguration<ResourcePoolT> sceConf, ProtocolSuite<ResourcePoolT, Builder> suite,
      NetworkingStrategy networkStrategy)
      throws IOException {
    int myId = sceConf.getMyId();
    Map<Integer, Party> parties = sceConf.getParties();

    // Secure random by default.
    Random rand = new Random(0);
    SecureRandom secRand = new SecureRandom();

    Network network = getNetworkFromConfiguration(myId, parties, networkStrategy);
    network.connect(10000);

    ResourcePoolT resourcePool =
        (ResourcePoolT) suite.createResourcePool(myId, parties.size(), network, rand, secRand);

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
