package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;

/**
 * Protocol class for the party acting as the sender in an OT extension. The
 * functionality works by doing a batch of random OTs the first time the receive
 * command is called and storing these results internally. When it runs out, it
 * automatically processes a new batch. These random OTs are adjusted to work as
 * chosen bit/message 1-out-of-2 OTs.
 * 
 * @author jot2re
 *
 */
public class BristolOtSender extends BristolOtShared {
  // The internal random OT sender functionality used
  private RotSender sender;
  // The random messages generated in the underlying random OT functionality
  private Pair<List<StrictBitVector>, List<StrictBitVector>> randomMessages;
  // Index of the current random OT to use
  private int offset = -1;

  public BristolOtSender(RotSender rotSender, int batchSize) {
    super(rotSender, batchSize);
    this.sender = rotSender;
  }

  /**
   * Initializes the underlying random OT functionality, if needed.
   * 
   * @throws FailedOtExtensionException
   *           Thrown if something, non-malicious, went wrong
   * @throws MaliciousCommitmentException
   *           Thrown if cheating occurred in the underlying commitments
   * @throws FailedCommitmentException
   *           Thrown if something, non-malicious, went wrong in the underlying
   *           commitments
   * @throws FailedCoinTossingException
   *           Thrown if something, non-malicious, went wrong in the underlying
   *           coin-tossing
   * @throws MaliciousOtExtensionException
   *           Thrown if cheating occurred
   */
  public void initialize()
      throws MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, FailedOtExtensionException,
      MaliciousOtExtensionException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    if (sender.initialized == false) {
      sender.initialize();
    }
    initialized = true;
  }

  /**
   * Send the serialized message from the current 1-out-of-2 OT.
   * 
   * @param messageZero
   *          The message to send for choice zero
   * @param messageOne
   *          The message to send for choice one
   * @throws FailedOtExtensionException
   *           Thrown if something, non-malicious, went wrong
   * @throws MaliciousCommitmentException
   *           Thrown if cheating occurred in the underlying commitments
   * @throws FailedCommitmentException
   *           Thrown if something, non-malicious, went wrong in the underlying
   *           commitments
   * @throws FailedCoinTossingException
   *           Thrown if something, non-malicious, went wrong in the underlying
   *           coin-tossing
   * @throws MaliciousOtExtensionException
   *           Thrown if cheating occurred
   */
  public void send(byte[] messageZero, byte[] messageOne)
      throws MaliciousOtExtensionException, FailedOtExtensionException,
      MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException {
    // Initialize the underlying functionalities if needed
    if (initialized == false) {
      initialize();
    }
    // Check if there is still an unused random OT stored, if not, execute a
    // random OT extension
    if (offset < 0 || offset >= batchSize) {
      randomMessages = sender.extend(batchSize);
      offset = 0;
    }
    doActualSend(messageZero, messageOne);
    offset++;
  }

  private void doActualSend(byte[] messageZero, byte[] messageOne)
      throws FailedOtExtensionException {
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
      byte[] randomMessage) throws FailedOtExtensionException {
    byte[] toSend;
    try {
      // Use the random message as a a seed for a PRG
      SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
      rand.setSeed(randomMessage);
      toSend = new byte[maxLength];
      // Use this to make a pseudorandom string the length of the message to
      // send
      rand.nextBytes(toSend);
    } catch (NoSuchAlgorithmException e) {
      throw new FailedOtExtensionException(e.getMessage());
    }
    byte[] paddedMessage = Arrays.copyOf(realMessage, maxLength);
    // XOR the pseudorandom string onto the message
    ByteArrayHelper.xor(toSend, paddedMessage);
    // Finally send the result
    getNetwork().send(getOtherId(), toSend);
  }
}
