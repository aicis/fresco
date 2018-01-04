package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.io.Closeable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MascotDemo {

  private final Mascot mascot;
  private final Closeable toClose;

  MascotDemo(Integer myId, List<Integer> partyIds) {
    MascotResourcePool resourcePool = defaultResourcePool(myId, partyIds);
    FieldElement macKeyShare = resourcePool.getLocalSampler().getNext(resourcePool.getModulus(),
        resourcePool.getModBitLength());
    int bufferSize = 104856800;
    Network network =
        new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds), bufferSize, false, 15000);
    toClose = (Closeable) network;
    mascot = new Mascot(resourcePool, network, macKeyShare);
  }

  void run(int numIts, int numTriples) {
    for (int i = 0; i < numIts; i++) {
      long startTime = System.currentTimeMillis();
      List<MultTriple> triples = mascot.getTriples(numTriples);
      long endTime = System.currentTimeMillis();
      long total = endTime - startTime;
      System.out.println("Generated " + triples.size() + " triples in " + total + " ms");
    }
    Callable<Void> closeTask = () -> {
      toClose.close();
      return null;
    };
    ExceptionConverter.safe(closeTask, "Failed closing network");
  }

  private NetworkConfiguration defaultNetworkConfiguration(Integer myId, List<Integer> partyIds) {
    Map<Integer, Party> parties = new HashMap<>();
    for (Integer partyId : partyIds) {
      parties.put(partyId, new Party(partyId, "localhost", 8005 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

  MascotResourcePool defaultResourcePool(Integer myId, List<Integer> partyIds) {
    int modBitLength = 256;
    BigInteger modulus = ModulusFinder.findSuitableModulus(modBitLength);
    int lambdaSecurityParam = 128;
    int prgSeedLength = 256;
    int numLeftFactors = 3;
    // generate random seed for local DRBG
    byte[] drbgSeed = new byte[prgSeedLength / 8];
    new SecureRandom().nextBytes(drbgSeed);
    return new MascotResourcePoolImpl(myId, partyIds,
        new PaddingAesCtrDrbg(drbgSeed, prgSeedLength), modulus, modBitLength, lambdaSecurityParam,
        prgSeedLength, numLeftFactors);
  }

  /**
   * Runs demo.
   */
  public static void main(String[] args) {
    Integer myId = Integer.parseInt(args[0]);
    List<Integer> partyIds = Arrays.asList(1, 2);
    new MascotDemo(myId, partyIds).run(10, 1024);
  }

}
