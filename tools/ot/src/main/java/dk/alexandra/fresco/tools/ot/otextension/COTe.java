package dk.alexandra.fresco.tools.ot.otextension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Container class for a protocol instance of correlated OT extension with errors.
 * @author jot2re
 *
 */
public class COTe {

  protected COTeSender sender;
  protected COTeReceiver receiver;

  /**
   * Constructs a new correlated OT with errors protocol and initializes the internal sender and receiver objects.
   * @param otherID The unique ID of the other party (not the calling party) participating in the protocol
   * @param kBitLength The computational security parameter
   * @param lambdaSecurityParam The statistical security parameter
   * @param rand Object used for randomness generation
   * @param network The network instance
   */
  public COTe(int otherID, int kBitLength, int lambdaSecurityParam, Random rand, Network network) {
    this.sender = new COTeSender(lambdaSecurityParam, lambdaSecurityParam,
        lambdaSecurityParam, rand, network);
    this.receiver = new COTeReceiver(lambdaSecurityParam, lambdaSecurityParam,
        lambdaSecurityParam, rand, network);
  }

  /**
   * Returns the sender object for the protocol
   * @return Returns the sender object for the protocol 
   */
  public COTeSender getSender() {
    return sender;
  }

  /**
   * Returns the receiver object for the protocol
   * @return Returns the receiver object for the protocol
   */
  public COTeReceiver getReceiver() {
    return receiver;
  }
}
