package dk.alexandra.fresco.tools.bitTriples;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Context for a single party to participate in networked test. Holds a resource pool and network.
 */
public class BitTriplesTestContext {

  private final BitTripleResourcePool resourcePool;
  private final Network network;

  /**
   * Creates new test context.
   */
  public BitTriplesTestContext(int myId, int noOfParties, int instanceId,
      BitTripleSecurityParameters securityParameters) {
    this.network = new SocketNetwork(defaultNetworkConfiguration(myId, noOfParties));
    byte[] drbgSeed = new byte[securityParameters.getPrgSeedBitLength() / 8];
    new Random(myId).nextBytes(drbgSeed); // Parties need to have same seed.
    Drbg drbg = AesCtrDrbgFactory.fromDerivedSeed(drbgSeed);
    this.resourcePool = new BitTripleResourcePoolImpl(myId, noOfParties, instanceId, drbg,
        securityParameters);
  }

  public BitTripleResourcePool getResourcePool() {
    return resourcePool;
  }

  public int getMyId() {
    return resourcePool.getMyId();
  }

  public int getNoOfParties() {
    return resourcePool.getNoOfParties();
  }

  public int getPrgSeedLength() {
    return resourcePool.getPrgSeedBitLength();
  }
  public int getComputationalSecurityBitParameter() {
    return resourcePool.getComputationalSecurityBitParameter();
  }

  public Network getNetwork() {
    return network;
  }

  private static NetworkConfiguration defaultNetworkConfiguration(int myId,
      int noOfParties) {
    Map<Integer, Party> parties = new HashMap<>();
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      parties.put(partyId, new Party(partyId, "localhost", 8000 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

}
