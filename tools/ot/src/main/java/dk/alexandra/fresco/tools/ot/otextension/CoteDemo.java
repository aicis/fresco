package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.DummyOt;

import java.util.List;

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
   */
  public void runPartyOne(int pid) {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected receiver");
    Drbg rand = new AesCtrDrbg(new byte[] { 0x42, 0x42 });
    Cote cote = new Cote(1, 2, getKbitLength(), getLambdaSecurityParam(), rand,
        network, new DummyOt(2, network));
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
   */
  public void runPartyTwo(int pid) {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected sender");
    Drbg rand = new AesCtrDrbg(new byte[] { 0x42, 0x04 });
    Cote cote = new Cote(2, 1, getKbitLength(), getLambdaSecurityParam(), rand,
        network, new DummyOt(1, network));
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
