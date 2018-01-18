package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
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
  public MascotTestContext(Integer myId, List<Integer> partyIds, int instanceId,
      BigInteger modulus, int modBitLength, int lambdaSecurityParam,
      int numLeftFactors, int prgSeedLength) {
    this.network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
    byte[] drbgSeed = new byte[prgSeedLength / 8];
    new Random(myId).nextBytes(drbgSeed);
    Drbg drbg = new PaddingAesCtrDrbg(drbgSeed);
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (Integer otherId : partyIds) {
      if (myId != otherId) {
        Ot ot = new DummyOt(otherId, network);
        RotList currentSeedOts = new RotList(drbg, prgSeedLength);
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
    this.resourcePool = new MascotResourcePoolImpl(myId, partyIds,
        instanceId, drbg, seedOts, modulus,
        modBitLength, lambdaSecurityParam, prgSeedLength, numLeftFactors);
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

  public List<Integer> getPartyIds() {
    return resourcePool.getPartyIds();
  }

  public int getModBitLength() {
    return resourcePool.getModBitLength();
  }

  public int getPrgSeedLength() {
    return resourcePool.getPrgSeedLength();
  }

  public Network getNetwork() {
    return network;
  }

  private static NetworkConfiguration defaultNetworkConfiguration(Integer myId,
      List<Integer> partyIds) {
    Map<Integer, Party> parties = new HashMap<>();
    for (Integer partyId : partyIds) {
      parties.put(partyId, new Party(partyId, "localhost", 8000 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

}
