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
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;

public class MascotContext {
  // TODO this getting very cluttered
  Integer myId;
  List<Integer> partyIds;
  BigInteger modulus;
  int kBitLength;
  int lambdaSecurityParam;
  Network network;
  Random rand;
  FieldElementSerializer feSerializer;
  StrictBitVectorSerializer sbvSerializer;
  CommitmentSerializer commSerializer;

  public MascotContext(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      int lambdaSecurityParameter, Network network, Random rand) {
    super();
    this.myId = myId;
    this.partyIds = partyIds;
    this.modulus = modulus;
    this.kBitLength = kBitLength;
    this.lambdaSecurityParam = lambdaSecurityParameter;
    this.network = network;
    this.rand = rand;
    this.feSerializer = new FieldElementSerializer(modulus, kBitLength);
    this.commSerializer = new CommitmentSerializer();
    this.sbvSerializer = new StrictBitVectorSerializer();
  }

  public static MascotContext defaultContext(Integer myId, List<Integer> partyIds) {
    Network network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
    // BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    BigInteger modulus = new BigInteger("65521");
    // int modBitLength = 128;
    int modBitLength = 16;
    // int lambdaSecurityParam = 128;
    int lambdaSecurityParam = 16;
    Random rand = new Random(myId);
    return new MascotContext(myId, partyIds, modulus, modBitLength, lambdaSecurityParam, network,
        rand);
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

  private static NetworkConfiguration defaultNetworkConfiguration(Integer myId,
      List<Integer> partyIds) {
    Map<Integer, Party> parties = new HashMap<>();
    for (Integer partyId : partyIds) {
      parties.put(partyId, new Party(partyId, "localhost", 8000 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

}
