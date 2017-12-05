package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.FailedOtException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

/**
 * Class implementing a batched random OT protocol, allowing the sending and
 * receiving of any type of objects and of any lengths. Use the underlying
 * random OT protocol to construct "numMessages" in a batch. Then use each of
 * these messages as a seed to a PRG and extend them to "sizeOfEachMessage"
 * bits.
 * 
 * @author jot2re
 *
 * @param <T>
 *          The objects to send/receive
 */
public class BristolRotBatch implements RotBatch<StrictBitVector> {
  protected RotSender sender;
  protected RotReceiver receiver;

  /**
   * Constructs a new batched random OT protocol using an already constructed random OT
   * extension protocol.
   * 
   * @param rot
   *          The random OT protocol to use internally
   */
  public BristolRotBatch(Rot rot) {
    this.sender = rot.getSender();
    this.receiver = rot.getReceiver();
  }

  @Override
  public List<Pair<StrictBitVector, StrictBitVector>> send(int numMessages, int sizeOfEachMessage)
      throws MaliciousOtException, FailedOtException {
    try {
      List<Pair<StrictBitVector, StrictBitVector>> res = new ArrayList<>(numMessages);
      Pair<List<StrictBitVector>, List<StrictBitVector>> messages = sender
          .extend(numMessages);
      List<StrictBitVector> rawZeroMessages = messages.getFirst();
      List<StrictBitVector> rawOneMessages = messages.getSecond();
      for (int i = 0; i < numMessages; i++) {
        StrictBitVector zeroMessage = computeRandomMessage(
            rawZeroMessages.get(i), sizeOfEachMessage);
        StrictBitVector oneMessage = computeRandomMessage(rawOneMessages.get(i),
            sizeOfEachMessage);
        Pair<StrictBitVector, StrictBitVector> currentPair = new Pair<>(
            zeroMessage, oneMessage);
        res.add(currentPair);
      }
      return res;
    } catch (MaliciousOtExtensionException e) {
      throw new MaliciousOtException(
          "Cheating occured in the underlying OT extension: " + e.getMessage());
    } catch (FailedOtExtensionException | NoSuchAlgorithmException e) {
      throw new FailedOtException(
          "The underlying OT extension failed: " + e.getMessage());
    }
  }

  @Override
  public List<StrictBitVector> receive(StrictBitVector choiceBits, int sizeOfEachMessage)
      throws MaliciousOtException, FailedOtException {
    try {
      List<StrictBitVector> res = new ArrayList<>(choiceBits.getSize());
      List<StrictBitVector> messages = receiver.extend(choiceBits);
      for (int i = 0; i < choiceBits.getSize(); i++) {
        StrictBitVector newMessage = computeRandomMessage(messages.get(i),
            sizeOfEachMessage);
        res.add(newMessage);
      }
      return res;
    } catch (FailedOtExtensionException | NoSuchAlgorithmException e) {
      throw new FailedOtException(
          "The underlying OT extension failed: " + e.getMessage());
    }
  }

  /**
   * Use "seed" as a seed to a PRG and construct a new messages of
   * "sizeOfMessage" bits using this PRG.
   * 
   * @param seed
   *          The seed for the PRG
   * @param sizeOfMessage
   *          Size in bits of the message to construct
   * @return A random messages generated using a PRG
   * 
   * @throws NoSuchAlgorithmException
   *           Thrown if the underlying PRG algorithm does not exist
   */
  private StrictBitVector computeRandomMessage(StrictBitVector seed,
      int sizeOfMessage) throws NoSuchAlgorithmException {
    // TODO change to SHA-256
    SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
    rand.setSeed(seed.toByteArray());
    return new StrictBitVector(sizeOfMessage, rand);
  }
}
