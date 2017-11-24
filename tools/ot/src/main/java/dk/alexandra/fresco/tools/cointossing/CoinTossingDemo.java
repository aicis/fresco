package dk.alexandra.fresco.tools.cointossing;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;

/**
 * Runs a demo session of the coin-tossing and commitment functionality.
 * 
 * @author jot2re
 *
 */
public class CoinTossingDemo {
  private int kbitLength = 128;
  private int amountOfBits = 1024;

  /**
   * Run one of the parties.
   * 
   * @param myId
   *          The PID of the party running this code.
   * @param otherId
   *          The PID of the other party.
   * @throws IOException
   *           Thrown in case of network issues
   * @throws NoSuchAlgorithmException
   *           Thrown in case the underlying PRG algorithm used does not exist
   * @throws MaliciousCommitmentException
   *           The opening info received to a commitment does not match the
   *           commitment. This is only thrown in case a party is running
   *           maliciously.
   * @throws FailedCoinTossingException
   *           Something went wrong internally with the coin tossing. This is
   *           *not* an indication of malicious behavior.
   * @throws FailedCommitmentException
   *           Something went wrong internally with a commitment. This is *not*
   *           an indication of malicious behavior.
   * 
   */
  public void run(int myId, int otherId)
      throws IOException, NoSuchAlgorithmException, FailedCoinTossingException,
      MaliciousCommitmentException, FailedCommitmentException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(myId));
    System.out.println("Connected party " + myId);
    Random rand = new Random(42);
    CoinTossing ct = new CoinTossing(myId, otherId, kbitLength, rand, network);
    ct.initialize();
    byte[] bits = ct.toss(amountOfBits);
    System.out.println("done party " + myId);
    for (int j = 0; j < (kbitLength + 8 - 1) / 8; j++) {
      System.out.print(String.format("%02x ", bits[j]));
    }
    System.out.println();
  }

  /**
   * The main function, taking one argument, the PID of the calling party. I.e.
   * the one currently executing the code.
   * 
   * @param args
   *          Argument list, consisting of only the PID
   */
  public static void main(String[] args) {
    int pid = Integer.parseInt(args[0]);
    try {
      if (pid == 1) {
        new CoinTossingDemo().run(1, 2);
      } else {
        new CoinTossingDemo().run(2, 1);
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
