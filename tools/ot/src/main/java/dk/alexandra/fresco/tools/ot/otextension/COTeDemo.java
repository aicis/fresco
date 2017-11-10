package dk.alexandra.fresco.tools.ot.otextension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class COTeDemo<ResourcePoolT extends ResourcePool> {
  private int timeout = 5000;
  private int kBitLength = 128;
  private int lambdaSecurityParam = 40;
  private int amountOfOTs = 1024;

  public void runPartyOne(Network network) throws IOException {
    network.connect(timeout);
    System.out.println("Connected receiver");
    Random rand = new Random(42);
    COTe cote = new COTe(2, kBitLength, lambdaSecurityParam, rand, network);
    COTeReceiver coteRec = cote.getReceiver();
    coteRec.initialize();
    coteRec.extend(amountOfOTs);
    System.out.println("done receiver");
    network.close();
  }

  public void runPartyTwo(Network network) throws IOException {
    network.connect(timeout);
    System.out.println("Connected sender");
    Random rand = new Random(420);
    COTe cote = new COTe(1, kBitLength, lambdaSecurityParam, rand, network);
    COTeSender coteSnd = cote.getSender();
    coteSnd.initialize();
    coteSnd.extend(amountOfOTs);
    System.out.println("done sender");
    network.close();
  }

  public static void main(String[] args) {
    int pid = Integer.parseInt(args[0]);
    Network network = new KryoNetNetwork();
    network.init(getNetworkConfiguration(pid), 1);
    try {
      if (pid == 1) {
        new COTeDemo<>().runPartyOne(network);
      } else {
        new COTeDemo<>().runPartyTwo(network);
      }
    } catch (Exception e) {
      System.out.println("Failed to connect: " + e);
      e.printStackTrace(System.out);
    }
  }

  private static NetworkConfiguration getNetworkConfiguration(int pid) {
    Map<Integer, Party> parties = new HashMap<>();
    parties.put(1, new Party(1, "localhost", 8001));
    parties.put(2, new Party(2, "localhost", 8002));
    return new NetworkConfigurationImpl(pid, parties);
  }
}
