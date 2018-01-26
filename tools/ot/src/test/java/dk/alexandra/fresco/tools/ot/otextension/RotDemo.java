package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Demo class for execute a light instance of random OT extension.
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
   * @throws IOException
   *           Thrown in case of a network issue.
   */
  public void runPartyOne(int pid) throws IOException {
    OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength,
        lambdaSecurityParam);
    // ctx.getDummyOtInstance().receive();
    RotFactory rot = new RotFactory(ctx.createResources(1), ctx.getNetwork());
    RotReceiver rotRec = rot.getReceiver();
    // rotRec.initialize();
    byte[] otChoices = new byte[amountOfOTs / 8];
    ctx.createRand(1).nextBytes(otChoices);
    List<StrictBitVector> vvec = rotRec
        .extend(new StrictBitVector(otChoices));
    System.out.println("done receiver");
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      byte[] output = vvec.get(i).toByteArray();
      for (byte current : output) {
        System.out.print(String.format("%02x ", current));
      }
      System.out.println();
    }
    ((Closeable) ctx.getNetwork()).close();
  }

  /**
   * Run the sending party.
   *
   * @param pid
   *          The PID of the sending party
   * @throws IOException
   *           Thrown in case of a network issue
   */
  public void runPartyTwo(int pid) throws IOException {
    OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength,
        lambdaSecurityParam);
    RotFactory rot = new RotFactory(ctx.createResources(1), ctx.getNetwork());
    RotSender rotSnd = rot.createSender();
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
    ((Closeable) ctx.getNetwork()).close();
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
