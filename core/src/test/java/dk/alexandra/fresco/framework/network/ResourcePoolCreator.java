package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ResourcePoolCreator<ResourcePoolT extends ResourcePool> {
  
  private static Map<Integer, ResourcePool> rps = new HashMap<>();
  
  public static Map<Integer, ResourcePool> getCurrentResourcePools() {
    return rps;
  }
  
  private static Network getNetworkFromConfiguration(SCEConfiguration sceConf,
      int myId, Map<Integer, Party> parties) {
    int channelAmount = 1;
    NetworkConfiguration conf = new NetworkConfigurationImpl(myId, parties);
    return buildNetwork(conf, channelAmount, sceConf.getNetworkStrategy());
  }

  private static Network buildNetwork(NetworkConfiguration conf,
      int channelAmount, NetworkingStrategy networkStrat) {
    Network network;
    switch (networkStrat) {
      case KRYONET:
        // TODO[PSN]
        // KryoNet currently works on mac, but Windows is still in the dark.
        //network = new KryoNetNetwork();
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
  
  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> ResourcePoolT createResourcePool(TestSCEConfiguration<ResourcePoolT, Builder> sceConf) throws IOException {
    int myId = sceConf.getMyId();
    Map<Integer, Party> parties = sceConf.getParties();
  
    // Secure random by default.
    Random rand = new Random(0);
    SecureRandom secRand = new SecureRandom();
  
    Network network = getNetworkFromConfiguration(sceConf, myId, parties);
    network.connect(10000);

    ProtocolSuite<ResourcePoolT, Builder> suite = sceConf.getSuite();

    ResourcePoolT resourcePool = suite
        .createResourcePool(myId, parties.size(), network, rand, secRand);
      
    ResourcePoolCreator.rps.put(myId, resourcePool);
    
    return resourcePool;
  
  }

}
