package dk.alexandra.fresco.tools.ot.otextension;

import java.util.List;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Demo class for execute a light instance of random OT extension.
 * 
 * @author jot2re
 *
 * @param <ResourcePoolT>
 *          The FRESCO resource pool used for the execution
 */
public class RotDemo<ResourcePoolT extends ResourcePool> extends Demo {
  // Amount of random OTs to construct
  private int amountOfOTs = 88;

  /**
   * Run the receiving party.
   * 
   * @param pid
   *          The PID of the receiving party
   * @throws FailedCoinTossingException
   *           Thrown in case something, non-malicious, goes wrong in the coin
   * @throws FailedCommitmentException
   *           Thrown in case something, non-malicious, goes wrong in the
   *           commitment protocol. tossing protocol.
   * @throws MaliciousCommitmentException
   *           Thrown in case the other party actively tries to cheat.
   * @throws FailedOtExtensionException
   *           Thrown in case something, non-malicious, goes wrong.
   * @throws MaliciousOtExtensionException
   *           Thrown if cheating occurred
   */
  public void runPartyOne(int pid) {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected receiver");
    Drbg rand = new AesCtrDrbg(new byte[] { 0x42, 0x42 });
    Rot rot = new Rot(1, 2, getKbitLength(), getLambdaSecurityParam(), rand,
        network);
    RotReceiver rotRec = rot.getReceiver();
    rotRec.initialize();
    byte[] otChoices = new byte[amountOfOTs / 8];
    rand.nextBytes(otChoices);
    List<StrictBitVector> vvec = rotRec
        .extend(new StrictBitVector(otChoices, amountOfOTs));
    System.out.println("done receiver");
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      byte[] output = vvec.get(i).toByteArray();
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
    Rot rot = new Rot(2, 1, getKbitLength(), getLambdaSecurityParam(), rand,
        network);
    RotSender rotSnd = rot.getSender();
    rotSnd.initialize();
    Pair<List<StrictBitVector>, List<StrictBitVector>> vpairs = rotSnd
        .extend(amountOfOTs);
    System.out.println("done sender");
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.println(i + ": ");
      byte[] outputZero = vpairs.getFirst().get(i).toByteArray();
      System.out.println("0-choice: ");
      for (byte current : outputZero) {
        System.out.print(String.format("%02x ", current));
      }
      byte[] outputOne = vpairs.getSecond().get(i).toByteArray();
      System.out.println("\n1-choice: ");
      for (byte current : outputOne) {
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
        new RotDemo<>().runPartyOne(pid);
      } else {
        new RotDemo<>().runPartyTwo(pid);
      }
    } catch (Exception e) {
      System.out.println("Failed to connect: " + e);
      e.printStackTrace(System.out);
    }
  }

}
