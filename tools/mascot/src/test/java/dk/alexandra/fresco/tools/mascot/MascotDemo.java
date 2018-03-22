package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.async.AsyncNetwork;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.ot.base.DhParameters;
import dk.alexandra.fresco.tools.ot.base.NaorPinkasOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.io.Closeable;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.crypto.spec.DHParameterSpec;

public class MascotDemo {

  private final Mascot mascot;
  private final Closeable toClose;
  private MascotSecurityParameters parameters = new MascotSecurityParameters();

  private MascotDemo(int myId, int noOfParties) {
       Network network =
        new AsyncNetwork(defaultNetworkConfiguration(myId, noOfParties));
    MascotResourcePool resourcePool = defaultResourcePool(myId, noOfParties,
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
      List<MultiplicationTriple> triples = mascot.getTriples(numTriples);
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

  private NetworkConfiguration defaultNetworkConfiguration(int myId, int noOfParties) {
    Map<Integer, Party> parties = new HashMap<>();
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      parties.put(partyId, new Party(partyId, "localhost", 8005 + partyId));
    }
    return new NetworkConfigurationImpl(myId, parties);
  }

  private MascotResourcePool defaultResourcePool(int myId, int noOfParties,
      Network network) {
    // generate random seed for local DRBG
    byte[] drbgSeed = new byte[parameters.getPrgSeedLength() / 8];
    new SecureRandom().nextBytes(drbgSeed);
    Drbg drbg = new PaddingAesCtrDrbg(drbgSeed);
    Map<Integer, RotList> seedOts = new HashMap<>();
    for (int otherId = 1; otherId <= noOfParties; otherId++) {
      if (myId != otherId) {
        DHParameterSpec dhSpec = DhParameters.getStaticDhParams();
        Ot ot = new NaorPinkasOt(otherId, drbg, network, dhSpec);
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
    int instanceId = 1;
    return new MascotResourcePoolImpl(myId, noOfParties, instanceId, drbg, seedOts, parameters);
  }

  /**
   * Runs demo.
   */
  public static void main(String[] args) {
    int myId = Integer.parseInt(args[0]);
    new MascotDemo(myId, 2).run(1, 9 * 1024);
  }

}
