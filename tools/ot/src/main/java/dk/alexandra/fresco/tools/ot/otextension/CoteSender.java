package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;

/**
 * Protocol class for the party acting as the sender in an correlated OT with
 * errors extension.
 * 
 * @author jot2re
 *
 */
public class CoteSender extends CoteShared {

  // The prgs based on the seeds learned from OT
  private List<SecureRandom> prgs;
  // The random messages choices for the random seed OTs
  private byte[] otChoices;

  /**
   * Construct a sending party for an instance of the correlated OT protocol.
   * 
   * @param otherId
   *          The ID of the receiving party
   * @param kbitLength
   *          Computational security parameter. Must be a positive number
   *          divisible by 8
   * @param lambdaSecurityParam
   *          The statistical security parameter. Must be positive.
   * @param rand
   *          The cryptographically secure and private randomness generator of
   *          this party. Must not be null and must be initialized.
   * @param network
   *          The network interface. Must not be null and must be initialized.
   */
  public CoteSender(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam,
      Random rand, Network network) {
    super(myId, otherId, kbitLength, lambdaSecurityParam, rand, network);
    this.otChoices = new byte[kbitLength / 8];
    this.prgs = new ArrayList<>(kbitLength);
  }

  /**
   * Initialize the correlated OT with errors extension. This should only be
   * called once as it completes extensive seed OTs.
   * 
   * @throws NoSuchAlgorithmException
   *           Thrown if the PRG algorithm used does not exist
   */
  public void initialize() throws NoSuchAlgorithmException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    rand.nextBytes(otChoices);
    // Complete the seed OTs acting as the receiver (NOT the sender)
    for (int i = 0; i < kbitLength; i++) {
      BigInteger message = ot.receive(ByteArrayHelper.getBit(otChoices, i));
      // Initialize the PRGs with the random messages
      // TODO should be changed to something that uses SHA-256
      SecureRandom prg = SecureRandom.getInstance("SHA1PRNG");
      prg.setSeed(message.toByteArray());
      prgs.add(prg);
    }
    initialized = true;
  }

  /**
   * Returns a clone of the random bit choices used for OT.
   * 
   * @return A clone of the OT choices
   */
  public byte[] getDelta() {
    return otChoices.clone();
  }

  /**
   * Constructs a new batch of correlated OTs with errors.
   * 
   * @param size
   *          Amount of OTs to construct
   */
  public List<byte[]> extend(int size) {
    if (size < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    if (size % 8 != 0) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer divisize by 8");
    }
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte), rounding up
    int bytesNeeded = size / 8;
    List<byte[]> tvec = new ArrayList<>(kbitLength);
    for (int i = 0; i < kbitLength; i++) {
      // Expand the message learned from the seed OTs using a PRG
      byte[] tval = new byte[bytesNeeded];
      prgs.get(i).nextBytes(tval);
      tvec.add(tval);
    }
    List<byte[]> uvec = receiveList(kbitLength);
    // Compute the q vector based on the random choices from the seed OTs, i.e
    // qVec = otChoices AND uVec XOR tVec
    for (int i = 0; i < kbitLength; i++) {
      if (ByteArrayHelper.getBit(otChoices, i) == true) {
        ByteArrayHelper.xor(tvec.get(i), uvec.get(i));
      }
    }
    // Complete tilt-your-head by transposing the message "matrix"
    return Transpose.transpose(tvec);
  }

}
