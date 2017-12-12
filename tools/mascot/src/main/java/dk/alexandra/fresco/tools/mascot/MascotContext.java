package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.PaddingPrg;

public class MascotContext {
  // TODO this getting very cluttered
  // party info
  Integer myId;
  List<Integer> partyIds;
  // security parameters
  BigInteger modulus;
  int kBitLength;
  int lambdaSecurityParam;
  int prgSeedLength;
  int numLeftFactors;
  FieldElement macKeyShare;
  // "resources"
  Network network;
  Random rand;
  FieldElementPrg localSampler;
  // should prgs go here?
  // serializer resources
  FieldElementSerializer feSerializer;
  StrictBitVectorSerializer sbvSerializer;
  CommitmentSerializer commSerializer;

  public MascotContext(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      int lambdaSecurityParameter, int prgSeedLength, int numLeftFactors, FieldElement macKeyShare,
      Network network, Random rand, FieldElementPrg localSampler) {
    super();
    this.myId = myId;
    this.partyIds = partyIds;
    this.modulus = modulus;
    this.kBitLength = kBitLength;
    this.lambdaSecurityParam = lambdaSecurityParameter;
    this.prgSeedLength = prgSeedLength;
    this.numLeftFactors = numLeftFactors;
    this.macKeyShare = macKeyShare;
    this.network = network;
    this.rand = rand;
    this.localSampler = localSampler;
    this.feSerializer = new FieldElementSerializer(modulus, kBitLength);
    this.commSerializer = new CommitmentSerializer();
    this.sbvSerializer = new StrictBitVectorSerializer();
  }

  public static MascotContext defaultTestingContext(Integer myId, List<Integer> partyIds) {
    Network network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
    // BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    BigInteger modulus = new BigInteger("65521");
    // int modBitLength = 128;
    int modBitLength = 16;
    // int lambdaSecurityParam = 128;
    int lambdaSecurityParam = 16;
    int prgSeedLength = 256;
    int numLeftFactors = 3;
    Random rand = new Random(myId);
    FieldElementPrg localSampler = new PaddingPrg(new StrictBitVector(prgSeedLength, rand));
    FieldElement macKeyShare = localSampler.getNext(modulus, modBitLength);
    return new MascotContext(myId, partyIds, modulus, modBitLength, lambdaSecurityParam,
        prgSeedLength, numLeftFactors, macKeyShare, network, rand, localSampler);
  }

  public static MascotContext defaultContext(Integer myId, List<Integer> partyIds) {
    Network network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    int modBitLength = 128;
    int lambdaSecurityParam = 128;
    int prgSeedLength = 256;
    int numLeftFactors = 3;
    Random rand = new Random(myId);
    FieldElementPrg localSampler = new PaddingPrg(new StrictBitVector(prgSeedLength, rand));
    FieldElement macKeyShare = localSampler.getNext(modulus, modBitLength);
    return new MascotContext(myId, partyIds, modulus, modBitLength, lambdaSecurityParam,
        prgSeedLength, numLeftFactors, macKeyShare, network, rand, localSampler);
  }

  public FieldElementPrg getLocalSampler() {
    return localSampler;
  }

  public int getNumLeftFactors() {
    return numLeftFactors;
  }

  public Integer getMyId() {
    return myId;
  }

  public List<Integer> getPartyIds() {
    return partyIds;
  }

  public BigInteger getModulus() {
    return modulus;
  }

  public int getkBitLength() {
    return kBitLength;
  }

  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  public Network getNetwork() {
    return network;
  }

  public Random getRand() {
    return rand;
  }

  public FieldElementSerializer getFeSerializer() {
    return feSerializer;
  }

  public CommitmentSerializer getCommSerializer() {
    return commSerializer;
  }

  public StrictBitVectorSerializer getSbvSerializer() {
    return sbvSerializer;
  }

  public int getPrgSeedLength() {
    return prgSeedLength;
  }

  public FieldElement getMacKeyShare() {
    return macKeyShare;
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
