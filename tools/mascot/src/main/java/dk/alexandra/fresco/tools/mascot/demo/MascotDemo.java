package dk.alexandra.fresco.tools.mascot.demo;

import java.io.Closeable;
import java.io.IOException;
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
import dk.alexandra.fresco.tools.mascot.field.MultTriple;

public class MascotDemo {

  private Mascot mascot;
  private Closeable toClose;

  public MascotDemo(Integer myId, List<Integer> partyIds) {
    MascotResourcePool resourcePool = defaultResourcePool(myId, partyIds);
    FieldElement macKeyShare = resourcePool.getLocalSampler()
        .getNext(resourcePool.getModulus(), resourcePool.getModBitLength());
    Network network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
    toClose = (Closeable) network;
    mascot = new Mascot(resourcePool, network, macKeyShare);
  }

  public void run() {
    List<MultTriple> triples = mascot.getTriples(10);
    System.out.println(triples.size());
    try {
      toClose.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
