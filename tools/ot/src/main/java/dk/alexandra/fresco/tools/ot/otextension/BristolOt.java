package dk.alexandra.fresco.tools.ot.otextension;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.ot.base.Ot;

/**
 * Container class for a protocol instance of Bristol OTs.
 * 
 * @author jot2re
 *
 */
public class BristolOt<T extends Serializable> implements Ot<T> {
  protected BristolOtSender sender;
  protected BristolOtReceiver receiver;

  /**
   * Constructs a new OT protocol and constructs the internal sender and
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
   * @param batchSize
   *          Size of the OT extension batch the protocol will construct
   */
  public BristolOt(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam, Random rand, Network network, int batchSize) {
    Rot rot = new Rot(myId, otherId, kbitLength, lambdaSecurityParam, rand,
        network);
    RotSender sender = rot.getSender();
    RotReceiver receiver = rot.getReceiver();
    this.sender = new BristolOtSender(sender, batchSize);
    this.receiver = new BristolOtReceiver(receiver, batchSize);
  }

  /**
   * Act as sender in a 1-out-of-2 OT.
   * 
   * @param messageZero
   *          The zero-choice message
   * @param messageOne
   *          the one-choice message
   */
  @Override
  public void send(T messageZero, T messageOne) {
    byte[] messageZeroBytes = ByteArrayHelper.serialize(messageZero);
    byte[] messageOneBytes = ByteArrayHelper.serialize(messageOne);
    sender.send(messageZeroBytes, messageOneBytes);
  }

  /**
   * Act as receiver in a 1-out-of-2 OT.
   * 
   * @param choiceBit
   *          The bit representing choice of message. False represents 0 and
   *          true represents 1.
   */
  @SuppressWarnings("unchecked")
  @Override
  public T receive(Boolean choiceBit) {
    try {
      return (T) ByteArrayHelper.deserialize(receiver.receive(choiceBit));
    } catch (NoSuchAlgorithmException e) {
      throw new MPCException(
          "Something, non-malicious, went wrong when receiving a Bristol OT.",
          e);
    }
  }
}
