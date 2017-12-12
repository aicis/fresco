package dk.alexandra.fresco.tools.mascot.demo;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.MascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MascotDemo {

  private Mascot mascot;

  public MascotDemo(Integer myId, List<Integer> partyIds) {
    MascotResourcePool resourcePool = defaultSmallModulusResourcePool(myId, partyIds);
    FieldElement macKeyShare = resourcePool.getLocalSampler()
        .getNext(resourcePool.getModulus(), resourcePool.getModBitLength());
    Network network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
    mascot = new Mascot(resourcePool, macKeyShare, network);
    mascot.initialize();
  }

  public void run() {
    System.out.println(mascot.getTriples(10));
  }

  public static void main(String[] args) {
    Integer myId = Integer.parseInt(args[0]);
    List<Integer> partyIds = Arrays.asList(1, 2);
    new MascotDemo(myId, partyIds).run();
  }

  // TODO where should this go?
  private static NetworkConfiguration defaultNetworkConfiguration(Integer myId,
      List<Integer> partyIds) {
    Map<Integer, Party> parties = new HashMap<>();
    for (Integer partyId : partyIds) {
      parties.put(partyId, new Party(partyId, "localhost", 8000 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

  MascotResourcePool defaultSmallModulusResourcePool(Integer myId, List<Integer> partyIds) {
    BigInteger modulus = new BigInteger("65521");
    int modBitLength = 16;
    int lambdaSecurityParam = 16;
    int prgSeedLength = 256;
    int numLeftFactors = 3;
    return new MascotResourcePoolImpl(myId, partyIds, modulus, modBitLength, lambdaSecurityParam,
        prgSeedLength, numLeftFactors);
  }

  MascotResourcePool defaultResourcePool(Integer myId, List<Integer> partyIds) {
    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    int modBitLength = 128;
    int lambdaSecurityParam = 128;
    int prgSeedLength = 256;
    int numLeftFactors = 3;
    return new MascotResourcePoolImpl(myId, partyIds, modulus, modBitLength, lambdaSecurityParam,
        prgSeedLength, numLeftFactors);
  }

}
