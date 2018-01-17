package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Demo class to execute a light instance of the correlated OT extension with
 * errors.
 *
 * @param <ResourcePoolT>
 *          The FRESCO resource pool used for the execution
 */
public class CoteDemo<ResourcePoolT extends ResourcePool> {
  private final int amountOfOTs = 1024;
  private final int kbitLength = 128;
  private final int lambdaSecurityParam = 40;

  /**
   * Run the receiving party.
   *
   * @param pid
   *          The PID of the receiving party
   * @throws IOException
   *           Thrown if the network cannot close
   */
  public void runPartyOne(int pid) throws IOException {
    OtExtensionTestContext ctx = new OtExtensionTestContext(1, 2, kbitLength,
        lambdaSecurityParam);
    Cote cote = new Cote(ctx.createResources(1), ctx.getNetwork());
    CoteReceiver coteRec = cote.getReceiver();
    byte[] otChoices = new byte[amountOfOTs / 8];
    ctx.createRand(1).nextBytes(otChoices);
    List<StrictBitVector> t = coteRec
        .extend(new StrictBitVector(otChoices));
    System.out.println("done receiver");
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.print(i + ": ");
      byte[] output = t.get(i).toByteArray();
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
   *           Thrown if the network cannot close
   */
  public void runPartyTwo(int pid) throws IOException {
    OtExtensionTestContext ctx = new OtExtensionTestContext(2, 1, kbitLength,
        lambdaSecurityParam);
    Cote cote = new Cote(ctx.createResources(1), ctx.getNetwork());
    CoteSender coteSnd = cote.getSender();
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
