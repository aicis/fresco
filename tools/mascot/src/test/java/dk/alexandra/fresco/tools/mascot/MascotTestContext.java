package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Context for a single party to participate in networked test. Holds a resource pool and network.
 */
public class MascotTestContext {

  private final MascotResourcePool resourcePool;
  private final Network network;

  /**
   * Creates new test context.
   */
  public MascotTestContext(int myId, int noOfParties, int instanceId,
      MascotSecurityParameters securityParameters) {
    this.network = new KryoNetNetwork(defaultNetworkConfiguration(myId, noOfParties));
    byte[] drbgSeed = new byte[securityParameters.getPrgSeedLength() / 8];
    new Random(myId).nextBytes(drbgSeed);
    Drbg drbg = new PaddingAesCtrDrbg(drbgSeed);
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (int otherId = 1; otherId <= noOfParties; otherId++) {
      if (myId != otherId) {
        Ot ot = new DummyOt(otherId, network);
        RotList currentSeedOts = new RotList(drbg, securityParameters.getPrgSeedLength());
        if (myId < otherId) {
          currentSeedOts.send(ot);
          currentSeedOts.receive(ot);
        } else {
          currentSeedOts.receive(ot);
          currentSeedOts.send(ot);
        }
        seedOts.put(otherId, currentSeedOts);
      }
    }
    this.resourcePool = new MascotResourcePoolImpl(myId, noOfParties, instanceId, drbg, seedOts,
        securityParameters);
  }

  public MascotResourcePool getResourcePool() {
    return resourcePool;
  }

  public BigInteger getModulus() {
    return resourcePool.getModulus();
  }

  public int getMyId() {
    return resourcePool.getMyId();
  }

  public int getNoOfParties() {
    return resourcePool.getNoOfParties();
  }

  public int getPrgSeedLength() {
    return resourcePool.getPrgSeedLength();
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
