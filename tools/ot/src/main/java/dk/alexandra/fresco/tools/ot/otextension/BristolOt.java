package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;

/**
 * Container class for a protocol instance of Bristol OTs.
 * 
 * @author jot2re
 *
 */
public class BristolOt implements Ot {
  private final BristolOtSender sender;
  private final BristolOtReceiver receiver;

  /**
   * Constructs a new OT protocol and constructs the internal sender and
   * receiver objects.
   * 
   * @param resources
   *          The common OT extension resource pool
   * @param network
   *          The network instance
   * @param ot
   *          The OT functionality to use for seed OTs
   * @param batchSize
   *          Size of the OT extension batch the protocol will construct
   */
  public BristolOt(OtExtensionResourcePool resources, Network network, Ot ot,
      int batchSize) {
    Rot rot = new Rot(resources, network, ot);
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
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    sender.send(messageZero.toByteArray(), messageOne.toByteArray());
  }

  /**
   * Act as receiver in a 1-out-of-2 OT.
   * 
   * @param choiceBit
   *          The bit representing choice of message. False represents 0 and
   *          true represents 1.
   */
  @Override
  public StrictBitVector receive(Boolean choiceBit) {
    byte[] res = receiver.receive(choiceBit);
    return new StrictBitVector(res, 8 * res.length);
  }
}
