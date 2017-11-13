package dk.alexandra.fresco.tools.ot.otextension;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class COTeDemo<ResourcePoolT extends ResourcePool> {
  private int timeout = 50;
  private int kBitLength = 8;
  private int lambdaSecurityParam = 40;
  private int amountOfOTs = 8;

  public void runPartyOne(int pid)
      throws IOException, NoSuchAlgorithmException {
    Network network = new KryoNetNetwork();
    network.init(getNetworkConfiguration(pid), 1);
    network.connect(timeout);
    System.out.println("Connected receiver");
    Random rand = new Random(42);
    COTe cote = new COTe(2, kBitLength, lambdaSecurityParam, rand, network);
    COTeReceiver coteRec = cote.getReceiver();
    coteRec.initialize();
    byte[] otChoices = new byte[amountOfOTs / 8];
    rand.nextBytes(otChoices);
    List<byte[]> t = coteRec.extend(otChoices, amountOfOTs);
    System.out.println("done receiver");
    // network.close();
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      for (int j = 0; j < kBitLength / 8; j++) {
        System.out.print(String.format("%02x ", t.get(i)[j]));
      }
      System.out.println();
    }
  }

  public void runPartyTwo(int pid)
      throws IOException, NoSuchAlgorithmException {
    Network network = new KryoNetNetwork();
    network.init(getNetworkConfiguration(pid), 1);
    network.connect(timeout);
    System.out.println("Connected sender");
    Random rand = new Random(420);
    COTe cote = new COTe(1, kBitLength, lambdaSecurityParam, rand, network);
    COTeSender coteSnd = cote.getSender();
    coteSnd.initialize();
    List<byte[]> q = coteSnd.extend(amountOfOTs);
    System.out.println("done sender");
    // network.close();
    byte[] delta = coteSnd.getDelta();
    System.out.print("Delta: ");
    for (int j = 0; j < kBitLength / 8; j++) {
      System.out.print(String.format("%02x ", delta[j]));
    }
    System.out.println();
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      for (int j = 0; j < kBitLength / 8; j++) {
        System.out.print(String.format("%02x ", q.get(i)[j]));
      }
      System.out.println();
    }
  }

  public static void main(String[] args) {
    int pid = Integer.parseInt(args[0]);
    try {
      if (pid == 1) {
        new COTeDemo<>().runPartyOne(pid);
      } else {
        new COTeDemo<>().runPartyTwo(pid);
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
