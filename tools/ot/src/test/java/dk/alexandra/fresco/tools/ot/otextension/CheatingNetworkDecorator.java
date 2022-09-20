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
  private int sendMessageNo;
  private int receiveMessageNo;
  private int sendCounter = 0;
  private int receiveCounter = 0;
  private boolean cheatinSend = false;
  private boolean cheatInReceive = false;
  private byte[] receiveCheatMsg;

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
  public void cheatInNextSendMessage(int messageNo, int byteNo) {
    this.sendMessageNo = messageNo;
    this.cheatByteNo = byteNo;
    this.cheatinSend = true;
    this.sendCounter = 0;
  }

  /**
   * The class will receive on "messageNo" message the byte[] specified with "msg"
   * @param messageNo
   *          The cheating will occur in the "messageNo"'th call to network.receive
   * @param msg
   *          The message which will be received
   */
  public void cheatInReceive(int messageNo, byte[] msg) {
    this.receiveMessageNo = messageNo;
    this.cheatInReceive = true;
    this.receiveCheatMsg = msg;
  }

  @Override
  public void send(int partyId, byte[] data) {
    if (cheatinSend == true && sendCounter == sendMessageNo) {
      // Flip a bit by XOR'ing with 1 in bit position 2
      data[cheatByteNo] ^= (byte) 0x02;
      cheatinSend = false;
    }
    network.send(partyId, data);
    sendCounter++;
  }

  @Override
  public void close() throws IOException {
    network.close();
  }

  @Override
  public byte[] receive(int partyId) {
    if (cheatInReceive == true && receiveCounter == receiveMessageNo) {
      cheatInReceive = false;
      return receiveCheatMsg;
    }
    receiveCounter++;
    return network.receive(partyId);
  }

  @Override
  public int getNoOfParties() {
    return network.getNoOfParties();
  }

}
