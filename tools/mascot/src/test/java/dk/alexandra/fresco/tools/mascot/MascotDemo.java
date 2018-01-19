package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.ot.base.NaorPinkasOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.io.Closeable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MascotDemo {

  private final Mascot mascot;
  private final Closeable toClose;
  private int networkBufferSize = 104856800;
  private MascotSecurityParameters parameters = new MascotSecurityParameters();
  private int instanceId = 1;

  MascotDemo(Integer myId, List<Integer> partyIds) {
    Network network =
        new KryoNetNetwork(defaultNetworkConfiguration(myId, partyIds), networkBufferSize, false,
            15000);
    MascotResourcePool resourcePool = defaultResourcePool(myId, partyIds,
        network);
    FieldElement macKeyShare = resourcePool.getLocalSampler().getNext(
        resourcePool.getModulus());
    toClose = (Closeable) network;
    mascot = new Mascot(resourcePool, network, macKeyShare);
  }

  private void run(int numIts, int numTriples) {
    for (int i = 0; i < numIts; i++) {
      System.out.println("Generating another triple batch.");
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

  private MascotResourcePool defaultResourcePool(Integer myId, List<Integer> partyIds,
      Network network) {
    // generate random seed for local DRBG
    byte[] drbgSeed = new byte[parameters.getPrgSeedLength() / 8];
    new SecureRandom().nextBytes(drbgSeed);
    Drbg drbg = new PaddingAesCtrDrbg(drbgSeed);
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (Integer otherId : partyIds) {
      if (myId != otherId) {
        Ot ot = new NaorPinkasOt(myId, otherId, drbg, network);
        RotList currentSeedOts = new RotList(drbg, parameters.getPrgSeedLength());
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
    return new MascotResourcePoolImpl(myId, partyIds.size(), instanceId, drbg, seedOts,
        new MascotSecurityParameters());
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
