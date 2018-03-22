package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.List;

/**
 * Protocol class for the party acting as the receiver in an OT extension. The
 * functionality works by doing a batch of random OTs the first time the receive
 * command is called and storing these results internally. When it runs out, it
 * automatically processes a new batch. These random OTs are adjusted to work as
 * chosen bit/message 1-out-of-2 OTs.
 */
public class BristolOtReceiver {
  private final RotReceiver receiver;
  private final OtExtensionResourcePool resources;
  private final Network network;
  private final int batchSize;

  // The random messages received from the batched random 1-out-of-2 OTs
  private List<StrictBitVector> randomMessages;
  // The random choices from the batched random 1-out-of-2 OTs
  private StrictBitVector choices;
  // Index of the current random OT to use
  private int offset = -1;

  /**
   * Initializes the underlying Rot functionality using {@code rotReceiver}. It
   * will then construct OTs in batches of {@code batchSize}.
   *
   * @param rotReceiver
   *          The random OT receiver instance to use
   * @param resources
   *          The resource pool for this specific instance
   * @param network
   *          The network to use
   * @param batchSize
   *          Size of the OT extension batch the protocol will construct
   */
  public BristolOtReceiver(RotReceiver rotReceiver,
      OtExtensionResourcePool resources, Network network, int batchSize) {
    this.receiver = rotReceiver;
    this.resources = resources;
    this.network = network;
    this.batchSize = batchSize;
  }

  /**
   * Receive the serialized message from the current 1-out-of-2 OT.
   *
   * @param choiceBit
   *          Choice-bit. False for message 0, true for message 1.
   * @return The serialized message from the OT
   */
  public byte[] receive(boolean choiceBit) {
    // Check if there is still an unused random OT stored, if not, execute a
    // random OT extension
    if (offset < 0 || offset >= batchSize) {
      choices = new StrictBitVector(batchSize, resources.getRandomGenerator());
      randomMessages = receiver.extend(choices);
      offset = 0;
    }
    // Notify the sender if it should switch the 0 and 1 messages around (s.t.
    // the random choice bit in the preprocessed random OTs matches the true
    // choice bit
    sendSwitchBit(choiceBit);
    // Receive the serialized adjusted messages
    byte[] zeroAdjustment = network.receive(resources.getOtherId());
    byte[] oneAdjustment = network.receive(resources.getOtherId());
    byte[] res = doActualReceive(zeroAdjustment, oneAdjustment);
    offset++;
    return res;
  }

  /**
   * Adjust the random, preprocessed message, to fit the specific message sent
   * by the sender.
   *
   * @param zeroAdjustment
   *          The adjustment value for the zero message
   * @param oneAdjustment
   *          The adjustment value for the one message
   * @return The actual message
   */
  private byte[] doActualReceive(byte[] zeroAdjustment, byte[] oneAdjustment) {
    if (zeroAdjustment.length != oneAdjustment.length) {
      throw new MaliciousException(
          "Sender gave adjustment messages of different length.");
    }
    byte[] adjustment;
    if (choices.getBit(offset, false) == false) {
      adjustment = zeroAdjustment;
    } else {
      adjustment = oneAdjustment;
    }
    return PseudoOtp.decrypt(adjustment, randomMessages.get(offset).toByteArray());
  }

  /**
   * Compute and send a bit indicating whether the sender should switch the zero
   * and one message around.
   *
   * @param choiceBit
   *          The actual choice bit of the receiver
   */
  private void sendSwitchBit(boolean choiceBit) {
    // Since we can only send a byte array we use 0x00 to indicate a 0-choice
    // and 0x01 to indicate a 1-choice
    byte[] switchBit = new byte[] { 0x00 };
    // Set the choice to 0x01 if the sender should switch the 0 and 1 messages
    if (choiceBit ^ choices.getBit(offset, false) == true) {
      switchBit[0] = 0x01;
    }
    network.send(resources.getOtherId(), switchBit);
  }
}
