package dk.alexandra.fresco.tools.ot.otextension;

import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.ot.base.FailedOtException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

public class BristolRotBatchDemo<ResourcePoolT extends ResourcePool>
    extends Demo {
  // Amount of random OTs to construct
  private int amountOfOTs = 88;
  // The amount of bits in each message
  private int messageSize = 2048;

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
   * @throws FailedOtException
   *           Thrown in case something, non-malicious, goes wrong
   * @throws MaliciousOtException
   *           Thrown if cheating occurred
   */
  public void runPartyOne(int pid)
      throws MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, FailedOtExtensionException,
      MaliciousOtExtensionException, MaliciousOtException, FailedOtException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected receiver");
    Random rand = new Random(424242);
    Rot rot = new Rot(1, 2, getKbitLength(), getLambdaSecurityParam(), rand,
        network);
    RotReceiver rotRec = rot.getReceiver();
    rotRec.initialize();
    RotBatch<StrictBitVector> ot = new BristolRotBatch(rot);
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
   * @throws FailedCoinTossingException
   *           Thrown in case something, non-malicious, goes wrong in the coin
   * @throws FailedCommitmentException
   *           Thrown in case something, non-malicious, goes wrong in the
   *           commitment protocol. tossing protocol.
   * @throws FailedOtExtensionException
   *           Thrown in case something, non-malicious, goes wrong.
   * @throws MaliciousCommitmentException
   *           Thrown in case the other party actively tries to cheat in the
   *           commitments.
   * @throws MaliciousOtExtensionException
   *           Thrown in case the other party actively tries to cheat.
   * @throws FailedOtException
   *           Thrown in case something, non-malicious, goes wrong
   * @throws MaliciousOtException
   *           Thrown if cheating occurred
   */
  public void runPartyTwo(int pid)
      throws FailedOtExtensionException, MaliciousCommitmentException,
      FailedCommitmentException, FailedCoinTossingException,
      MaliciousOtExtensionException, MaliciousOtException, FailedOtException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected sender");
    Random rand = new Random(420420);
    Rot rot = new Rot(2, 1, getKbitLength(), getLambdaSecurityParam(), rand,
        network);
    RotSender rotSnd = rot.getSender();
    rotSnd.initialize();
    RotBatch<StrictBitVector> ot = new BristolRotBatch(rot);
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
