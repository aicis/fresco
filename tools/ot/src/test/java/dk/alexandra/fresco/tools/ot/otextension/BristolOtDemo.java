package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;
import java.io.Closeable;
import java.io.IOException;
import java.util.Random;

public class BristolOtDemo<ResourcePoolT extends ResourcePool> {
  // Amount of OTs to construct
  private final int amountOfOTs = 88;
  private final int kbitLength = 128;
  private final int lambdaSecurityParam = 40;
  private final int messageSize = 512;

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
    Ot ot = new BristolOt(
        amountOfOTs, new RotImpl(ctx.createResources(1), ctx.getNetwork()));
    for (int i = 0; i < amountOfOTs; i++) {
      boolean choice = (new Random()).nextBoolean();
      System.out.print("Choice " + choice + ": ");
      StrictBitVector res = ot.receive(choice);
      System.out.println(res);
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
    Ot ot = new BristolOt(
        amountOfOTs, new RotImpl(ctx.createResources(1), ctx.getNetwork()));
    Drbg rand = ctx.createRand(1);
    for (int i = 0; i < amountOfOTs; i++) {
      StrictBitVector msgZero = new StrictBitVector(messageSize, rand);
      StrictBitVector msgOne = new StrictBitVector(messageSize, rand);
      System.out.println("Message 0: " + msgZero);
      System.out.println("Message 1: " + msgOne);
      ot.send(msgZero, msgOne);
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
        new BristolOtDemo<>().runPartyOne(pid);
      } else {
        new BristolOtDemo<>().runPartyTwo(pid);
      }
    } catch (Exception e) {
      System.out.println("Failed to connect: " + e);
      e.printStackTrace(System.out);
    }
  }
}
