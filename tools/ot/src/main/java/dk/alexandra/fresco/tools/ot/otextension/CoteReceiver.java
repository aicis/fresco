package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class CoteReceiver extends CoteShared {
  // Random messages used for the seed OTs
  private List<Pair<StrictBitVector, StrictBitVector>> seeds;
  private List<Pair<SecureRandom, SecureRandom>> prgs;

  /**
   * Constructs a correlated OT extension with errors receiver instance.
   * 
   * @param myId
   *          The ID of the calling party
   * @param otherId
   *          ID of the other party to execute with
   * @param kbitLength
   *          The computational security parameter
   * @param lambdaSecurityParam
   *          The statistical security parameter
   * @param rand
   *          The current party's cryptographically secure randomness generator
   * @param network
   *          The network object used to communicate with the other party
   */
  public CoteReceiver(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam,
      Random rand, Network network) {
    super(myId, otherId, kbitLength, lambdaSecurityParam, rand, network);
    this.seeds = new ArrayList<>(kbitLength);
    this.prgs = new ArrayList<>(kbitLength);
  }

  /**
   * Initializes the correlated OT extension with errors by running true seed
   * OTs. This should only be done once for a given sender/receiver pair.
   * 
   * @throws NoSuchAlgorithmException
   *           Thrown if the underlying PRG algorithm does not exist.
   */
  public void initialize() throws FailedOtExtensionException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // Complete the seed OTs acting as the sender (NOT the receiver)
    for (int i = 0; i < kbitLength; i++) {
      StrictBitVector seedZero = new StrictBitVector(kbitLength, rand);
      StrictBitVector seedFirst = new StrictBitVector(kbitLength, rand);
      ot.send(seedZero, seedFirst);
      seeds.add(new Pair<>(seedZero, seedFirst));
      // Initialize the PRGs with the random messages
      SecureRandom prgZero = null;
      SecureRandom prgFirst = null;
      try {
        prgZero = SecureRandom.getInstance("SHA1PRNG");
        prgFirst = SecureRandom.getInstance("SHA1PRNG");
      } catch (NoSuchAlgorithmException e) {
        throw new FailedOtExtensionException(
            "Random OT extension failed. No malicious behaviour detected. "
                + "Failure was caused by the following internal error: "
                + e.getMessage());
      }
      prgZero.setSeed(seedZero.toByteArray());
      prgFirst.setSeed(seedFirst.toByteArray());
      prgs.add(new Pair<>(prgZero, prgFirst));
    }
    initialized = true;
  }

  /**
   * Constructs a new batch of correlated OTs with errors.
   * 
   * @return A list of pairs consisting of the bit choices, followed by the
   *         received messages
   */
  public List<StrictBitVector> extend(StrictBitVector randomChoices) {
    if (randomChoices.getSize() < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    if (randomChoices.getSize() % 8 != 0) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer divisize by 8");
    }
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte)
    int bytesNeeded = randomChoices.getSize() / 8;
    // Use prgs to expand the seeds
    List<StrictBitVector> tvecZero = new ArrayList<>(kbitLength);
    // u vector
    List<StrictBitVector> uvec = new ArrayList<>(kbitLength);
    for (int i = 0; i < kbitLength; i++) {
      // Expand the seed OTs using a prg
      byte[] byteBuffer = new byte[bytesNeeded];
      prgs.get(i).getFirst().nextBytes(byteBuffer);
      StrictBitVector tzero = new StrictBitVector(byteBuffer,
          randomChoices.getSize());
      tvecZero.add(tzero);
      byteBuffer = new byte[bytesNeeded];
      prgs.get(i).getSecond().nextBytes(byteBuffer);
      // Compute the u vector, i.e. tZero XOR tFirst XOR randomChoices
      // Note that this is an in-place call and thus tFirst gets modified
      StrictBitVector tone = new StrictBitVector(byteBuffer,
          randomChoices.getSize());
      tone.xor(tzero);
      tone.xor(randomChoices);
      uvec.add(tone);
    }
    sendList(uvec);
    // Complete tilt-your-head by transposing the message "matrix"
    return Transpose.transpose(tvecZero);
  }
}
