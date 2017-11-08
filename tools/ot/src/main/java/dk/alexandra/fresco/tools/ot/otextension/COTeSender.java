package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;

/**
 * Protocol class for the party acting as the sender in an correlated OT with
 * errors extension.
 * 
 * @author jot2re
 *
 */
public class COTeSender extends COTeShared {

  // The prgs based on the seeds learned from OT
  private List<SecureRandom> prgs;
  // The random messages choices for the random seed OTs
  private List<Boolean> otChoices;

  public COTeSender(int otherID, int kBitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    super(otherID, kBitLength, lambdaSecurityParam, rand, network);
    this.otChoices = new ArrayList<>(kBitLength);
    this.prgs = new ArrayList<>(kBitLength);
  }

  /**
   * Initialize the correlated OT with errors extension. This should only be
   * called once as it completes extensive seed OTs.
   */
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // Complete the seed OTs acting as the receiver (NOT the sender)
    for (int i = 0; i < kBitLength; i++) {
      Boolean choiceBit = rand.nextBoolean();
      otChoices.add(choiceBit);
      BigInteger message = ot.receive(choiceBit);
      // Initialize the PRGs with the random messages
      SecureRandom prg = new SecureRandom(message.toByteArray());
      prgs.add(prg);
    }
    initialized = true;
  }

  /**
   * Constructs a new batch of correlated OTs with errors.
   * 
   * @param size
   *          Amount of OTs to construct
   */
  public List<Pair<byte[], byte[]>> extend(int size) {
    if (size < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    if (!initialized) {
      initialize();
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte), rounding up
    int bytesNeeded = (size + 8 - 1) / 8;
    List<byte[]> tVec = new ArrayList<>(kBitLength);
    for (int i = 0; i < kBitLength; i++) {
      // Expand the message learned from the seed OTs using a PRG
      byte[] tVal = new byte[bytesNeeded];
      prgs.get(i).nextBytes(tVal);
      tVec.add(tVal);
    }
    List<byte[]> uVec = receiveList(kBitLength);
    List<byte[]> qVec = new ArrayList<>(kBitLength);
    // Compute the q vector based on the random choices from the seed OTs, i.e
    // qVec = otChoices AND uVec XOR tVec
    for (int i = 0; i < kBitLength; i++) {
      byte[] currentArr;
      if (otChoices.get(i) == true)
        currentArr = xor(uVec.get(i), tVec.get(i));
      else
        currentArr = tVec.get(i);
      qVec.add(currentArr);
    }
    // Complete tilt-your-head by transposing the message "matrix"
    List<Pair<byte[], byte[]>> messages = new ArrayList<>(size);
    // TODO
    return messages;
  }
}
