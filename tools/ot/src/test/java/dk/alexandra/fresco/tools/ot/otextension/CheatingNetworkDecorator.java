package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.CloseableNetwork;
import java.io.IOException;

/**
 * Decorator class used for testing to ensure that proper checked exceptions are
 * thrown if a party is trying to cheat. This class intercepts messages send
 * over the network and flips a bit when asked to.
 */
public class CheatingNetworkDecorator implements CloseableNetwork {

  private final CloseableNetwork network;
  private int cheatByteNo;
  private int messageNo;
  private int counter = 0;
  private boolean cheat = false;

  public CheatingNetworkDecorator(CloseableNetwork network) {
    this.network = network;
  }

  /**
   * The class will flip a bit in the "byteNo" byte of the "messageNo" next
   * message sent.
   *
   * @param messageNo
   *          The cheating will occur in the "messageNo"'th call to network.send
   * @param byteNo
   *          The byte number in the next message to flip a bit in
   */
  public void cheatInNextMessage(int messageNo, int byteNo) {
    this.messageNo = messageNo;
    this.cheatByteNo = byteNo;
    this.cheat = true;
    this.counter = 0;
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (cheat == true && counter == messageNo) {
      // Flip a bit by XOR'ing with 1 in bit position 2
      data[cheatByteNo] ^= (byte) 0x02;
      cheat = false;
    }
    network.send(partyId, data);
    counter++;
  }

  @Override
  public void close() throws IOException {
    network.close();
  }

  @Override
  public byte[] receive(int partyId) {
    return network.receive(partyId);
  }

  @Override
  public int getNoOfParties() {
    return network.getNoOfParties();
  }

}
