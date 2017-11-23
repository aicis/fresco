package dk.alexandra.fresco.tools.ot.otextension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;

/** 
 * Container class for a protocol instance of correlated OT extension with errors.
 * @author jot2re
 *
 */
public class Cote {

  protected CoteSender sender;
  protected CoteReceiver receiver;

  /**
   * Constructs a new correlated OT with errors protocol and initializes the
   * internal sender and receiver objects.
   * 
   * @param myId
   *          The unique ID of the calling party
   * @param otherId
   *          The unique ID of the other party (not the calling party)
   *          participating in the protocol
   * @param kbitLength
   *          The computational security parameter
   * @param lambdaSecurityParam
   *          The statistical security parameter
   * @param rand
   *          Object used for randomness generation
   * @param network
   *          The network instance
   */
  public Cote(int myId, int otherId, int kbitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    this.sender = new CoteSender(myId, otherId, kbitLength,
        lambdaSecurityParam, rand, network);
    this.receiver = new CoteReceiver(myId, otherId, kbitLength,
        lambdaSecurityParam, rand, network);
  }

  /**
   * Returns the sender object for the protocol.
   * 
   * @return Returns the sender object for the protocol
   */
  public CoteSender getSender() {
    return sender;
  }

  /**
   * Returns the receiver object for the protocol.
   * 
   * @return Returns the receiver object for the protocol
   */
  public CoteReceiver getReceiver() {
    return receiver;
  }
}
