package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;

/**
 * Wrapper class used for testing to ensure that proper checked exceptions are
 * thrown if a party is trying to cheat. This class intercepts messages send
 * over the network and flips a bit when asked to.
 * 
 * @author jot2re
 *
 */
public class CheatingNetwork extends KryoNetNetwork {
  private int cheatByteNo;
  private boolean cheat = false;

  public CheatingNetwork(NetworkConfiguration conf) {
    super(conf);
  }

  /**
   * The class will flip a bit in the "byteNo" byte of the next message sent.
   * 
   * @param byteNo
   *          The byte number in the next message to flip a bit in
   */
  public void cheatInNextMessage(int byteNo) {
    cheatByteNo = byteNo;
    cheat = true;
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (cheat == true) {
      // Flip a bit by XOR'ing with 1 in bit position 2
      data[cheatByteNo] ^= (byte) 0x02;
    }
    super.send(partyId, data);
  }
}
