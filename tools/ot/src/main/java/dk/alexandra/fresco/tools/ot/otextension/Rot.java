package dk.alexandra.fresco.tools.ot.otextension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Container class for a protocol instance of random OT extension.
 * 
 * @author jot2re
 *
 */
public class Rot {
  protected RotSender sender;
  protected RotReceiver receiver;

  /**
   * Constructs a new random OT protocol using an already constructed correlated
   * OT with errors protocol.
   * 
   * @param cote
   *          The correlated OT with errors protocol to use
   */
  public Rot(Cote cote) {
    this.sender = new RotSender(cote.getSender());
    this.receiver = new RotReceiver(cote.getReceiver());
  }

  /**
   * Constructs a new random OT protocol and constructs the internal sender and
   * receiver objects.
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
  public Rot(int myId, int otherId, int kbitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    CoteSender sender = new CoteSender(myId, otherId, kbitLength,
        lambdaSecurityParam, rand, network);
    CoteReceiver receiver = new CoteReceiver(myId, otherId, kbitLength,
        lambdaSecurityParam, rand, network);
    this.sender = new RotSender(sender);
    this.receiver = new RotReceiver(receiver);
  }

  /**
   * Returns the sender object for the protocol.
   * 
   * @return Returns the sender object for the protocol
   */
  public RotSender getSender() {
    return sender;
  }

  /**
   * Returns the receiver object for the protocol.
   * 
   * @return Returns the receiver object for the protocol
   */
  public RotReceiver getReceiver() {
    return receiver;
  }
}
