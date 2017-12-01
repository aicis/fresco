package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;

/**
 * Protocol class for the party acting as the receiver in an OT extension. The
 * functionality works by doing a batch of random OTs the first time the receive
 * command is called and storing these results internally. When it runs out, it
 * automatically processes a new batch. These random OTs are adjusted to work as
 * chosen bit/message 1-out-of-2 OTs.
 * 
 * @author jot2re
 *
 */
public class BristolOtReceiver extends BristolOtShared {
  // The internal random OT receiver functionality used
  private RotReceiver receiver;
  // The random messages received from the batched random 1-out-of-2 OTs
  private List<StrictBitVector> randomMessages;
  // The random choices from the batched random 1-out-of-2 OTs
  private StrictBitVector choices;
  private int offset = -1;

  public BristolOtReceiver(RotReceiver rotReceiver, int batchSize) {
    super(rotReceiver, batchSize);
    this.receiver = rotReceiver;
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
  public void initialize() throws FailedOtExtensionException,
      MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, MaliciousOtExtensionException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    if (receiver.initialized == false) {
      receiver.initialize();
    }
    initialized = true;
  }

  /**
   * Receive the serialized message from the current 1-out-of-2 OT.
   * 
   * @param choiceBit
   *          Choice-bit. False for message 0, true for message 1.
   * @return The serialized message from the OT
   * @throws FailedOtExtensionException
   *           Thrown if something, non-malicious, went wrong
   * @throws MaliciousOtException
   *           Thrown if cheating occurred
   * @throws NoSuchAlgorithmException
   *           Thrown if the underlying PRG algorithm does not exist.
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
  public byte[] receive(Boolean choiceBit) throws FailedOtExtensionException,
      MaliciousOtException, NoSuchAlgorithmException,
      MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, MaliciousOtExtensionException {
    // Initialize the underlying functionalities
    if (initialized == false) {
      initialize();
    }
    // Check if there is still an unused random OT stored, if not, execute a
    // random OT extension
    if (offset < 0 || offset >= batchSize) {
      choices = new StrictBitVector(batchSize, getRand());
      randomMessages = receiver.extend(choices);
      offset = 0;
    }
    byte[] res = doActualReceive(choiceBit);
    offset++;
    return res;
  }

  private byte[] doActualReceive(Boolean choiceBit)
      throws MaliciousOtException, FailedOtExtensionException {
    // Notify the sender if it should switch the 0 and 1 messages around (s.t.
    // the random choice bit in the preprocessed random OTs matches the true
    // choice bit
    sendSwitchBit(choiceBit);
    // Receive the serialized adjusted messages
    byte[] zeroAdjustment = getNetwork().receive(getOtherId());
    byte[] oneAdjustment = getNetwork().receive(getOtherId());
    if (zeroAdjustment.length != oneAdjustment.length) {
      throw new MaliciousOtException(
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

  private void adjustMessage(byte[] adjustment)
      throws FailedOtExtensionException {
    // Retrieve the random preprocessed message
    byte[] randomMessage = randomMessages.get(offset).toByteArray();
    try {
      // Use the random message as the seed to a PRG
      SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
      rand.setSeed(randomMessage);
      byte[] randomness = new byte[adjustment.length];
      // Expand the seed to the length of the received message from the sender
      rand.nextBytes(randomness);
      // Use XOR to unpad the received message
      ByteArrayHelper.xor(adjustment, randomness);
    } catch (NoSuchAlgorithmException e) {
      throw new FailedOtExtensionException(e.getMessage());
    }
  }
}
