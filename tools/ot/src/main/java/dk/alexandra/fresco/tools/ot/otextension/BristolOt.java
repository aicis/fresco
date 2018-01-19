package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;

/**
 * Container class for a protocol instance of Bristol OTs.
 */
public class BristolOt implements Ot {
  private BristolOtSender sender = null;
  private BristolOtReceiver receiver = null;
  private final Rot rot;
  private final int batchSize;

  /**
   * Constructs a new OT protocol and constructs the internal sender and
   * receiver objects.
   *
   * @param batchSize
   *          Size of the OT extension batch the protocol will construct
   * @param randomOtExtension random OT extension
   */
  public BristolOt(int batchSize, Rot randomOtExtension) {
    this.rot = randomOtExtension;
    this.batchSize = batchSize;
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
    if (this.sender == null) {
      RotSender sender = rot.getSender();
      this.sender = new BristolOtSender(sender, batchSize);
    }
    this.sender.send(messageZero.toByteArray(), messageOne.toByteArray());
  }

  /**
   * Act as receiver in a 1-out-of-2 OT.
   *
   * @param choiceBit
   *          The bit representing choice of message. False represents 0 and
   *          true represents 1.
   */
  @Override
  public StrictBitVector receive(boolean choiceBit) {
    if (this.receiver == null) {
      RotReceiver receiver = rot.getReceiver();
      this.receiver = new BristolOtReceiver(receiver, batchSize);
    }
    byte[] res = receiver.receive(choiceBit);
    return new StrictBitVector(res);
  }
}
