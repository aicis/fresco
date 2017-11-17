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

/**
 * Demo class to execute a light instance of the correlated OT extension with
 * errors.
 * 
 * @author jot2re
 *
 * @param <ResourcePoolT>
 *          The FRESCO resource pool used for the execution
 */
public class CoteDemo<ResourcePoolT extends ResourcePool> {
  private int timeout = 5000;
  private int kbitLength = 128;
  private int lambdaSecurityParam = 40;
  private int amountOfOTs = 1024;

  /**
   * Run the receiving party.
   * 
   * @param pid
   *          The PID of the receiving party
   * @throws IOException
   *           Thrown in case of network issues
   * @throws NoSuchAlgorithmException
   *           Thrown in case the underlying PRG algorithm used does not exist
   */
  public void runPartyOne(int pid)
      throws IOException, NoSuchAlgorithmException {
    Network network = new KryoNetNetwork();
    network.init(getNetworkConfiguration(pid), 1);
    network.connect(timeout);
    System.out.println("Connected receiver");
    Random rand = new Random(42);
    Cote cote = new Cote(2, kbitLength, lambdaSecurityParam, rand, network);
    CoteReceiver coteRec = cote.getReceiver();
    coteRec.initialize();
    byte[] otChoices = new byte[amountOfOTs / 8];
    rand.nextBytes(otChoices);
    List<byte[]> t = coteRec.extend(otChoices, amountOfOTs);
    System.out.println("done receiver");
    // network.close();
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      for (int j = 0; j < kbitLength / 8; j++) {
        System.out.print(String.format("%02x ", t.get(i)[j]));
      }
      System.out.println();
    }
  }

  /**
   * Run the sending party.
   * 
   * @param pid
   *          The PID of the sending party
   * @throws IOException
   *           Thrown in case of network issues
   * @throws NoSuchAlgorithmException
   *           Thrown in case the underlying PRG algorithm used does not exist
   */
  public void runPartyTwo(int pid)
      throws IOException, NoSuchAlgorithmException {
    Network network = new KryoNetNetwork();
    network.init(getNetworkConfiguration(pid), 1);
    network.connect(timeout);
    System.out.println("Connected sender");
    Random rand = new Random(420);
    Cote cote = new Cote(1, kbitLength, lambdaSecurityParam, rand, network);
    CoteSender coteSnd = cote.getSender();
    coteSnd.initialize();
    List<byte[]> q = coteSnd.extend(amountOfOTs);
    System.out.println("done sender");
    // network.close();
    byte[] delta = coteSnd.getDelta();
    System.out.print("Delta: ");
    for (int j = 0; j < kbitLength / 8; j++) {
      System.out.print(String.format("%02x ", delta[j]));
    }
    System.out.println();
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      for (int j = 0; j < kbitLength / 8; j++) {
        System.out.print(String.format("%02x ", q.get(i)[j]));
      }
      System.out.println();
    }
  }

  /**
   * The main function, taking one argument, the PID of the calling party.
   * 
   * @param args
   *          Argument list, consisting of only the PID
   */
  public static void main(String[] args) {
    int pid = Integer.parseInt(args[0]);
    try {
      if (pid == 1) {
        new CoteDemo<>().runPartyOne(pid);
      } else {
        new CoteDemo<>().runPartyTwo(pid);
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
