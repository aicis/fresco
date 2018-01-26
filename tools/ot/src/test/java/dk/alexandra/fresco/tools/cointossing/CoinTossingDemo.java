package dk.alexandra.fresco.tools.cointossing;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs a demo session of the coin-tossing and commitment functionality.
 */
public class CoinTossingDemo {
  private int amountOfBits = 6666;

  /**
   * Run one of the parties.
   *
   * @param myId
   *          The PID of the party running this code.
   * @param otherId
   *          The PID of the other party.
   * @throws NoSuchAlgorithmException
   *           Thrown if the underlying hash algorithm does not exist
   */
  public void run(int myId, int otherId) throws NoSuchAlgorithmException {
    Network network = new KryoNetNetwork(getNetworkConfiguration(myId));
    System.out.println("Connected party " + myId);
    Drbg rand = new HmacDrbg(new byte[] { 0x42 });
    CoinTossing ct = new CoinTossing(myId, otherId, rand);
    ct.initialize(network);
    StrictBitVector bits = ct.toss(amountOfBits);
    byte[] bytes = bits.toByteArray();
    System.out.println("done party " + myId);
    // We just print the first bytes
    for (int j = 0; j < 32; j++) {
      System.out.print(String.format("%02x ", bytes[j]));
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
