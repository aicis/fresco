package dk.alexandra.fresco.tools.ot.otextension;

import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

public class BristolRotBatchDemo<ResourcePoolT extends ResourcePool>
    extends Demo {
  // Amount of random OTs to construct
  private int amountOfOTs = 128;
  // The amount of bits in each message
  private int messageSize = 2048;

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
    RotBatch<StrictBitVector> ot = new BristolRotBatch(1, 2, getKbitLength(),
        getLambdaSecurityParam(), rand, network);
    StrictBitVector choices = new StrictBitVector(amountOfOTs, rand);
    List<StrictBitVector> messages = ot.receive(choices, messageSize);
    for (int i = 0; i < amountOfOTs; i++) {
      System.out
          .println(
              "Iteration " + i + ", Choice " + choices.getBit(i, false) + ": "
                  + messages.get(i));
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
    RotBatch<StrictBitVector> ot = new BristolRotBatch(2, 1, getKbitLength(),
        getLambdaSecurityParam(), rand, network);
    List<Pair<StrictBitVector, StrictBitVector>> messages = ot.send(amountOfOTs,
        messageSize);
    for (int i = 0; i < amountOfOTs; i++) {
      System.out.println("Iteration " + i);
      System.out.println("Message 0: " + messages.get(i).getFirst());
      System.out.println("Message 1: " + messages.get(i).getSecond());
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