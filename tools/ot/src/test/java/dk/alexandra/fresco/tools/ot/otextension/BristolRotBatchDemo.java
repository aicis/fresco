package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class BristolRotBatchDemo<ResourcePoolT extends ResourcePool> {
  // Amount of random OTs to construct
  private final int amountOfOTs = 128;
  // The amount of bits in each message
  private final int messageSize = 2048;
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
    RotBatch rotBatch = new BristolRotBatch(ctx.getResources(),
        ctx
        .getNetwork(), ctx.getDummyOtInstance());
    StrictBitVector choices = new StrictBitVector(amountOfOTs, ctx.getRand());
    List<StrictBitVector> messages = rotBatch.receive(choices, messageSize);
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.println("Iteration " + i + ", Choice " + choices.getBit(i,
          false) + ": " + messages.get(i));
    }
    System.out.println("done receiver");
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
    RotBatch rotBatch = new BristolRotBatch(ctx.getResources(),
        ctx.getNetwork(), ctx.getDummyOtInstance());
    List<Pair<StrictBitVector, StrictBitVector>> messages = rotBatch.send(
        amountOfOTs, messageSize);
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.println("Iteration " + i);
      System.out.println("Message 0: " + messages.get(i).getFirst());
      System.out.println("Message 1: " + messages.get(i).getSecond());
    }
    System.out.println("done sender");
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
        new BristolRotBatchDemo<>().runPartyOne(pid);
      } else {
        new BristolRotBatchDemo<>().runPartyTwo(pid);
      }
    } catch (Exception e) {
      System.out.println("Failed to connect: " + e);
      e.printStackTrace(System.out);
    }
  }
}
