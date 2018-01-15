package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.List;

/**
 * Protocol class for the party acting as the receiver in an OT extension. The
 * functionality works by doing a batch of random OTs the first time the receive
 * command is called and storing these results internally. When it runs out, it
 * automatically processes a new batch. These random OTs are adjusted to work as
 * chosen bit/message 1-out-of-2 OTs.
 */
public class BristolOtReceiver extends BristolOtShared {
  // The internal random OT receiver functionality used
  private final RotReceiver receiver;
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
   *          The underlying receiver object to use
   * @param batchSize
   *          The amount of OTs to construct at a time, internally.
   */
  public BristolOtReceiver(RotReceiver rotReceiver, int batchSize) {
    super(rotReceiver, batchSize);
    this.receiver = rotReceiver;
  }

  /**
   * Receive the serialized message from the current 1-out-of-2 OT.
   *
   * @param choiceBit
   *          Choice-bit. False for message 0, true for message 1.
   * @return The serialized message from the OT
   */
  public byte[] receive(Boolean choiceBit) {
    // Check if there is still an unused random OT stored, if not, execute a
    // random OT extension
    if (offset < 0 || offset >= getBatchSize()) {
      choices = new StrictBitVector(getBatchSize(), getRand());
      randomMessages = receiver.extend(choices);
      offset = 0;
    }
    // Notify the sender if it should switch the 0 and 1 messages around (s.t.
    // the random choice bit in the preprocessed random OTs matches the true
    // choice bit
    sendSwitchBit(choiceBit);
    // Receive the serialized adjusted messages
    byte[] zeroAdjustment = getNetwork().receive(getOtherId());
    byte[] oneAdjustment = getNetwork().receive(getOtherId());
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
    adjustMessage(adjustment);
    return adjustment;
  }

  /**
   * Compute and send a bit indicating whether the sender should switch the zero
   * and one message around.
   *
   * @param choiceBit
   *          The actual choice bit of the receiver
   */
  private void sendSwitchBit(Boolean choiceBit) {
    // Since we can only send a byte array we use 0x00 to indicate a 0-choice
    // and 0x01 to indicate a 1-choice
    byte[] switchBit = new byte[] { 0x00 };
    // Set the choice to 0x01 if the sender should switch the 0 and 1 messages
    if (choiceBit ^ choices.getBit(offset, false) == true) {
      switchBit[0] = 0x01;
    }
    getNetwork().send(getOtherId(), switchBit);
  }

  private void adjustMessage(byte[] adjustment) {
    // Retrieve the random preprocessed message
    byte[] randomMessage = randomMessages.get(offset).toByteArray();
    // Use the random message as the seed to a PRG
    Drbg currentPrg = new AesCtrDrbg(randomMessage);
    byte[] randomness = new byte[adjustment.length];
    // Expand the seed to the length of the received message from the sender
    currentPrg.nextBytes(randomness);
    // Use XOR to one-time unpad the message
    ByteArrayHelper.xor(adjustment, randomness);
  }
}
