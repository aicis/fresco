package dk.alexandra.fresco.tools.ot.otextension;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Protocol class for the party acting as the receiver in an correlated OT with
 * errors extension.
 * 
 * @author jot2re
 *
 */
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
   */
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // Complete the seed OTs acting as the sender (NOT the receiver)
    for (int i = 0; i < getkBitLength(); i++) {
      StrictBitVector seedZero = new StrictBitVector(getkBitLength(),
          getRand());
      StrictBitVector seedFirst = new StrictBitVector(getkBitLength(),
          getRand());
      ot.send(seedZero, seedFirst);
      seeds.add(new Pair<>(seedZero, seedFirst));
      // Initialize the PRGs with the random messages
      SecureRandom prgZero = makePrg(seedZero);
      SecureRandom prgFirst = makePrg(seedFirst);
      prgs.add(new Pair<>(prgZero, prgFirst));
    }
    initialized = true;
  }

  /**
   * Constructs a new batch of correlated OTs with errors.
   * 
   * @param choices
   *          The receivers random choices for this extension. This MUST have
   *          size 8*2^x for some x >=0.
   * @return A list of pairs consisting of the bit choices, followed by the
   *         received messages
   */
  public List<StrictBitVector> extend(StrictBitVector choices) {
    if (choices.getSize() < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    if (choices.getSize() % 8 != 0) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer divisize by 8");
    }
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte)
    int bytesNeeded = choices.getSize() / 8;
    // Use prgs to expand the seeds
    List<StrictBitVector> tlistZero = new ArrayList<>(getkBitLength());
    List<StrictBitVector> ulist = new ArrayList<>(getkBitLength());
    for (int i = 0; i < getkBitLength(); i++) {
      // Expand the seed OTs using a prg and store the result in tlistZero
      byte[] byteBuffer = new byte[bytesNeeded];
      prgs.get(i).getFirst().nextBytes(byteBuffer);
      StrictBitVector tzero = new StrictBitVector(byteBuffer,
          choices.getSize());
      tlistZero.add(tzero);
      byteBuffer = new byte[bytesNeeded];
      prgs.get(i).getSecond().nextBytes(byteBuffer);
      // Compute the u list, i.e. tzero XOR tone XOR randomChoices
      // Note that this is an in-place call and thus tone gets modified
      StrictBitVector tone = new StrictBitVector(byteBuffer,
          choices.getSize());
      tone.xor(tzero);
      tone.xor(choices);
      ulist.add(tone);
    }
    sendList(ulist);
    // Complete tilt-your-head by transposing the message "matrix"
    return Transpose.transpose(tlistZero);
  }
}
