package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;

public class MascotTestContext {

  MascotResourcePool resourcePool;
  Network network;

  public MascotTestContext(Integer myId, List<Integer> partyIds) {
    BigInteger modulus = new BigInteger("65521");
    int modBitLength = 16;
    int lambdaSecurityParam = 16;
    int prgSeedLength = 256;
    int numLeftFactors = 3;
    this.resourcePool = new MascotResourcePoolImpl(myId, partyIds, modulus, modBitLength,
        lambdaSecurityParam, prgSeedLength, numLeftFactors);
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
    return resourcePool.getNumLeftFactors();
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

  public CommitmentSerializer getCommitmentSerializer() {
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
