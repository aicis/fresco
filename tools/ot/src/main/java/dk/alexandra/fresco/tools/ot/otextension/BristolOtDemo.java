package dk.alexandra.fresco.tools.ot.otextension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;

public class BristolOtDemo<ResourcePoolT extends ResourcePool> extends Demo {
  // Amount of OTs to construct
  private int amountOfOTs = 88;

  /**
   * Run the receiving party.
   * 
   * @param pid
   *          The PID of the receiving party
   */
  public void runPartyOne(int pid) {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected receiver");
    Random rand = new Random(424242);
    Ot ot = new BristolOt(1, 2, getKbitLength(),
        getLambdaSecurityParam(), rand, network, amountOfOTs);
    for (int i = 0; i < amountOfOTs; i++) {
      boolean choice = rand.nextBoolean();
      System.out.print("Choice " + choice + ": ");
      StrictBitVector res = ot.receive(choice);
      System.out.println(res);
    }
    System.out.println("done receiver");
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
    Random rand = new Random(420420);
    Ot ot = new BristolOt(2, 1, getKbitLength(), getLambdaSecurityParam(),
        rand, network, amountOfOTs);
    for (int i = 0; i < amountOfOTs; i++) {
      // We send random 512 bit bitstrings
      StrictBitVector msgZero = new StrictBitVector(512, rand);
      StrictBitVector msgOne = new StrictBitVector(512, rand);
      System.out.println("Message 0: " + msgZero);
      System.out.println("Message 1: " + msgOne);
      ot.send(msgZero, msgOne);
    }
    System.out.println("done sender");
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
