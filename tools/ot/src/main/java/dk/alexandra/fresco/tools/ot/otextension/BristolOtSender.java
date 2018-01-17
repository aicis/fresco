package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.Arrays;
import java.util.List;

/**
 * Protocol class for the party acting as the sender in an OT extension. The
 * functionality works by doing a batch of random OTs the first time the receive
 * command is called and storing these results internally. When it runs out, it
 * automatically processes a new batch. These random OTs are adjusted to work as
 * chosen bit/message 1-out-of-2 OTs.
 */
public class BristolOtSender extends BristolOtShared {
  // The internal random OT sender functionality used
  private final RotSender sender;
  // The random messages generated in the underlying random OT functionality
  private Pair<List<StrictBitVector>, List<StrictBitVector>> randomMessages;
  // Index of the current random OT to use
  private int offset = -1;

  /**
   * Initializes the underlying Rot functionality using {@code rotSender}. It
   * will then construct OTs in batches of {@code batchSize}.
   *
   * @param rotSender
   *          The underlying receiver object to use
   * @param batchSize
   *          The amount of OTs to construct at a time, internally.
   */
  public BristolOtSender(RotSender rotSender, int batchSize) {
    super(rotSender, batchSize);
    this.sender = rotSender;
  }

  /**
   * Send the serialized message from the current 1-out-of-2 OT.
   *
   * @param messageZero
   *          The message to send for choice zero
   * @param messageOne
   *          The message to send for choice one
   */
  public void send(byte[] messageZero, byte[] messageOne) {
    // Check if there is still an unused random OT stored, if not, execute a
    // random OT extension
    if (offset < 0 || offset >= getBatchSize()) {
      randomMessages = sender.extend(getBatchSize());
      offset = 0;
    }
    doActualSend(messageZero, messageOne);
    offset++;
  }

  /**
   * Adjust the random, preprocessed message, to fit the specific messages to
   * send.
   *
   * @param messageZero
   *          The actual zero message to send
   * @param messageOne
   *          The actual one message to send
   */
  private void doActualSend(byte[] messageZero, byte[] messageOne) {
    // Find the correct preprocessed random OT messages
    StrictBitVector randomZero = randomMessages.getFirst().get(offset);
    StrictBitVector randomOne = randomMessages.getSecond().get(offset);
    int maxLength = Math.max(messageZero.length, messageOne.length);
    // Receive a bit from the receiver indicating whether the zero and one
    // messages should be switched around
    byte[] switchBit = getNetwork().receive(getOtherId());
    // If false (indicated by byte 0x00), then don't switch around
    if (switchBit[0] == 0x00) {
      sendAdjustedMessage(messageZero, maxLength, randomZero.toByteArray());
      sendAdjustedMessage(messageOne, maxLength, randomOne.toByteArray());
    } else {
      sendAdjustedMessage(messageOne, maxLength, randomZero.toByteArray());
      sendAdjustedMessage(messageZero, maxLength, randomOne.toByteArray());
    }
  }

  private void sendAdjustedMessage(byte[] realMessage, int maxLength,
      byte[] randomMessage) {
    byte[] toSend = new byte[maxLength];
    // Use the random message as a a seed for a PRG
    Drbg currentPrg = new AesCtrDrbg(randomMessage);
    // Use this to make a pseudorandom string the length of the message to
    // send
    currentPrg.nextBytes(toSend);
    byte[] paddedMessage = Arrays.copyOf(realMessage, maxLength);
    // XOR the pseudorandom string onto the message
    ByteArrayHelper.xor(toSend, paddedMessage);
    // Finally send the result
    getNetwork().send(getOtherId(), toSend);
  }
}
