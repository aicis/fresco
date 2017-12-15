package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

import java.util.List;

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
    Drbg currentPrg = new AesCtrDrbg(new byte[] { 0x42, 0x42 });
    RotBatch<StrictBitVector> ot = new BristolRotBatch(1, 2, getKbitLength(),
        getLambdaSecurityParam(), currentPrg, network, new DummyOt(2, network));
    StrictBitVector choices = new StrictBitVector(amountOfOTs, currentPrg);
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
    Drbg currentPrg = new AesCtrDrbg(new byte[] { 0x42, 0x04 });
    RotBatch<StrictBitVector> ot = new BristolRotBatch(2, 1, getKbitLength(),
        getLambdaSecurityParam(), currentPrg, network, new DummyOt(1, network));
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
