package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
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

  MascotResourcePool resourcePool;
  Network network;

  /**
   * Creates new test context.
   */
  public MascotTestContext(Integer myId, List<Integer> partyIds, BigInteger modulus,
      int modBitLength, int lambdaSecurityParam, int numLeftFactors, int prgSeedLength) {
    byte[] drbgSeed = new byte[prgSeedLength / 8];
    new Random(myId).nextBytes(drbgSeed);
    this.resourcePool = new DummyMascotResourcePoolImpl(myId, partyIds,
        new PaddingAesCtrDrbg(drbgSeed, prgSeedLength), modulus, modBitLength, lambdaSecurityParam,
        prgSeedLength, numLeftFactors);
    this.network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
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

  public List<Integer> getPartyIds() {
    return resourcePool.getPartyIds();
  }

  public MessageDigest getMessageDigest() {
    return resourcePool.getMessageDigest();
  }

  public int getModBitLength() {
    return resourcePool.getModBitLength();
  }

  public int getLambdaSecurityParam() {
    return resourcePool.getLambdaSecurityParam();
  }

  public int getNumLeftFactors() {
    return resourcePool.getNumCandidatesPerTriple();
  }

  public int getPrgSeedLength() {
    return resourcePool.getPrgSeedLength();
  }

  public FieldElementPrg getLocalSampler() {
    return resourcePool.getLocalSampler();
  }

  public FieldElementSerializer getFieldElementSerializer() {
    return resourcePool.getFieldElementSerializer();
  }

  public StrictBitVectorSerializer getStrictBitVectorSerializer() {
    return resourcePool.getStrictBitVectorSerializer();
  }

  public ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return resourcePool.getCommitmentSerializer();
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
