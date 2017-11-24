package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;

public class MascotContext {

  Integer myId;
  List<Integer> partyIds;
  BigInteger modulus;
  int kBitLength;
  int lambdaSecurityParam;
  Network network;
  ExecutorService executor;
  Random rand;

  public MascotContext(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      int lambdaSecurityParameter, Network network, ExecutorService executor, Random rand) {
    super();
    this.myId = myId;
    this.partyIds = partyIds;
    this.modulus = modulus;
    this.kBitLength = kBitLength;
    this.lambdaSecurityParam = lambdaSecurityParameter;
    this.network = network;
    this.executor = executor;
    this.rand = rand;
  }

  public static MascotContext defaultContext(Integer myId, List<Integer> partyIds) {
    ExecutorService executor = Executors.newCachedThreadPool();
    Network network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
//    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    BigInteger modulus = new BigInteger("65521");
//    int modBitLength = 128;
    int modBitLength = 16;
//    int lambdaSecurityParam = 128;
    int lambdaSecurityParam = 16;
    Random rand = new Random(42);
    return new MascotContext(myId, partyIds, modulus, modBitLength, lambdaSecurityParam, network,
        executor, rand);
  }

  public Integer getMyId() {
    return myId;
  }

  public void setMyId(Integer myId) {
    this.myId = myId;
  }

  public List<Integer> getPartyIds() {
    return partyIds;
  }

  public void setPartyIds(List<Integer> partyIds) {
    this.partyIds = partyIds;
  }

  public BigInteger getModulus() {
    return modulus;
  }

  public void setModulus(BigInteger modulus) {
    this.modulus = modulus;
  }

  public int getkBitLength() {
    return kBitLength;
  }

  public void setkBitLength(int kBitLength) {
    this.kBitLength = kBitLength;
  }

  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  public void setLambdaSecurityParam(int lambdaSecurityParam) {
    this.lambdaSecurityParam = lambdaSecurityParam;
  }

  public Network getNetwork() {
    return network;
  }

  public void setNetwork(Network network) {
    this.network = network;
  }

  public ExecutorService getExecutor() {
    return executor;
  }

  public void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }

  public Random getRand() {
    return rand;
  }

  public void setRand(Random rand) {
    this.rand = rand;
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
