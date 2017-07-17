package dk.alexandra.fresco.framework.network;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;

public class NetworkCreator {

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
        // This might work on mac?
        network = new KryoNetNetwork();
        //network = new ScapiNetworkImpl();
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
  
  public static ResourcePoolImpl createResourcePool(SCEConfiguration sceConf, ProtocolSuiteConfiguration<ResourcePool, ProtocolBuilder> suite) throws IOException {
    int myId = sceConf.getMyId();
    Map<Integer, Party> parties = sceConf.getParties();
  
    StreamedStorage streamedStorage = sceConf.getStreamedStorage();
    // Secure random by default.
    Random rand = new Random(0);
    SecureRandom secRand = new SecureRandom();
  
    Network network = getNetworkFromConfiguration(sceConf, myId, parties);
    network.connect(10000);
  
    ResourcePoolImpl resourcePool =
        new ResourcePoolImpl(myId, parties.size(),
            network, rand, secRand);
      
    NetworkCreator.rps.put(myId, resourcePool);
    
    return resourcePool;
  
  }

}
