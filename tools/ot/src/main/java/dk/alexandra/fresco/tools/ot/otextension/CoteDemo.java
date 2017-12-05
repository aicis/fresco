package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Demo class to execute a light instance of the correlated OT extension with
 * errors.
 * 
 * @author jot2re
 *
 * @param <ResourcePoolT>
 *          The FRESCO resource pool used for the execution
 */
public class CoteDemo<ResourcePoolT extends ResourcePool> extends Demo {
  private int amountOfOTs = 1024;

  /**
   * Run the receiving party.
   * 
   * @param pid
   *          The PID of the receiving party
   * @throws FailedOtExtensionException
   *           Thrown in case the underlying PRG algorithm used does not exist
   * @throws MaliciousOtExtensionException
   *           Thrown if cheating occurred
   */
  public void runPartyOne(int pid)
      throws FailedOtExtensionException, MaliciousOtExtensionException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected receiver");
    Random rand = new Random(42);
    Cote cote = new Cote(1, 2, getKbitLength(), getLambdaSecurityParam(), rand,
        network);
    CoteReceiver coteRec = cote.getReceiver();
    coteRec.initialize();
    byte[] otChoices = new byte[amountOfOTs / 8];
    rand.nextBytes(otChoices);
    List<StrictBitVector> t = coteRec
        .extend(new StrictBitVector(otChoices, amountOfOTs));
    System.out.println("done receiver");
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      byte[] output = t.get(i).toByteArray();
      for (byte current : output) {
        System.out.print(String.format("%02x ", current));
      }
      System.out.println();
    }
  }

  /**
   * Run the sending party.
   * 
   * @param pid
   *          The PID of the sending party
   * @throws MaliciousOtExtensionException
   *           Thrown if cheating occurred
   * @throws NoSuchAlgorithmException
   *           Thrown in case the underlying PRG algorithm used does not exist
   */
  public void runPartyTwo(int pid)
      throws FailedOtExtensionException, MaliciousOtExtensionException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected sender");
    Random rand = new Random(420);
    Cote cote = new Cote(2, 1, getKbitLength(), getLambdaSecurityParam(), rand,
        network);
    CoteSender coteSnd = cote.getSender();
    coteSnd.initialize();
    List<StrictBitVector> q = coteSnd.extend(amountOfOTs);
    System.out.println("done sender");
    StrictBitVector delta = coteSnd.getDelta();
    System.out.print("Delta: ");
    byte[] output = delta.toByteArray();
    for (byte current : output) {
      System.out.print(String.format("%02x ", current));
    }
    System.out.println();
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      output = q.get(i).toByteArray();
      for (byte current : output) {
        System.out.print(String.format("%02x ", current));
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
}
