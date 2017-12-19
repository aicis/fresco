package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.List;

/**
 * Demo class for execute a light instance of random OT extension.
 * 
 * @author jot2re
 *
 * @param <ResourcePoolT>
 *          The FRESCO resource pool used for the execution
 */
public class RotDemo<ResourcePoolT extends ResourcePool> {
  // Amount of random OTs to construct
  private final int amountOfOTs = 88;
  private final int kbitLength = 128;
  private final int lambdaSecurityParam = 40;

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
    OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength,
        lambdaSecurityParam);
    Rot rot = new Rot(ctx.getResources(), ctx.getNetwork(), ctx
        .getDummyOtInstance());
    RotReceiver rotRec = rot.getReceiver();
    rotRec.initialize();
    byte[] otChoices = new byte[amountOfOTs / 8];
    ctx.getRand().nextBytes(otChoices);
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
    OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength,
        lambdaSecurityParam);
    Rot rot = new Rot(ctx.getResources(), ctx.getNetwork(), ctx
        .getDummyOtInstance());
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
