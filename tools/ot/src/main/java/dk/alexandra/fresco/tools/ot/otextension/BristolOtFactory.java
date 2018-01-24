package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;

/**
 * Factory class for the Bristol OT protocol, which is an OT extension protocol,
 * secure in the random oracle model. It is specified in the paper
 * https://eprint.iacr.org/2015/546
 * <p>
 * This class constructs instances for sender and receiver objects that then
 * work together to execute the Bristol OT protocol.
 * </p>
 */
public class BristolOtFactory implements Ot {
  private BristolOtSender sender = null;
  private BristolOtReceiver receiver = null;
  private final RotFactory rot;
  private final OtExtensionResourcePool resources;
  private final Network network;
  private final int batchSize;

  /**
   * Constructs a new OT protocol and constructs the internal sender and receiver
   * objects.
   *
   * @param randomOtExtension
   *          The random OT extension to use
   * @param resources
   *          The resource pool for this specific instance
   * @param network
   *          The network to use
   * @param batchSize
   *          Size of the OT extension batch the protocol will construct
   */
  public BristolOtFactory(RotFactory randomOtExtension, OtExtensionResourcePool resources,
      Network network, int batchSize) {
    this.rot = randomOtExtension;
    this.resources = resources;
    this.network = network;
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
      RotSender sender = rot.createSender();
      this.sender = new BristolOtSender(sender, resources, network, batchSize);
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
      this.receiver = new BristolOtReceiver(receiver, resources, network,
          batchSize);
    }
    byte[] res = receiver.receive(choiceBit);
    return new StrictBitVector(res);
  }
}
