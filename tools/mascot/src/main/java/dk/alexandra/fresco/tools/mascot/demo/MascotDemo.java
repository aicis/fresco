package dk.alexandra.fresco.tools.mascot.demo;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.mascot.DummyMascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import java.io.Closeable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;


public class MascotDemo {

  private Mascot mascot;
  private Closeable toClose;

  MascotDemo(Integer myId, List<Integer> partyIds) {
    MascotResourcePool resourcePool = defaultResourcePool(myId, partyIds);
    FieldElement macKeyShare = resourcePool.getLocalSampler().getNext(resourcePool.getModulus(),
        resourcePool.getModBitLength());
    Network network = new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds));
    toClose = (Closeable) network;
    mascot = new Mascot(resourcePool, network, macKeyShare);
  }

  void run() {
    for (int i = 0; i < 100; i++) {
      long startTime = System.currentTimeMillis();
      List<MultTriple> triples = mascot.getTriples(256);
      long endTime = System.currentTimeMillis();
      System.out
          .println("Generated " + triples.size() + " triples in " + (endTime - startTime) + " ms");
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
    BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
    int modBitLength = 128;
    int lambdaSecurityParam = 128;
    int prgSeedLength = 256;
    int numLeftFactors = 3;
    byte[] drbgSeed = new byte[prgSeedLength / 8];
    // TODO change back to secure random and non-dummy
    new Random(myId).nextBytes(drbgSeed);
    return new DummyMascotResourcePoolImpl(myId, partyIds,
        new PaddingAesCtrDrbg(drbgSeed, prgSeedLength), modulus, modBitLength, lambdaSecurityParam,
        prgSeedLength, numLeftFactors);
  }

  /**
   * Runs demo.
   */
  public static void main(String[] args) {
    Integer myId = Integer.parseInt(args[0]);
    List<Integer> partyIds = Arrays.asList(1, 2);
    new MascotDemo(myId, partyIds).run();
  }

}
