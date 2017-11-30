package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.ot.base.FailedOtException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;
import dk.alexandra.fresco.tools.ot.base.Ot;

public class BristolOtDemo<ResourcePoolT extends ResourcePool> {
  // Computational security parameter
  private int kbitLength = 128;
  // Statistical security parameter
  private int lambdaSecurityParam = 40;
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
   * @throws FailedOtException
   * @throws MaliciousOtException
   */
  public void runPartyOne(int pid) throws MaliciousCommitmentException,
      FailedCommitmentException, FailedCoinTossingException,
      FailedOtExtensionException, MaliciousOtExtensionException,
      MaliciousOtException, FailedOtException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected receiver");
    Random rand = new Random(424242);
    Ot<BigInteger> ot = new BristolOt<>(1, 2, kbitLength,
        lambdaSecurityParam, rand, network, amountOfOTs);
    for (int i = 0; i < amountOfOTs; i++) {
      boolean choice = rand.nextBoolean();
      System.out.print("Choice " + choice + ": ");
      BigInteger res = ot.receive(choice);
      System.out.println(res);
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
   * @throws MaliciousOtException
   */
  public void runPartyTwo(int pid) throws FailedOtExtensionException,
      MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, MaliciousOtExtensionException,
      MaliciousOtException, FailedOtException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(pid));
    System.out.println("Connected sender");
    Random rand = new Random(420420);
    Ot<BigInteger> ot = new BristolOt<>(1, 2, kbitLength, lambdaSecurityParam,
        rand, network, amountOfOTs);
    for (int i = 0; i < amountOfOTs; i++) {
      BigInteger msgZero = new BigInteger(512, rand);
      BigInteger msgOne = new BigInteger(512, rand);
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

  private static NetworkConfiguration getNetworkConfiguration(int pid) {
    Map<Integer, Party> parties = new HashMap<>();
    parties.put(1, new Party(1, "localhost", 8001));
    parties.put(2, new Party(2, "localhost", 8002));
    return new NetworkConfigurationImpl(pid, parties);
  }
}
