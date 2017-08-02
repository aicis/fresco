package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResourcePoolCreator {

  private static ConcurrentMap<Integer, ResourcePool> rps = new ConcurrentHashMap<>();

  private static Network getNetworkFromConfiguration(NetworkingStrategy networkStrategy,
      NetworkConfiguration networkConfiguration) {
    int channelAmount = 1;
    Network network;
    switch (networkStrategy) {
      case KRYONET:
        network = new KryoNetNetwork();
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
      TestSCEConfiguration<ResourcePoolT, Builder> sceConf) throws IOException {
    int myId = sceConf.getNetworkConfiguration().getMyId();

    // Secure random by default.
    Random rand = new Random(0);
    SecureRandom secRand = new SecureRandom();

    Network network = getNetworkFromConfiguration(sceConf.getNetworkStrategy(),
        sceConf.getNetworkConfiguration());
    network.connect(10000);

    ProtocolSuite<ResourcePoolT, Builder> suite = sceConf.getSuite();

    ResourcePoolT resourcePool = suite.createResourcePool(myId,
        sceConf.getNetworkConfiguration().noOfParties(), network, rand, secRand);

    ResourcePoolCreator.rps.put(myId, resourcePool);

    return resourcePool;

  }

  public static Map<Integer, ResourcePool> getCurrentResourcePools() {
    return rps;
  }

}
